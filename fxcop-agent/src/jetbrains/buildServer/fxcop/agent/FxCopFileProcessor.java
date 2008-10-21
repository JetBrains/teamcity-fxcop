/*
 * Copyright (c) 2008, JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.fxcop.agent;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.XppReader;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Stack;
import java.util.Vector;
import jetbrains.buildServer.agent.SimpleBuildLogger;
import jetbrains.buildServer.agent.inspections.*;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopFileProcessor {
  private enum PassType {
    ISSUES, RULES
  }

  private enum EntityType {
    TARGET, RESOURCE, NAMESPACE, TYPE, MEMBER, ACCESSOR
  }

  private Stack<String> myMessageInspectionId = new Stack<String>();
  private Stack<EntityType> myCurrentEntity = new Stack<EntityType>();
  private String myCurrentTarget;
  private String myCurrentResource;
  private String myCurrentNamespace;
  private String myCurrentType;
  private String myCurrentMember;
  private String myCurrentAccessor;

  private PassType myCurrentPass;

  private final File myFxCopReport;
  private final SimpleBuildLogger myLogger;
  private final String mySourceFilePrefix;
  private final InspectionReporter myReporter;
  private HierarchicalStreamReader myStream = null;

  public FxCopFileProcessor(@NotNull final File fxcopReport,
                            @NotNull final String sourceFilePrefix,
                            @NotNull final SimpleBuildLogger logger,
                            @NotNull final InspectionReporter reporter) {
    myFxCopReport = fxcopReport;
    myLogger = logger;
    mySourceFilePrefix = sourceFilePrefix;
    myReporter = reporter;
  }

  public void processReport() throws IOException {
    myCurrentPass = PassType.RULES;
    handleFile();

    myCurrentPass = PassType.ISSUES;
    handleFile();
  }

  private void handleFile() throws IOException {
    Reader reader = new BufferedReader(
      new InputStreamReader(new FileInputStream(myFxCopReport), "UTF8"));

    try {
      try {
        myStream = new XppReader(reader);

        myCurrentEntity.clear();
        handleChildren();
      } finally {
        if (myStream != null) myStream.close();
      }
    } finally {
      reader.close();
    }
  }

  private Method getHandlerMethod(final String tagName) throws NoSuchMethodException {
    return getClass().getDeclaredMethod("handle" + tagName + "Tag");
  }

  private void handleChildren() {
    while (myStream.hasMoreChildren()) {
      myStream.moveDown();

      final String nodeName = myStream.getNodeName();
      try {
        Method handler = getHandlerMethod(nodeName);
        //handler.setAccessible(true);
        handler.invoke(this);
      } catch (NoSuchMethodException e) {
        myLogger.message("Won't handle tag " + nodeName);
      } catch (InvocationTargetException e) {
        myLogger.error(e.toString());
        if (e.getTargetException() != null) {
          myLogger.error(e.getTargetException().toString());                 
        }
      } catch (IllegalAccessException e) {
        myLogger.error(e.toString());
      }
      myStream.moveUp();
    }
  }

  private void handleNamespacesTag() {
    handleChildren();
  }

  private void handleNamespaceTag() {
    myCurrentNamespace = myStream.getAttribute("Name");
    myCurrentEntity.push(EntityType.NAMESPACE);
    handleChildren();
    myCurrentEntity.pop();
    myCurrentNamespace = null;
  }

  private void handleMessagesTag() {
    handleChildren();
  }

  private void handleMessageTag() {
    myMessageInspectionId.add(myStream.getAttribute("TypeName") +
                              " " + myStream.getAttribute("CheckId"));
    handleChildren();
    myMessageInspectionId.pop();
  }

  private String getEntitySpec() {
    if (myCurrentEntity.isEmpty()) {
      return "_none_/_none_";
    }

    switch (myCurrentEntity.peek()) {
      case NAMESPACE:
        return "_Namespaces_/" + myCurrentNamespace;
      case TARGET:
        return myCurrentTarget + "/_assembly_";
      case RESOURCE:
        return myCurrentTarget + "/_resources_/" + myCurrentResource;
      case TYPE:
      case MEMBER:
      case ACCESSOR:
        return myCurrentTarget + "/" + myCurrentNamespace.replace(".", "/") + "/" + myCurrentType;
      default:
        return "_unknown_/_unknown_";
    }
  }

  private void handleIssueTag() {
    if (myCurrentPass != PassType.ISSUES) {
      return;
    }

    InspectionInstance info = new InspectionInstance();

    final String path = myStream.getAttribute("Path");
    final String file = myStream.getAttribute("File");
    final String line = myStream.getAttribute("Line");
    final String level = myStream.getAttribute("Level");

    String inspectionMessage = reformatInOneLine(myStream.getValue());
    if (myCurrentEntity.peek() == EntityType.MEMBER) {
      inspectionMessage += " (" + myCurrentMember + ")";
    } else if (myCurrentEntity.peek() == EntityType.ACCESSOR) {
      inspectionMessage += " (" + myCurrentAccessor + ")";
    }
    info.setMessage(inspectionMessage);

    String inspectionFile = getEntitySpec();
    if (!StringUtil.isEmptyOrSpaces(file)) {
      if (StringUtil.isEmptyOrSpaces(path)) {
        inspectionFile += " :: " + file;
      } else {
        String reportPath = path;

        if (reportPath.toLowerCase().startsWith(mySourceFilePrefix.toLowerCase())) {
          reportPath = reportPath.substring(mySourceFilePrefix.length());
        }

        reportPath = reportPath.replace('/', '|').replace("\\", "|");
        if (reportPath.startsWith("|")) {
          reportPath = reportPath.substring(1);
        }

        if (reportPath.length() > 0) {
          inspectionFile += " :: " + reportPath + "|" + file;
        } else {
          inspectionFile += " :: " + file;
        }
      }
    }
    info.setFilePath(inspectionFile);

    if (StringUtil.isEmptyOrSpaces(line)) {
      info.setLine(0);
    } else {
      info.setLine(Integer.parseInt(line));
    }

    if (!StringUtil.isEmptyOrSpaces(level)) {
      final InspectionSeverityValues apiLevel = convertLevel(level);

      final Collection<String> attrValue = new Vector<String>();
      attrValue.add(apiLevel.toString());

      info.addAttribute(InspectionAttributesId.SEVERITY.toString(), attrValue);
    }

    info.setInspectionId(myMessageInspectionId.peek());

    myReporter.reportInspection(info);
  }

  private InspectionSeverityValues convertLevel(String level) {
    if (level.contains("Error")) {
      return InspectionSeverityValues.ERROR;
    }

    if (level.contains("Warning")) {
      return InspectionSeverityValues.WARNING;
    }

    return InspectionSeverityValues.INFO;
  }

  private void handleTargetsTag() {
    handleChildren();
  }

  private void handleExceptionsTag() {
    handleChildren();
  }

  private void handleExceptionTag() {
    if (myCurrentPass != PassType.ISSUES) {
      return;
    }
    
    final String keyword = myStream.getAttribute("Keyword");
    final String kind = myStream.getAttribute("Kind");
    final String treatAsWarning = myStream.getAttribute("TreatAsWarning");

    String type = null, message = null, stacktrace = null;
    while (myStream.hasMoreChildren()) {
      myStream.moveDown();

      if (myStream.getNodeName().equals("Type")) {
        type = reformatInOneLine(myStream.getValue());
      }

      if (myStream.getNodeName().equals("ExceptionMessage")) {
        message = reformatInOneLine(myStream.getValue());
      }

      if (myStream.getNodeName().equals("StackTrace")) {
        stacktrace = myStream.getValue();
      }

      myStream.moveUp();
    }

    final boolean warningMessage = treatAsWarning != null && treatAsWarning.equals("True");
    final StringBuilder descr = new StringBuilder("FxCop " + (warningMessage ? "warning" : "error") + ":");

    if (keyword != null) {
      descr.append(" Keyword=").append(keyword);
    }
    if (kind != null) {
      descr.append(" Kind=").append(kind);
    }
    if (type != null) {
      descr.append(" Type=").append(type);
    }
    if (message != null) {
      descr.append(" ").append(message);
    }
    if (stacktrace != null) {
      final String lineSeparator = System.getProperty("line.separator");
      descr.append(lineSeparator).append(stacktrace);
    }

    myLogger.warning(descr.toString());
  }

  private void handleTargetTag() {
    myCurrentTarget = new File(myStream.getAttribute("Name")).getName();
    myCurrentEntity.push(EntityType.TARGET);
    handleChildren();
    myCurrentEntity.pop();
    myCurrentTarget = null;
  }

  private void handleModulesTag() {
    handleChildren();
  }

  private void handleModuleTag() {
    handleChildren();
  }

  private void handleTypesTag() {
    handleChildren();
  }

  private void handleTypeTag() {
    myCurrentType = myStream.getAttribute("Name");
    myCurrentEntity.push(EntityType.TYPE);
    handleChildren();
    myCurrentEntity.pop();
    myCurrentType = null;
  }

  private void handleMembersTag() {
    handleChildren();
  }

  private void handleMemberTag() {
    myCurrentMember = myStream.getAttribute("Name");
    myCurrentEntity.push(EntityType.MEMBER);
    handleChildren();
    myCurrentEntity.pop();
    myCurrentMember = null;
  }

  private void handleAccessorsTag() {
    handleChildren();
  }

  private void handleAccessorTag() {
    myCurrentAccessor = myStream.getAttribute("Name");
    myCurrentEntity.push(EntityType.ACCESSOR);
    handleChildren();
    myCurrentEntity.pop();
    myCurrentAccessor = null;
  }

  private void handleResourcesTag() {
    handleChildren();
  }

  private void handleResourceTag() {
    myCurrentResource = myStream.getAttribute("Name");
    myCurrentEntity.push(EntityType.RESOURCE);
    handleChildren();
    myCurrentEntity.pop();
    myCurrentResource = null;
  }

  private void handleRulesTag() {
    handleChildren();
  }

  private void handleLocalizedTag() {
    // Do nothing
  }

  private void handleRuleTag() {
    if (myCurrentPass != PassType.RULES) {
      return;
    }

    final InspectionTypeInfo type = new InspectionTypeInfo();

    type.setId(myStream.getAttribute("TypeName") + " " + myStream.getAttribute("CheckId"));
    type.setCategory(myStream.getAttribute("Category"));

    while (myStream.hasMoreChildren()) {
      myStream.moveDown();

      if (myStream.getNodeName().equals("Name")) {
        type.setName(reformatInOneLine(myStream.getValue()));
      }

      if (myStream.getNodeName().equals("Description")) {
        type.setDescription(reformatInOneLine(myStream.getValue()));
      }

      myStream.moveUp();
    }

    myReporter.reportInspectionType(type);
  }

  private String reformatInOneLine(@NotNull final String source) {
    return source.replace("\r", "").replace("\n", " ").replaceAll("\\s+", " ").trim();
  }
}

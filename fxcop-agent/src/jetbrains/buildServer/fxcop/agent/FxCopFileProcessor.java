package jetbrains.buildServer.fxcop.agent;

import com.intellij.openapi.util.text.StringUtil;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.XppReader;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Stack;
import java.util.Vector;
import jetbrains.buildServer.agent.inspections.*;
import jetbrains.buildServer.fxcop.agent.loggers.SimpleLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 27.07.2008
 * Time: 20:00:01
 */
public class FxCopFileProcessor {
  private static char BYTE_ORDER_MARK = 65279;

  private enum PassType {
    ISSUES, RULES
  }

  private Stack<String> myMessageInspectionId = new Stack<String>();
  private PassType myCurrentPass;

  private final File myFxCopReport;
  private final SimpleLogger myLogger;
  private final String mySourceFilePrefix;
  private final InspectionReporter myReporter;
  private HierarchicalStreamReader myStream = null;

  public FxCopFileProcessor(@NotNull final File fxcopReport,
                            @NotNull final String sourceFilePrefix,
                            @NotNull final SimpleLogger logger,
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
        myLogger.info("Won't handle tag " + nodeName);
      } catch (InvocationTargetException e) {
        myLogger.error(e.toString());
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
    handleChildren();
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

  private boolean isNullOrEmpty(String s) {
    return s == null || s.length() == 0;
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

    if (path == null || isNullOrEmpty(file) || isNullOrEmpty(line)) {
      info.setFilePath("-/-");
      info.setLine(0);
    } else {
      if (path.length() == 0) {
        info.setFilePath(file);
      } else {
        String reportPath = path;

        if (reportPath.toLowerCase().startsWith(mySourceFilePrefix.toLowerCase())) {
          reportPath = reportPath.substring(mySourceFilePrefix.length());
        }

        reportPath = reportPath.replace('\\', '/');
        if (reportPath.startsWith("/")) {
          reportPath = reportPath.substring(1);
        }

        if (reportPath.length() > 0) {
          info.setFilePath(reportPath + '/' + file);
        } else {
          info.setFilePath(file);          
        }
      }

      info.setLine(Integer.parseInt(line));
    }

    if (!StringUtil.isEmpty(level)) {
      final InspectionSeverityValues apiLevel = convertLevel(level);

      final Collection<String> attrValue = new Vector<String>();
      attrValue.add(apiLevel.toString());

      info.addAttribute(InspectionAttributesId.SEVERITY.toString(), attrValue);
    }

    info.setMessage(reformatInOneLine(myStream.getValue()));
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

  private void handleTargetTag() {
    handleChildren();
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
    handleChildren();
  }

  private void handleMembersTag() {
    handleChildren();
  }

  private void handleMemberTag() {
    handleChildren();
  }

  private void handleAccessorsTag() {
    handleChildren();
  }

  private void handleAccessorTag() {
    handleChildren();
  }

  private void handleResourcesTag() {
    handleChildren();
  }

  private void handleResourceTag() {
    handleChildren();
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

/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.fxcop.agent;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.fxcop.common.ArtifactsUtil;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.util.AntPatternFileFinder;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopBuildService extends BuildServiceAdapter {
  private final ArtifactsWatcher myArtifactsWatcher;
  private final InspectionReporter myInspectionReporter;
  private File myOutputDirectory;
  private File myXmlReportFile;
  private File myHtmlReportFile;

  public FxCopBuildService(final ArtifactsWatcher artifactsWatcher, final InspectionReporter inspectionReporter) {
    myArtifactsWatcher = artifactsWatcher;
    myInspectionReporter = inspectionReporter;

  }

  @Override
  public void afterInitialized() throws RunBuildException {
    super.afterInitialized();

    try {
      myOutputDirectory = FileUtil.createTempFile(getBuildTempDirectory(), "fxcop-output-", "", false);
      if (!myOutputDirectory.mkdirs()) {
        throw new RuntimeException("Unable to create temp output directory " + myOutputDirectory);
      }

      myXmlReportFile = new File(myOutputDirectory, FxCopConstants.OUTPUT_FILE);
      myHtmlReportFile = new File(myOutputDirectory, FxCopConstants.REPORT_FILE);
    } catch (IOException e) {
      final String message = "Unable to create temporary file in " +
          getBuildTempDirectory() + " for fxcop: " +
          e.getMessage();

      Logger.getInstance(getClass().getName()).error(message, e);
      throw new RunBuildException(message);
    }
  }

  @Override
  public void beforeProcessStarted() throws RunBuildException {
    getLogger().progressMessage("Running FxCop");
  }

  private void importInspectionResults() throws Exception {
    final String workingRoot = getCheckoutDirectory().toString();

    getLogger().progressMessage("Importing inspection results");

    myInspectionReporter.markBuildAsInspectionsBuild();
    final FxCopFileProcessor fileProcessor = new FxCopFileProcessor(myXmlReportFile, workingRoot, getLogger(), myInspectionReporter);
    fileProcessor.processReport();
  }

  private void generateHtmlReport() throws TransformerException, IOException {
    final String fxcopReportXslt = getRunnerParameters().get(FxCopConstants.SETTINGS_REPORT_XSLT);
    if (StringUtil.isEmptyOrSpaces(fxcopReportXslt)) {
      getLogger().message("Skipped html report generation since not requested");
      return;
    }

    final File xsltFile = new File(fxcopReportXslt);
    if (!xsltFile.exists()) {
      getLogger().warning(xsltFile.getAbsolutePath() + " not found => won't generate html report");
      return;
    }

    getLogger().progressMessage("Generating HTML report");

    Source xmlSource = new StreamSource(myXmlReportFile);
    Source xsltSource = new StreamSource(xsltFile);
    final FileOutputStream reportFileStream = new FileOutputStream(myHtmlReportFile);

    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer trans = transformerFactory.newTransformer(xsltSource);

      trans.transform(xmlSource, new StreamResult(reportFileStream));
    } finally {
      reportFileStream.close();
    }

    myArtifactsWatcher.addNewArtifactsPath(myOutputDirectory.getPath() + "/*.html" + "=>" + ArtifactsUtil.getInternalArtifactPath(""));
  }

  @NotNull
  @Override
  public BuildFinishedStatus getRunResult(final int exitCode) {
    String failMessage = null;

    if (exitCode != 0) {
      final EnumSet<FxCopReturnCode> errors = FxCopReturnCode.decodeReturnCode(exitCode);
      StringBuilder exitCodeStr = new StringBuilder("FxCop return code (" + exitCode + "):");
      for (FxCopReturnCode rc : errors) {
        exitCodeStr.append(" ").append(rc.name());
      }

      getLogger().warning(exitCodeStr.toString());

      if (errors.contains(FxCopReturnCode.BUILD_BREAKING_MESSAGE)) {
        failMessage = "FxCop return code contains 'Build breaking message'\"";
      }

      if (errors.contains(FxCopReturnCode.COMMAND_LINE_SWITCH_ERROR)) {
        failMessage = exitCodeStr.toString();
      }

      if (errors.contains(FxCopReturnCode.ANALYSIS_ERROR) ||
          errors.contains(FxCopReturnCode.ASSEMBLY_LOAD_ERROR) ||
          errors.contains(FxCopReturnCode.ASSEMBLY_REFERENCES_ERROR) ||
          errors.contains(FxCopReturnCode.PROJECT_LOAD_ERROR) ||
          errors.contains(FxCopReturnCode.RULE_LIBRARY_LOAD_ERROR) ||
          errors.contains(FxCopReturnCode.UNKNOWN_ERROR) ||
          errors.contains(FxCopReturnCode.OUTPUT_ERROR)) {
        boolean failOnAnalysisErrors = isParameterEnabled(FxCopConstants.SETTINGS_FAIL_ON_ANALYSIS_ERROR);

        if (failOnAnalysisErrors) {
          failMessage = exitCodeStr.toString();
        } else {
          getLogger().warning("Analysis errors ignored as 'Fail on analysis errors' option unchecked");
        }
      }
    }

    if (myXmlReportFile.exists()) {
      myArtifactsWatcher.addNewArtifactsPath(myXmlReportFile.getPath() + "=>" + ArtifactsUtil.getInternalArtifactPath(""));

      try {
        importInspectionResults();
        generateHtmlReport();
      } catch (Exception e) {
        getLogger().error("Exception while importing fxcop results: " + e);
        failMessage = "FxCop results import error";
      }
    } else {
      if (failMessage == null) {
        failMessage = "Output xml from FxCop is not found";
      }
    }

    if (failMessage != null) {
      logBuildProblem(BuildProblemData.createBuildProblem(String.valueOf(exitCode), FxCopConstants.RUNNER_TYPE, failMessage));
      return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
    }

    return BuildFinishedStatus.FINISHED_SUCCESS;
  }

  private boolean isParameterEnabled(final String key) {
    final Map<String, String> runnerParameters = getRunnerParameters();

    return runnerParameters.containsKey(key) && runnerParameters.get(key).equals(Boolean.TRUE.toString());
  }

  @NotNull
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    final Map<String, String> runParameters = getRunnerParameters();

    List<String> files = new ArrayList<String>();
    final String what = runParameters.get(FxCopConstants.SETTINGS_WHAT_TO_INSPECT);
    if (FxCopConstants.WHAT_TO_INSPECT_FILES.equals(what)) {
      try {
        files = matchFiles();
      } catch (IOException e) {
        throw new RunBuildException("I/O error while collecting files", e);
      }

      if (files.size() == 0) {
        throw new RunBuildException("No files matched the pattern");
      }
    }

    final List<String> finalFiles = files;

    final FxCopCommandLineBuilder commandLineBuilder = new FxCopCommandLineBuilder(runParameters, getBuildParameters().getAllParameters(),  myXmlReportFile, getLogger());
    return new ProgramCommandLine() {
      @NotNull
      public String getExecutablePath() throws RunBuildException {
        return commandLineBuilder.getExecutablePath();
      }

      @NotNull
      public String getWorkingDirectory() throws RunBuildException {
        return getCheckoutDirectory().getPath();
      }

      @NotNull
      public List<String> getArguments() throws RunBuildException {
        return commandLineBuilder.getArguments(finalFiles);
      }

      @NotNull
      public Map<String, String> getEnvironment() throws RunBuildException {
        return getBuildParameters().getEnvironmentVariables();
      }
    };
  }

  private List<String> matchFiles() throws IOException {
    final Map<String, String> runParameters = getRunnerParameters();

    final AntPatternFileFinder finder = new AntPatternFileFinder(
      splitFileWildcards(runParameters.get(FxCopConstants.SETTINGS_FILES)),
      splitFileWildcards(runParameters.get(FxCopConstants.SETTINGS_FILES_EXCLUDE)),
      SystemInfo.isFileSystemCaseSensitive);
    final File[] files = finder.findFiles(getCheckoutDirectory());

    getLogger().logMessage(DefaultMessagesInfo.createTextMessage("Matched assembly files:"));

    final List<String> result = new ArrayList<String>(files.length);
    for (File file : files) {
      final String relativeName = FileUtil.getRelativePath(getWorkingDirectory(), file);

      result.add(relativeName);
      getLogger().logMessage(DefaultMessagesInfo.createTextMessage("  " + relativeName));
    }

    if (files.length == 0) {
      getLogger().logMessage(DefaultMessagesInfo.createTextMessage("  none"));
    }

    return result;
  }

  private static String[] splitFileWildcards(final String string) {
    if (string != null) {
      final String filesStringWithSpaces = string.replace('\n', ' ').replace('\r', ' ').replace('\\', '/');
      final List<String> split = StringUtil.splitCommandArgumentsAndUnquote(filesStringWithSpaces);
      return split.toArray(new String[split.size()]);
    }

    return new String[0];
  }
}

/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import jetbrains.buildServer.agent.util.AntPatternFileFinder;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopBuildService extends CommandLineBuildService {
  private final ArtifactsWatcher myArtifactsWatcher;
  private InspectionReporter myInspectionReporter;

  public FxCopBuildService(ArtifactsWatcher artifactsWatcher, final InspectionReporter inspectionReporter) {
    myArtifactsWatcher = artifactsWatcher;
    myInspectionReporter = inspectionReporter;
  }

  @Override
  public void beforeProcessStarted() throws RunBuildException {
    getBuild().getBuildLogger().progressMessage("Running FxCop");

    final File outDir = getOutputDirectory();
    FileUtil.delete(outDir);
    outDir.mkdirs();
  }

  private File getOutputDirectory() {
    return new File(getBuild().getWorkingDirectory(), FxCopConstants.OUTPUT_DIR);
  }

  private File getOutputFile(String shortName) {
    return new File(getOutputDirectory(), shortName);
  }

  private void ImportInspectionResults() throws Exception {
    final String workingRoot = getBuild().getWorkingDirectory().toString();
    final Map<String, String> runParameters = getBuild().getRunnerParameters();

    getLogger().progressMessage("Importing inspection results");

    final FxCopFileProcessor fileProcessor =
      new FxCopFileProcessor(getOutputFile(FxCopConstants.OUTPUT_FILE),
                             workingRoot, getLogger(), myInspectionReporter);

    fileProcessor.processReport();

    final int errors = fileProcessor.getErrorsCount();
    final int warnings = fileProcessor.getWarningsCount();

    boolean limitReached = false;

    final Integer errorLimit = PropertiesUtil.parseInt(runParameters.get(FxCopConstants.SETTINGS_ERROR_LIMIT));
    if (errorLimit != null && errors > errorLimit) {
      getLogger().error("Errors limit reached: found " + errors + " errors, limit " + errorLimit);
      limitReached = true;
    }

    final Integer warningLimit = PropertiesUtil.parseInt(runParameters.get(FxCopConstants.SETTINGS_WARNING_LIMIT));
    if (warningLimit != null && warnings > warningLimit) {
      getLogger().error("Warnings limit reached: found " + warnings + " warnings, limit " + warningLimit);
      limitReached = true;
    }

    if (limitReached) {
      getLogger().message("##teamcity[buildStatus status='FAILURE' " + "text='" + generateBuildStatus(errors, warnings) + "']");
    }
  }

  private String generateBuildStatus(int errors, int warnings) {
    return "Errors: " + errors + ", warnings: " + warnings;
  }

  private void GenerateHtmlReport() throws TransformerException, IOException {
    final String fxcopReportXslt = getBuild().getRunnerParameters().get(FxCopConstants.SETTINGS_REPORT_XSLT);
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

    final File reportFile = getOutputFile(FxCopConstants.REPORT_FILE);

    Source xmlSource = new StreamSource(getOutputFile(FxCopConstants.OUTPUT_FILE));
    Source xsltSource = new StreamSource(xsltFile);
    final FileOutputStream reportFileStream = new FileOutputStream(reportFile);

    try {
      TransformerFactory transformerFactory =
        TransformerFactory.newInstance();
      Transformer trans = transformerFactory.newTransformer(xsltSource);

      trans.transform(xmlSource, new StreamResult(reportFileStream));
    } finally {
      reportFileStream.close();
    }

    myArtifactsWatcher.addNewArtifactsPath(FxCopConstants.OUTPUT_DIR + "/*.html");
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
        boolean failOnAnalysisErrors = isParameterEnabled(
          getBuild().getRunnerParameters(),
          FxCopConstants.SETTINGS_FAIL_ON_ANALYSIS_ERROR);

        if (failOnAnalysisErrors) {
          failMessage = exitCodeStr.toString();
        } else {
          getLogger().warning("Analysis errors ignored as 'Fail on analysis errors' option unchecked");
        }
      }
    }

    if (getOutputFile(FxCopConstants.OUTPUT_FILE).exists()) {
      myArtifactsWatcher.addNewArtifactsPath(FxCopConstants.OUTPUT_DIR + "/*.xml");

      try {
        ImportInspectionResults();
        GenerateHtmlReport();
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
      getLogger().buildFailureDescription(failMessage);
    }

    return failMessage != null
           ? BuildFinishedStatus.FINISHED_FAILED
           : BuildFinishedStatus.FINISHED_SUCCESS;
  }

  private static boolean isParameterEnabled(final Map<String, String> runParameters, final String key) {
    return runParameters.containsKey(key) && runParameters.get(key)
      .equals(Boolean.TRUE.toString());
  }

  @NotNull
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    final AgentRunningBuild build = getBuild();
    final Map<String, String> runParameters = build.getRunnerParameters();

    List<String> files = new ArrayList<String>();
    final String what = runParameters.get(FxCopConstants.SETTINGS_WHAT_TO_INSPECT);
    if (FxCopConstants.WHAT_TO_INSPECT_FILES.equals(what)) {
      try {
        files = matchFiles(build);
      } catch (IOException e) {
        throw new RunBuildException("I/O error while collecting files", e);
      }
    }

    return new SimpleProgramCommandLine(build,
                                        FxCopCommandLineBuilder.getExecutablePath(runParameters),
                                        FxCopCommandLineBuilder.getArguments(runParameters, files));
  }

  private static List<String> matchFiles(AgentRunningBuild build) throws IOException {
    final Map<String, String> runParameters = build.getRunnerParameters();

    final File[] files = AntPatternFileFinder.findFiles(
      build.getCheckoutDirectory(),
      splitFileWildcards(runParameters.get(FxCopConstants.SETTINGS_FILES)),
      splitFileWildcards(runParameters.get(FxCopConstants.SETTINGS_FILES_EXCLUDE)));

    build.getBuildLogger().logMessage(DefaultMessagesInfo.createTextMessage("Matched assembly files:"));

    final List<String> result = new ArrayList<String>(files.length);
    for (File file : files) {
      final String relativeName = FileUtil.getRelativePath(build.getWorkingDirectory(), file);

      result.add(relativeName);
      build.getBuildLogger().logMessage(DefaultMessagesInfo.createTextMessage("  " + relativeName));
    }

    if (files.length == 0) {
      build.getBuildLogger().logMessage(DefaultMessagesInfo.createTextMessage("  none"));
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

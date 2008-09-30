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

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.inspections.InspectionInstance;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.inspections.InspectionTypeInfo;
import jetbrains.buildServer.agent.runner.GenericProgramRunner;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopRunner extends GenericProgramRunner {
  private final ArtifactsWatcher myArtifactsWatcher;
  private final CurrentBuildTracker myCurrentBuild;
  private File myOutputFile = null;
  private File myOutputDir = null;
  private final FxCopDataProcessor myDataProcessor;
  private final FxCopCommandLineBuilder myCommandLineBuilder;

  public FxCopRunner(@NotNull final ArtifactsWatcher artifactsWatcher,
                     @NotNull final CurrentBuildTracker currentBuild,
                     @NotNull final FxCopDataProcessor dataProcessor,
                     @NotNull final FxCopCommandLineBuilder commandLineBuilder) {
    myArtifactsWatcher = artifactsWatcher;
    myCurrentBuild = currentBuild;
    myDataProcessor = dataProcessor;
    myCommandLineBuilder = commandLineBuilder;
  }

  public String getType() {
    return FxCopConstants.RUNNER_TYPE;
  }

  public boolean canRun(final BuildAgentConfiguration agentConfiguration) {
    return true;
  }

  protected void buildCommandLine(
    final GeneralCommandLine cmd, final File workingDir,
    final Map<String, String> runParameters,
    final Map<String, String> buildParameters) throws IOException, RunBuildException {

    final AgentRunningBuild currentBuild = myCurrentBuild.getCurrentBuild();

    myCommandLineBuilder.buildCommandLine(cmd, runParameters);

    myOutputDir = new File(currentBuild.getWorkingDirectory(), FxCopConstants.OUTPUT_DIR);
    FileUtil.delete(myOutputDir);
    myOutputDir.mkdirs();

    myOutputFile = new File(myOutputDir, FxCopConstants.OUTPUT_FILE);

    final BuildProgressLogger logger = currentBuild.getBuildLogger();
    logger.message("Running " + cmd.getCommandLineString());
    logger.progressMessage("Running FxCop");
    logger.flush();
  }

  @Override
  protected void prepareForRunning(final Map<String, String> runParameters, final Map<String, String> buildParameters, final File tempDir)
    throws RunBuildException {
    runParameters.put(AgentRuntimeProperties.FAIL_EXIT_CODE, "false");
  }

  @Override
  protected boolean failBuildOnExitCode(final int code, final RunEnvironment runEnv) {
    final BuildProgressLogger logger = myCurrentBuild.getCurrentBuild().getBuildLogger();

    if (code != 0) {
      final EnumSet<FxCopReturnCode> errors = FxCopReturnCode.decodeReturnCode(code);

      StringBuilder exitCodeStr = new StringBuilder("FxCop return code contains flags:");
      for (FxCopReturnCode rc : errors) {
        exitCodeStr.append(" ").append(rc.name());
      }

      logger.error(exitCodeStr.toString());

      if (errors.contains(FxCopReturnCode.BUILD_BREAKING_MESSAGE)) {
        logger.buildFailureDescription("Return code contains 'Build breaking message'");
      }
    }

    return (code & FxCopReturnCode.BUILD_BREAKING_MESSAGE.getCode()) != 0;
  }

  @Override
  protected void processTerminated(RunEnvironment runEnvironment, boolean badExitCode) {
    final AgentRunningBuild currentBuild = myCurrentBuild.getCurrentBuild();
    final BuildProgressLogger logger = currentBuild.getBuildLogger();

    if (!myOutputFile.exists()) {
      logger.error("Output xml from fxcop not found");
      logger.buildFailureDescription("FxCop failed");
    }

    try {
      ImportInspectionResults();
      GenerateHtmlReport();
    } catch (Exception e) {
      logger.error("Exception while importing fxcop results: " + e);
      logger.buildFailureDescription("FxCop results import error");
    }

    final File outputDir = new File(currentBuild.getWorkingDirectory(),
                                    FxCopConstants.OUTPUT_DIR);

    if (outputDir.listFiles(new ExtensionFilenameFilter(".xml")).length > 0) {
      myArtifactsWatcher.addNewArtifactsPath(FxCopConstants.OUTPUT_DIR + "/*.xml");
    }

    if (outputDir.listFiles(new ExtensionFilenameFilter(".html")).length > 0) {
      myArtifactsWatcher.addNewArtifactsPath(FxCopConstants.OUTPUT_DIR + "/*.html");
    }
  }

  private void ImportInspectionResults() throws Exception {
    final AgentRunningBuild currentBuild = myCurrentBuild.getCurrentBuild();
    final BuildProgressLogger logger = currentBuild.getBuildLogger();

    logger.progressMessage("Importing inspection results");
    logger.flush();

    myDataProcessor.processData(myOutputFile, new HashMap<String, String>());
  }

  private void GenerateHtmlReport() throws TransformerException {
    final AgentRunningBuild currentBuild = myCurrentBuild.getCurrentBuild();
    final BuildProgressLogger logger = currentBuild.getBuildLogger();

    final String fxcopReportXslt = currentBuild.getRunnerParameters().get(FxCopConstants.SETTINGS_REPORT_XSLT);
    if (StringUtil.isEmptyOrSpaces(fxcopReportXslt)) {
      logger.message("Skipped html report generation since not requested");
      return;
    }

    final File xsltFile = new File(fxcopReportXslt);
    if (!xsltFile.exists()) {
      logger.warning(xsltFile.getAbsolutePath() + " not found => won't generate html report");
      return;
    }

    logger.progressMessage("Generating HTML report");
    logger.flush();

    final File reportFile = new File(myOutputDir, FxCopConstants.REPORT_FILE);

    Source xmlSource = new StreamSource(myOutputFile);
    Source xsltSource = new StreamSource(xsltFile);

    TransformerFactory transformerFactory =
      TransformerFactory.newInstance();
    Transformer trans = transformerFactory.newTransformer(xsltSource);

    trans.transform(xmlSource, new StreamResult(reportFile));
  }

  // debug
  private void logInspectionMessages(final BuildProgressLogger logger) throws Exception {
    final InspectionReporter reporter = new InspectionReporter() {
      public void reportInspection(@NotNull final InspectionInstance inspection) {
        logger.message(inspection.toString() + "\n");
      }

      public void reportInspectionType(@NotNull final InspectionTypeInfo inspectionType) {
        logger.message(inspectionType.toString() + "\n");
      }

      public void flush() {
      }
    };
    new FxCopDataProcessor(myCurrentBuild, reporter).processData(myOutputFile, new HashMap<String, String>());
  }
}
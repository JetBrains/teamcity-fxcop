package jetbrains.buildServer.fxcop.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.CurrentBuildTracker;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.inspections.InspectionInstance;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.inspections.InspectionTypeInfo;
import jetbrains.buildServer.agent.runner.GenericProgramRunner;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 28.08.2008
 * Time: 18:47:13
 */
public class FxCopRunner extends GenericProgramRunner {
  private final ArtifactsWatcher myArtifactsWatcher;
  private final CurrentBuildTracker myCurrentBuild;
  private File myOutputFile = null;
  private File myOutputDir = null;
  private final FxCopInspectionsProcessor myInspectionsProcessor;
  private final FxCopCommandLineBuilder myCommandLineBuilder;

  public FxCopRunner(@NotNull final ArtifactsWatcher artifactsWatcher,
                     @NotNull final CurrentBuildTracker currentBuild,
                     @NotNull final FxCopInspectionsProcessor inspectionsProcessor,
                     @NotNull final FxCopCommandLineBuilder commandLineBuilder) {
    myArtifactsWatcher = artifactsWatcher;
    myCurrentBuild = currentBuild;
    myInspectionsProcessor = inspectionsProcessor;
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
  protected void processTerminated(RunEnvironment runEnvironment, boolean badExitCode) {
    if (badExitCode) {
      return;
    }

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

    myArtifactsWatcher.addNewArtifactsPath(FxCopConstants.OUTPUT_DIR + "/*.xml");
    myArtifactsWatcher.addNewArtifactsPath(FxCopConstants.OUTPUT_DIR + "/*.html");    
  }

  private void ImportInspectionResults() throws Exception {
    final AgentRunningBuild currentBuild = myCurrentBuild.getCurrentBuild();
    final BuildProgressLogger logger = currentBuild.getBuildLogger();

    logger.progressMessage("Importing inspection results");
    logger.flush();

    myInspectionsProcessor.processData(myOutputFile, new HashMap<String, String>());
  }

  private void GenerateHtmlReport() throws TransformerException {
    final AgentRunningBuild currentBuild = myCurrentBuild.getCurrentBuild();
    final BuildProgressLogger logger = currentBuild.getBuildLogger();

    final String fxcopReportXslt = currentBuild.getRunParameters().get(FxCopConstants.SETTINGS_REPORT_XSLT);
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
    new FxCopInspectionsProcessor(myCurrentBuild, reporter).processData(myOutputFile, new HashMap<String, String>());
  }
}
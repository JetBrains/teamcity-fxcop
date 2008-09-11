package jetbrains.buildServer.fxcop.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.CurrentBuildTracker;
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
  private final CurrentBuildTracker myCurrentBuild;
  private File myOutputFile = null;
  private final FxCopInspectionsProcessor myInspectionsProcessor;
  private final FxCopCommandLineBuilder myCommandLineBuilder;

  public FxCopRunner(@NotNull final CurrentBuildTracker currentBuild,
                     @NotNull final FxCopInspectionsProcessor inspectionsProcessor,
                     @NotNull final FxCopCommandLineBuilder commandLineBuilder) {
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

    myOutputFile = myCommandLineBuilder.buildCommandLine(cmd, runParameters);

    final BuildProgressLogger logger = myCurrentBuild.getCurrentBuild().getBuildLogger();
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
      logger.progressMessage("Importing inspection results");
      logger.flush();

      logInspectionMessages(logger);

      myInspectionsProcessor.processData(myOutputFile, new HashMap<String, String>());
    } catch (Exception e) {
      logger.error("Exception while importing fxcop results: " + e);
      logger.buildFailureDescription("FxCop results import error");
    }
  }

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
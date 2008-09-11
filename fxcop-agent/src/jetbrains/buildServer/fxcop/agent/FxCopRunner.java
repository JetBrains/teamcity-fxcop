package jetbrains.buildServer.fxcop.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.HashMap;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.CurrentBuildTracker;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.inspections.InspectionInstance;
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

  public FxCopRunner(@NotNull CurrentBuildTracker currentBuild, FxCopInspectionsProcessor inspectionsProcessor) {
    myCurrentBuild = currentBuild;
    myInspectionsProcessor = inspectionsProcessor;
  }

  public String getType() {
    return FxCopConstants.RUNNER_TYPE;
  }

  //@Override
  public boolean canRun(final BuildAgentConfiguration agentConfig) {
    return true;
  }

  protected void buildCommandLine(
    final GeneralCommandLine cmd, final File workingDir,
    final Map<String, String> runParameters,
    final Map<String, String> buildParameters) throws IOException, RunBuildException {

    final BuildProgressLogger logger = myCurrentBuild.getCurrentBuild().getBuildLogger();

    final String fxcopRoot = runParameters.get(FxCopConstants.SETTINGS_FXCOP_ROOT);
    if (StringUtil.isEmpty(fxcopRoot)) {
      throw new RunBuildException("FxCop root not specified in build settings");
    }

    final String fxCopCmd = fxcopRoot + File.separator + FxCopConstants.FXCOPCMD_BINARY;
    final File fxCopCmdFile = new File(fxCopCmd);
    if (!fxCopCmdFile.exists()) {
      throw new RunBuildException("File not found: " + fxCopCmd);
    }

    cmd.setExePath(fxCopCmdFile.getAbsolutePath());

    // Additional options
    final String additionalOptions = runParameters.get(FxCopConstants.SETTINGS_ADDITIONAL_OPTIONS);
    if (additionalOptions != null) {
      StringTokenizer tokenizer = new StringTokenizer(additionalOptions);
      while (tokenizer.hasMoreTokens()) {
        cmd.addParameter(tokenizer.nextToken());
      }
    }

    // Files to be processed
    final String files = runParameters.get(FxCopConstants.SETTINGS_FILES);
    if (files != null) {
      StringTokenizer tokenizer = new StringTokenizer(files);
      while (tokenizer.hasMoreTokens()) {
        cmd.addParameter("/f:" + tokenizer.nextToken());
      }
    }

    // Output file
    myOutputFile = File.createTempFile("fxcop-runner-output-", ".xml");
    myOutputFile.delete();
    cmd.addParameter("/out:" + myOutputFile.getAbsolutePath());

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
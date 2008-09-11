package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.Map;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.CurrentBuildTracker;
import jetbrains.buildServer.agent.DataProcessor;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.fxcop.agent.loggers.BuildLogSimpleLogger;
import jetbrains.buildServer.fxcop.agent.loggers.SimpleLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 15.08.2008
 * Time: 20:11:11
 */
public class FxCopInspectionsProcessor implements DataProcessor {
  private final CurrentBuildTracker myCurrentBuild;
  private final InspectionReporter myReporter;

  public FxCopInspectionsProcessor(final CurrentBuildTracker currentBuild,
                                   final InspectionReporter reporter) {
    myCurrentBuild = currentBuild;
    myReporter = reporter;
  }

  public void processData(@NotNull final File file, final Map<String, String> arguments) throws Exception {
    final AgentRunningBuild currentBuild = myCurrentBuild.getCurrentBuild();
    final String workingRoot = currentBuild.getWorkingDirectory().toString();

    final SimpleLogger logger = new BuildLogSimpleLogger(currentBuild.getBuildLogger());
    final FxCopFileProcessor fileProcessor =
      new FxCopFileProcessor(file, workingRoot, logger, myReporter);

    fileProcessor.processReport();
  }

  @NotNull
  public String getId() {
    return "FxCop";
  }
}

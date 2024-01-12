

package jetbrains.buildServer.fxcop.agent;

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.DataProcessor;
import jetbrains.buildServer.agent.DataProcessorContext;
import jetbrains.buildServer.agent.SimpleBuildLogger;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import org.jetbrains.annotations.NotNull;

public class FxCopDataProcessor implements DataProcessor {
  private final InspectionReporter myReporter;

  public FxCopDataProcessor(@NotNull final InspectionReporter reporter) {
    myReporter = reporter;
  }

  public void processData(@NotNull final DataProcessorContext context) throws Exception {
    final AgentRunningBuild currentBuild = context.getBuild();

    final String checkoutDir = currentBuild.getCheckoutDirectory().toString();

    final SimpleBuildLogger logger = currentBuild.getBuildLogger();
    final FxCopFileProcessor fileProcessor =
      new FxCopFileProcessor(context.getFile(), checkoutDir, logger, myReporter);

    myReporter.markBuildAsInspectionsBuild();
    fileProcessor.processReport();
  }

  @NotNull
  public String getType() {
    return "FxCop";
  }
}
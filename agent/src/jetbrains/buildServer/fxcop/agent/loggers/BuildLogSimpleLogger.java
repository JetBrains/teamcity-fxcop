package jetbrains.buildServer.fxcop.agent.loggers;

import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 15.08.2008
 * Time: 20:21:37
 */
public class BuildLogSimpleLogger implements SimpleLogger {
  private BuildProgressLogger myBuildLog;

  public BuildLogSimpleLogger(final BuildProgressLogger buildLog) {
    myBuildLog = buildLog;
  }

  public void info(@NotNull final String message) {
    myBuildLog.message(message);
  }

  public void warning(@NotNull final String message) {
    myBuildLog.warning(message);
  }

  public void error(@NotNull final String message) {
    myBuildLog.error(message);
  }
}

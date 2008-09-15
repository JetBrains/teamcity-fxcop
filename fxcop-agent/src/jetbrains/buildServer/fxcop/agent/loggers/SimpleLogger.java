package jetbrains.buildServer.fxcop.agent.loggers;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 15.08.2008
 * Time: 20:20:18
 */
public interface SimpleLogger {
  void info(@NotNull final String message);

  void warning(@NotNull final String message);

  void error(@NotNull final String message);
}

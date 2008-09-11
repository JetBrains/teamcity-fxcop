package jetbrains.buildServer.fxcop.agent.loggers;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 15.08.2008
 * Time: 20:22:59
 */
public class SimpleLoggerMock implements SimpleLogger {
  private final StringBuilder myText = new StringBuilder();

  public void info(@NotNull final String message) {
    myText.append("INFO: ");
    myText.append(message);
    myText.append("\n");
  }

  public void warning(@NotNull final String message) {
    myText.append("WARNING: ");
    myText.append(message);
    myText.append("\n");
  }

  public void error(@NotNull final String message) {
    myText.append("ERROR: ");
    myText.append(message);
    myText.append("\n");
  }

  public String getText() {
    return myText.toString();
  }
}

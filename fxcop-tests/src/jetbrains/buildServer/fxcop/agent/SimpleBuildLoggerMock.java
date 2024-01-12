

package jetbrains.buildServer.fxcop.agent;

import jetbrains.buildServer.agent.SimpleBuildLogger;
import jetbrains.buildServer.messages.Status;
import org.jetbrains.annotations.NotNull;

public class SimpleBuildLoggerMock implements SimpleBuildLogger {
  private final StringBuilder myText;

  public SimpleBuildLoggerMock(final StringBuilder text) {
    myText = text;
  }

  public void warning(@NotNull final String message) {
    myText.append("WARNING: ");
    myText.append(message);
    myText.append("\n");
  }

  public void exception(final Throwable th) {
    myText.append("EXCEPTION: ");
    myText.append(th.toString());
    myText.append("\n");
  }

  public void progressMessage(final String message) {
    myText.append("PROGRESS: ");
    myText.append(message);
    myText.append("\n");
  }

  public void message(final String message) {
    myText.append("MESSAGE: ");
    myText.append(message);
    myText.append("\n");
  }

  @Override
  public void debug(final String message) {
    myText.append("DEBUG: ");
    myText.append(message);
    myText.append("\n");
  }

  @Override
  public void message(final String message, final Status status) {
    message(message);
  }

  public void error(@NotNull final String message) {
    myText.append("ERROR: ");
    myText.append(message);
    myText.append("\n");
  }

  public StringBuilder getText() {
    return myText;
  }
}
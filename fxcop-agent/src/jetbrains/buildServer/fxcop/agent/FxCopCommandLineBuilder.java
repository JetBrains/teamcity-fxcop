package jetbrains.buildServer.fxcop.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import java.io.IOException;
import java.util.Map;
import jetbrains.buildServer.RunBuildException;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 11.09.2008
 * Time: 16:03:58
 */
public interface FxCopCommandLineBuilder {
  void buildCommandLine(
    GeneralCommandLine cmd, Map<String, String> runParameters) throws IOException, RunBuildException;
}

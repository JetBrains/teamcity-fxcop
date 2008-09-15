package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.RunBuildException;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 11.09.2008
 * Time: 16:03:58
 */
public interface FxCopCommandLineBuilder {
  // returns output file
  File buildCommandLine(
    GeneralCommandLine cmd, Map<String, String> runParameters) throws IOException, RunBuildException;
}

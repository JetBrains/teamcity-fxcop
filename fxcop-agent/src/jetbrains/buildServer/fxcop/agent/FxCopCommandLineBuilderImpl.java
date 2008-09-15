package jetbrains.buildServer.fxcop.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.text.StringUtil;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.fxcop.common.FxCopConstants;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 11.09.2008
 * Time: 15:59:45
 */
public class FxCopCommandLineBuilderImpl implements FxCopCommandLineBuilder {
  public File buildCommandLine(
    final GeneralCommandLine cmd, final Map<String, String> runParameters) throws IOException, RunBuildException {

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
    File outputFile = File.createTempFile("fxcop-runner-output-", ".xml");
    cmd.addParameter("/out:" + outputFile.getAbsolutePath());

    return outputFile;
  }
}

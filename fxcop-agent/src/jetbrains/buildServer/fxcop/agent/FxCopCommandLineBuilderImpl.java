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
  public void buildCommandLine(
    final GeneralCommandLine cmd, final Map<String, String> runParameters) throws IOException, RunBuildException {

    final String fxcopRoot = runParameters.get(FxCopConstants.SETTINGS_FXCOP_ROOT);
    if (StringUtil.isEmpty(fxcopRoot)) {
      throw new RunBuildException("FxCop root not specified in build settings");
    }

    final String fxCopCmd = fxcopRoot + File.separator + FxCopConstants.FXCOPCMD_BINARY;
    final File fxCopCmdFile = new File(fxCopCmd);

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
    final String what = runParameters.get(FxCopConstants.SETTINGS_WHAT_TO_INSPECT);
    if (FxCopConstants.WHAT_TO_INSPECT_PROJECT.equals(what)) {
      final String project = runParameters.get(FxCopConstants.SETTINGS_PROJECT);
      if (project != null) {
        cmd.addParameter("/project:" + project);
      }
    } else if (FxCopConstants.WHAT_TO_INSPECT_FILES.equals(what)) {
      final String files = runParameters.get(FxCopConstants.SETTINGS_FILES);
      if (files != null) {
        StringTokenizer tokenizer = new StringTokenizer(files);
        while (tokenizer.hasMoreTokens()) {
          cmd.addParameter("/f:" + tokenizer.nextToken());
        }
      }
    } else {
      throw new RunBuildException("Unknown target to inspect: " + what);
    }

    // Output file
    cmd.addParameter("/out:" + FxCopConstants.OUTPUT_DIR +
                     File.separator + FxCopConstants.OUTPUT_FILE);
  }
}

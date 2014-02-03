package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.util.PEReader.PEUtil;
import jetbrains.buildServer.util.PEReader.PEVersion;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.Win32RegistryAccessor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class FxCopSearcher {

  @NotNull
  private static final Logger LOG = Logger.getLogger(FxCopSearcher.class);

  @NotNull
  static final String VS_2010_PATH = "VS2010_Path";

  @NotNull
  public static final String FXCOP_RELATIVE_PATH = "..\\..\\Team Tools\\Static Analysis Tools\\FxCop\\";

  @NotNull
  public static final String FXCOP_EXE_RELATIVE_PATH = FXCOP_RELATIVE_PATH + FxCopConstants.FXCOPCMD_BINARY;



  @NotNull
  final Win32RegistryAccessor myAccessor;

  public FxCopSearcher(@NotNull final Win32RegistryAccessor accessor) {
    this.myAccessor = accessor;
  }

  public void search(@NotNull final BuildAgentConfiguration config) {
    //TODO: introduce .net properties searcher in open api and use it here
    if (!config.getSystemInfo().isWindows()) {
      return;
    }
    // search config params
    String fxCopRoot = config.getBuildParameters().getAllParameters().get(FxCopConstants.FXCOP_ROOT_PROPERTY);
    if (StringUtil.isEmptyOrSpaces(fxCopRoot)) {
      fxCopRoot = searchFxCopInWinRegistry();
    }
    if (StringUtil.isEmptyOrSpaces(fxCopRoot)) {
      try {
        fxCopRoot = searchFxCopInVS2010Installation(config);
      } catch (IOException e) {
        LOG.warn("Error while searching for FxCop: " + e.toString());
        LOG.debug("Error while searching for FxCop", e);
      }
    }

    if (StringUtil.isNotEmpty(fxCopRoot)) {
      setupEnvironment(config, fxCopRoot);
    }
  }


  private void setupEnvironment(final BuildAgentConfiguration config, final String fxcopRoot) {
    final File fxcopCmd = new File(fxcopRoot, FxCopConstants.FXCOPCMD_BINARY);
    PEVersion fileVersion = PEUtil.getFileVersion(fxcopCmd);
    if (fileVersion != null) {
      config.addCustomProperty(FxCopConstants.FXCOPCMD_FILE_VERSION_PROPERTY, fileVersion.toString());
      LOG.info("Found FxCopCmd file version: " + fileVersion);
    }
    config.addCustomProperty(FxCopConstants.FXCOP_ROOT_PROPERTY, fxcopRoot);
  }

  @Nullable
  private String searchFxCopInWinRegistry() {
    // Use .fxcop file association

    final String fxcopClass =
      myAccessor.readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, ".fxcop", "");
    if (fxcopClass == null) {
      LOG.info(".fxcop file association wasn't found in CLASSES_ROOT");
      return null;
    }

    LOG.info("Found FxCop class in CLASSES_ROOT: " + fxcopClass);

    final String fxcopStartCmd = myAccessor
      .readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, fxcopClass + "\\shell\\open\\command", "");
    if (fxcopStartCmd == null) {
      return null;
    }

    LOG.info("Found FxCopCmd start cmd: " + fxcopStartCmd);

    final String fxcopBinary = extractFxCopBinary(fxcopStartCmd);
    if (fxcopBinary == null) {
      LOG.warn("Can't extract fxcop binary from: " + fxcopStartCmd);
      return null;
    }

    LOG.info("Found FxCopCmd binary: " + fxcopBinary);

    final File fxcopBinaryFile = new File(fxcopBinary);
    if (!fxcopBinaryFile.exists()) {
      LOG.warn("FxCopCmd was found in the registry but it does not exist on disk at path \"" + fxcopBinaryFile + "\"");
      return null;
    }

    final String fxcopRoot = fxcopBinaryFile.getParent();
    LOG.info("Found FxCop root: \"" + fxcopRoot + "\"");

    return fxcopRoot;
  }

  private String extractFxCopBinary(@NotNull final String fxcopStartCmd) {
    String bin = fxcopStartCmd;
    if (bin.startsWith("\"")) {
      bin = bin.substring(1);
    }

    if (bin.endsWith("\" \"%1\"")) {
      bin = bin.substring(0, bin.length() - 6);
    }

    return bin;
  }

  @Nullable
  private String searchFxCopInVS2010Installation(@NotNull final BuildAgentConfiguration config) throws IOException {
    final Map<String,String> configurationParameters = config.getConfigurationParameters();
    if(!configurationParameters.containsKey(VS_2010_PATH)){
      LOG.info("VS2010_Path configuration parameter was not found");
      return null;
    }
    final String vs2010Path =  configurationParameters.get(VS_2010_PATH);
    if(vs2010Path == null || StringUtil.isEmptyOrSpaces(vs2010Path)) {
      LOG.info("VS2010_Path configuration parameter value is empty");
      return null;
    }
    final File devenvExeHome = new File(vs2010Path);
    if(!devenvExeHome.exists()){
      LOG.warn("VS2010 home directory was found in the agent configuration but it does not exist on disk at path: \"" + devenvExeHome.getAbsolutePath() + "\"");
      return null;
    }
    final File fxCopExe = new File(devenvExeHome, FXCOP_EXE_RELATIVE_PATH);
    if(!fxCopExe.exists()){
      LOG.info("FxCopCmd.exe was not found in VS2010 installation directory at path: \"" + fxCopExe.getAbsolutePath() + "\"");
      return null;
    }
    LOG.info("FxCopCmd.exe found at path \"" + fxCopExe.getAbsolutePath() + "\"");
    return fxCopExe.getParentFile().getCanonicalPath();
  }

}

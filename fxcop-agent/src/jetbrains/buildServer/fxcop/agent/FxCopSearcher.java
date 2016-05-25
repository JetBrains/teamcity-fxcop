/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
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

  public static final String VS_2010_PATH = "VS2010_Path";
  private static final Collection<String> KNOWN_VS_PATH_PARAM_NAMES = Arrays.asList("VS2015_Path", "VS2013_Path", VS_2010_PATH);

  @NotNull
  public static final String FXCOP_RELATIVE_PATH = "..\\..\\Team Tools\\Static Analysis Tools\\FxCop\\";

  @NotNull
  public static final String FXCOP_EXE_RELATIVE_PATH = FXCOP_RELATIVE_PATH + FxCopConstants.FXCOPCMD_BINARY;

  private static final String FXCOP_PROJECT_FILE_EXT = ".fxcop";

  @NotNull
  final Win32RegistryAccessor myRegistryAccessor;

  public FxCopSearcher(@NotNull final Win32RegistryAccessor registryAccessor) {
    myRegistryAccessor = registryAccessor;
  }

  public void search(@NotNull final BuildAgentConfiguration config) {
    //TODO: introduce .net properties searcher in open api and use it here
    if (!config.getSystemInfo().isWindows()) return;

    // search config params
    String fxCopRoot = config.getBuildParameters().getAllParameters().get(FxCopConstants.FXCOP_ROOT_PROPERTY);
    if (StringUtil.isEmptyOrSpaces(fxCopRoot)) {
      fxCopRoot = searchFxCopInWinRegistry();
    }

    if(StringUtil.isEmptyOrSpaces(fxCopRoot)){
      final Map<String, String> configurationParameters = config.getConfigurationParameters();
      for (String paramName : KNOWN_VS_PATH_PARAM_NAMES){
        if(!StringUtil.isEmptyOrSpaces(fxCopRoot)) continue;
        try {
          fxCopRoot = searchFxCopInVSInstallation(configurationParameters, paramName);
        } catch (IOException e) {
          LOG.warn("Error while searching for FxCop: " + e.toString());
          LOG.debug("Error while searching for FxCop", e);
        }
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
      myRegistryAccessor.readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, FXCOP_PROJECT_FILE_EXT, "");
    if (fxcopClass == null) {
      LOG.info(".fxcop file association wasn't found in CLASSES_ROOT");
      return null;
    }

    LOG.info("Found FxCop class in CLASSES_ROOT: " + fxcopClass);

    final String fxcopStartCmd = myRegistryAccessor
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
  private String searchFxCopInVSInstallation(@NotNull final Map<String,String> configurationParameters, @NotNull final String vsPathParamName) throws IOException {
    if(!configurationParameters.containsKey(vsPathParamName)){
      LOG.info(vsPathParamName + " configuration parameter was not found");
      return null;
    }
    final String vsPath =  configurationParameters.get(vsPathParamName);
    if(vsPath == null || StringUtil.isEmptyOrSpaces(vsPath)) {
      LOG.info(vsPathParamName + " configuration parameter value is empty");
      return null;
    }
    final File devenvExeHome = new File(vsPath);
    if(!devenvExeHome.exists()){
      LOG.warn("VS home directory was found in the agent configuration but it does not exist on disk at path: \"" + devenvExeHome.getAbsolutePath() + "\"");
      return null;
    }
    final File fxCopExe = new File(devenvExeHome, FXCOP_EXE_RELATIVE_PATH);
    if(!fxCopExe.exists()){
      LOG.info("FxCopCmd.exe was not found in VS installation directory at path: \"" + fxCopExe.getAbsolutePath() + "\"");
      return null;
    }
    LOG.info("FxCopCmd.exe found at path \"" + fxCopExe.getAbsolutePath() + "\"");
    return fxCopExe.getParentFile().getCanonicalPath();
  }

}

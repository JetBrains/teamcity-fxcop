/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.PEReader.PEUtil;
import jetbrains.buildServer.util.PEReader.PEVersion;
import jetbrains.buildServer.util.Win32RegistryAccessor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FxCopPropertiesExtension extends AgentLifeCycleAdapter {
  private static final Logger LOG = Logger.getLogger(FxCopPropertiesExtension.class);

  private final Win32RegistryAccessor myAccessor;

  public FxCopPropertiesExtension(@NotNull final EventDispatcher<AgentLifeCycleListener> events, final Win32RegistryAccessor accessor) {
    myAccessor = accessor;
    events.addListener(this);
  }

  @Override
  public void beforeAgentConfigurationLoaded(@NotNull final BuildAgent agent) {
    final BuildAgentConfiguration config = agent.getConfiguration();
    
    if (!config.getSystemInfo().isWindows()) {
      return;
    }

    // Try to detect FxCop from windows registry
    final String fxcopRoot = searchFxCopInClassesRoot();

    if (fxcopRoot != null) {
      final File fxcopCmd = new File(fxcopRoot, FxCopConstants.FXCOPCMD_BINARY);
      PEVersion fileVersion = PEUtil.getFileVersion(fxcopCmd);
      if (fileVersion != null) {
        config.addCustomProperty(FxCopConstants.FXCOPCMD_FILE_VERSION_PROPERTY, fileVersion.toString());
        LOG.info("Found FxCopCmd file version: " + fileVersion);
      }

      config.addCustomProperty(FxCopConstants.FXCOP_ROOT_PROPERTY, fxcopRoot);
    }
  }

  @Nullable
  private String searchFxCopInClassesRoot() {
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
      LOG.warn("FxCopCmd was found in the registry but non-existent on disk");
      return null;
    }

    final String fxcopRoot = fxcopBinaryFile.getParent();
    LOG.info("Found FxCop root: " + fxcopRoot);

    return fxcopRoot;
  }

  private String extractFxCopBinary(final String fxcopStartCmd) {
    String bin = fxcopStartCmd;
    if (bin.startsWith("\"")) {
      bin = bin.substring(1);
    }

    if (bin.endsWith("\" \"%1\"")) {
      bin = bin.substring(0, bin.length() - 6);
    }

    return bin;
  }
}

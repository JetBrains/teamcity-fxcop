/*
 * Copyright (c) 2008, JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.fxcop.agent;

import com.jniwrapper.win32.registry.RegistryException;
import com.jniwrapper.win32.registry.RegistryKey;
import java.io.File;
import java.util.Map;
import jetbrains.buildServer.agent.AgentPropertiesExtension;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class FxCopPropertiesExtension extends AgentPropertiesExtension {
  private static final Logger LOG = Logger.getLogger(FxCopPropertiesExtension.class);

  public FxCopPropertiesExtension() {
  }

  private static String readRegistryText(RegistryKey rootKey, final String subKey, final String value) {
    if (rootKey == null || subKey == null) {
      return null;
    }

    try {
      try {
        RegistryKey registryKey = rootKey.openSubKey(subKey);
        if (registryKey != null) {
          try {
            return (String)registryKey.values().get(value);
          } finally {
            registryKey.close();
          }
        }
      } catch (RegistryException e) {
        //
      }
    } catch (Error e) {
      //
    }
    return null;
  }

  public void appendAfterUserProperties(@NotNull BuildAgentConfiguration config) {
    if (!config.getSystemInfo().isWindows()) {
      return;
    }

    // Try to detect FxCop from windows registry
    final String fxcopRoot = searchFxCopInClassesRoot();

    if (fxcopRoot != null) {
      addRootProperty(config, fxcopRoot);
    }
  }

  private String searchFxCopInClassesRoot() {
    // Use .fxcop file association

    final String fxcopClass = readRegistryText(RegistryKey.CLASSES_ROOT, ".fxcop", "");
    if (fxcopClass == null) {
      LOG.info(".fxcop file association wasn't found in CLASSES_ROOT");
      return null;
    }

    LOG.info("Found FxCop class in CLASSES_ROOT: " + fxcopClass);

    final String fxcopStartCmd = readRegistryText(RegistryKey.CLASSES_ROOT, fxcopClass + "\\shell\\open\\command", "");
    if (fxcopStartCmd == null) {
      return null;
    }

    LOG.info("Found FxCopCmd start cmd: " + fxcopStartCmd);

    final String fxcopBinary = extractFxCopBinary(fxcopStartCmd);
    if (fxcopBinary == null) {
      LOG.error("Can't extract fxcop binary from: " + fxcopStartCmd);
      return null;
    }

    LOG.info("Found FxCopCmd binary: " + fxcopBinary);

    final File fxcopBinaryFile = new File(fxcopBinary);
    if (!fxcopBinaryFile.exists()) {
      LOG.error("FxCopCmd was found in the registry but non-existent on disk");
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

  private void addRootProperty(BuildAgentConfiguration config, String fxcopRoot) {
    final Map customProperties = config.getCustomProperties();
    final String propertyName = FxCopConstants.FXCOP_ROOT_PROPERTY;
    if (!customProperties.containsKey(propertyName)) {
      config.addCustomProperty(propertyName, fxcopRoot);
    }
  }
}

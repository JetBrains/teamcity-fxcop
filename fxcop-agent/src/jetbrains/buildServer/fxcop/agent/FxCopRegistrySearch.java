/*
 * Copyright 2000-2022 JetBrains s.r.o.
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
import java.util.Collection;
import java.util.Collections;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.util.Win32RegistryAccessor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class FxCopRegistrySearch implements FxCopSearch {

  private static final Logger LOG = Logger.getLogger(FxCopRegistrySearch.class);
  private static final String FXCOP_PROJECT_FILE_EXT = ".fxcop";
  private final Win32RegistryAccessor myRegistryAccessor;

  FxCopRegistrySearch(@NotNull final Win32RegistryAccessor registryAccessor) {
    myRegistryAccessor = registryAccessor;
  }

  @NotNull
  @Override
  public Collection<File> getHintPaths(@NotNull final BuildAgentConfiguration config) {
    // Use .fxcop file association
    final String fxcopClass = myRegistryAccessor.readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, FXCOP_PROJECT_FILE_EXT, "");
    if (fxcopClass == null) {
      LOG.debug(".fxcop file association wasn't found in CLASSES_ROOT");
      return Collections.emptyList();
    }

    LOG.info("Found FxCop class in CLASSES_ROOT: " + fxcopClass);
    final String fxcopStartCmd = myRegistryAccessor.readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, fxcopClass + "\\shell\\open\\command", "");
    if (fxcopStartCmd == null) {
      return Collections.emptyList();
    }

    final String fxcopBinary = extractFxCopBinary(fxcopStartCmd);
    if (fxcopBinary == null) {
      LOG.warn("Can't extract fxcop binary from: " + fxcopStartCmd);
      return Collections.emptyList();
    }

    LOG.info("Found FxCopCmd start cmd: " + fxcopStartCmd);
    return Collections.singleton(new File(fxcopBinary));
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
}

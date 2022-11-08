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
import java.util.*;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.config.AgentParametersSupplier;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.PEReader.PEUtil;
import jetbrains.buildServer.util.PEReader.PEVersion;
import jetbrains.buildServer.util.Win32RegistryAccessor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class FxCopSearcher {

  private static final Logger LOG = Logger.getLogger(FxCopSearcher.class);
  private final List<FxCopSearch> mySearches;
  @NotNull private final BuildAgentConfiguration myBuildAgentConfiguration;

  public FxCopSearcher(@NotNull final Win32RegistryAccessor registryAccessor,
                       @NotNull BuildAgentConfiguration buildAgentConfiguration,
                       @NotNull ExtensionHolder extensionHolder) {
    AgentParametersSupplier dotNetParametersSupplier = extensionHolder.getExtension(AgentParametersSupplier.class, FxCopConstants.DOTNET_SUPPLIER_NAME);

    mySearches = Arrays.asList(
      new FxCopAgentConfigSearch(),
      new FxCopRegistrySearch(registryAccessor),
      new FxCopVisualStudioSearch(dotNetParametersSupplier),
      new FxCopMsBuildSearch(dotNetParametersSupplier)
    );
    myBuildAgentConfiguration = buildAgentConfiguration;
  }

  public Map<String, String> search() {
    //TODO: introduce .net properties searcher in open api and use it here
    if (!myBuildAgentConfiguration.getSystemInfo().isWindows()) return Collections.emptyMap();

    for (FxCopSearch search : mySearches) {
      for (File fxCopExe : search.getHintPaths(myBuildAgentConfiguration)) {
        if (!fxCopExe.exists()) {
          continue;
        }

        final PEVersion fileVersion = PEUtil.getFileVersion(fxCopExe);
        if (fileVersion == null) {
          LOG.warn(String.format("Unable to get FxCop version located at \"%s\"", fxCopExe));
          continue;
        }

        final String fxcopRoot = FileUtil.getCanonicalFile(fxCopExe).getParent();
        final String version = fileVersion.toString();

        LOG.info(String.format("Found FxCop %s in \"%s\"", version, fxcopRoot));
        final Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put(FxCopConstants.FXCOP_ROOT_NAME, fxcopRoot);
        systemProperties.put(FxCopConstants.FXCOPCMD_FILE_VERSION_NAME, fileVersion.toString());

        return systemProperties;
      }
    }
    return Collections.emptyMap();
  }
}



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
  @NotNull private final ExtensionHolder myExtensionHolder;

  public FxCopSearcher(@NotNull final Win32RegistryAccessor registryAccessor,
                       @NotNull BuildAgentConfiguration buildAgentConfiguration,
                       @NotNull ExtensionHolder extensionHolder) {

    mySearches = Arrays.asList(
      new FxCopAgentConfigSearch(),
      new FxCopRegistrySearch(registryAccessor),
      new FxCopVisualStudioSearch(),
      new FxCopMsBuildSearch()
    );
    myBuildAgentConfiguration = buildAgentConfiguration;
    myExtensionHolder = extensionHolder;
  }

  public Map<String, String> search() {
    //TODO: introduce .net properties searcher in open api and use it here
    if (!myBuildAgentConfiguration.getSystemInfo().isWindows()) return Collections.emptyMap();

    AgentParametersSupplier dotNetParametersSupplier = myExtensionHolder.getExtension(AgentParametersSupplier.class, FxCopConstants.DOTNET_SUPPLIER_NAME);

    if (dotNetParametersSupplier == null){
      LOG.warn("Failed to find dotNet's parameter supplier. Will not be able to detect FxCop from Visual Studio or MsBuild");
    }

    for (FxCopSearch search : mySearches) {
      for (File fxCopExe : search.getHintPaths(myBuildAgentConfiguration, dotNetParametersSupplier)) {
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
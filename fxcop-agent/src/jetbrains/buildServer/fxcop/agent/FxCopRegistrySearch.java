

package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.config.AgentParametersSupplier;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.util.Win32RegistryAccessor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FxCopRegistrySearch implements FxCopSearch {

  private static final Logger LOG = Logger.getLogger(FxCopRegistrySearch.class);
  private static final String FXCOP_PROJECT_FILE_EXT = ".fxcop";
  private final Win32RegistryAccessor myRegistryAccessor;

  FxCopRegistrySearch(@NotNull final Win32RegistryAccessor registryAccessor) {
    myRegistryAccessor = registryAccessor;
  }

  @NotNull
  @Override
  public Collection<File> getHintPaths(@NotNull final BuildAgentConfiguration config, @Nullable AgentParametersSupplier dotNetParametersSupplier) {
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
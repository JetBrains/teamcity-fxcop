

package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.Collection;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.config.AgentParametersSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface FxCopSearch {
  @NotNull
  Collection<File> getHintPaths(@NotNull final BuildAgentConfiguration config, @Nullable AgentParametersSupplier dotNetParametersSupplier);
}
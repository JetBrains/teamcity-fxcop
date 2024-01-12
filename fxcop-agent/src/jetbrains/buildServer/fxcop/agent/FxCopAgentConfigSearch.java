

package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.config.AgentParametersSupplier;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides fxCop info based on system parameter `FxCopRoot`.
 */
public class FxCopAgentConfigSearch implements FxCopSearch {
  @NotNull
  @Override
  public Collection<File> getHintPaths(@NotNull final BuildAgentConfiguration config, @Nullable AgentParametersSupplier dotNetParametersSupplier) {
    final String fxCopRoot = config.getBuildParameters().getAllParameters().get(FxCopConstants.FXCOP_ROOT_PROPERTY);
    if (StringUtil.isEmpty(fxCopRoot)) {
      return Collections.emptyList();
    }

    return Collections.singleton(new File(fxCopRoot, FxCopConstants.FXCOPCMD_BINARY));
  }
}
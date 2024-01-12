

package jetbrains.buildServer.fxcop.agent;

import java.util.Map;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.config.AgentParametersSupplier;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

public class FxCopPropertiesExtension implements PositionAware, AgentParametersSupplier {

  @NotNull
  private final FxCopSearcher mySearcher;

  public FxCopPropertiesExtension(@NotNull final ExtensionHolder extensionHolder,
                                  @NotNull final FxCopSearcher searcher) {
    mySearcher = searcher;
    extensionHolder.registerExtension(AgentParametersSupplier.class, getClass().getName(), this);
  }

  @NotNull
  public String getOrderId() {
    return FxCopConstants.RUNNER_TYPE;
  }

  @NotNull
  public PositionConstraint getConstraint() {
    return PositionConstraint.last();
  }

  @Override
  public Map<String, String> getSystemProperties() {
    return mySearcher.search();
  }
}
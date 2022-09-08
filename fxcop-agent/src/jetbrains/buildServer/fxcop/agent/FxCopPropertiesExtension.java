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

import java.util.Map;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.config.AgentConfigurationAdapter;
import jetbrains.buildServer.agent.config.AgentConfigurationSnapshot;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

public class FxCopPropertiesExtension extends AgentConfigurationAdapter implements PositionAware {

  @NotNull
  private final FxCopSearcher mySearcher;

  public FxCopPropertiesExtension(@NotNull final ExtensionHolder extensionHolder,
                                  @NotNull final FxCopSearcher searcher) {
    mySearcher = searcher;
    extensionHolder.registerExtension(AgentConfigurationSnapshot.class, getClass().getName(), this);
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
  public void addSystemProperties(@NotNull Map<String, String> systemProperties) {
    mySearcher.search(systemProperties);
  }
}

/*
 * Copyright 2000-2019 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.positioning.PositionAware;
import jetbrains.buildServer.util.positioning.PositionConstraint;
import org.jetbrains.annotations.NotNull;

public class FxCopPropertiesExtension extends AgentLifeCycleAdapter implements PositionAware {

  @NotNull
  private final FxCopSearcher mySearcher;

  public FxCopPropertiesExtension(@NotNull final EventDispatcher<AgentLifeCycleListener> events,
                                  @NotNull final FxCopSearcher searcher) {
    mySearcher = searcher;
    events.addListener(this);
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
  public void agentInitialized(@NotNull final BuildAgent agent) {
    mySearcher.search(agent.getConfiguration());
  }
}

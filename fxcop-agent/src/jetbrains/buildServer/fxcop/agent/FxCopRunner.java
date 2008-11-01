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

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.runner.ProcessBuildRunner;
import jetbrains.buildServer.agent.runner.ProcessBuildRunnerState;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import org.jetbrains.annotations.NotNull;
import org.apache.log4j.Logger;

public class FxCopRunner implements ProcessBuildRunner {
  private static final Logger LOG = Logger.getLogger(FxCopRunner.class);

  private final ArtifactsWatcher myArtifactsWatcher;
  private final FxCopDataProcessor myDataProcessor;

  public FxCopRunner(@NotNull final ArtifactsWatcher artifactsWatcher,
                     @NotNull final FxCopDataProcessor dataProcessor) {
    myArtifactsWatcher = artifactsWatcher;
    myDataProcessor = dataProcessor;
  }

  @NotNull
  public String getType() {
    return FxCopConstants.RUNNER_TYPE;
  }

  public boolean canRun(@NotNull final BuildAgentConfiguration agentConfiguration) {
    if (!agentConfiguration.getSystemInfo().isWindows()) {
      LOG.info(getType() + " runner can works only under Windows");

      return false;
    }

    return true;
  }


  @NotNull
  public ProcessBuildRunnerState runBuild(@NotNull final AgentRunningBuild build) {
    return new FxCopRunnerState(build, myArtifactsWatcher, myDataProcessor);
  }
}
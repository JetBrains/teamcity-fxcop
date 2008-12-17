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

import java.io.File;
import java.util.Map;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.CurrentBuildTracker;
import jetbrains.buildServer.agent.DataProcessor;
import jetbrains.buildServer.agent.SimpleBuildLogger;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import org.jetbrains.annotations.NotNull;

public class FxCopDataProcessor implements DataProcessor {
  private final CurrentBuildTracker myCurrentBuild;
  private final InspectionReporter myReporter;

  public FxCopDataProcessor(final CurrentBuildTracker currentBuild,
                            final InspectionReporter reporter) {
    myCurrentBuild = currentBuild;
    myReporter = reporter;
  }

  public void processData(@NotNull final File path, final Map<String, String> arguments) throws Exception {
    final AgentRunningBuild currentBuild = myCurrentBuild.getCurrentBuild();
    final String workingRoot = currentBuild.getWorkingDirectory().toString();

    final SimpleBuildLogger logger = currentBuild.getBuildLogger();
    final FxCopFileProcessor fileProcessor =
      new FxCopFileProcessor(path, workingRoot, logger, myReporter);

    fileProcessor.processReport();
  }

  @NotNull
  public String getType() {
    return "FxCop";
  }
}

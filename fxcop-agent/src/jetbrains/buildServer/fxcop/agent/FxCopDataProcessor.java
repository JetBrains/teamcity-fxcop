/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.DataProcessor;
import jetbrains.buildServer.agent.DataProcessorContext;
import jetbrains.buildServer.agent.SimpleBuildLogger;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import org.jetbrains.annotations.NotNull;

public class FxCopDataProcessor implements DataProcessor {
  private final InspectionReporter myReporter;

  public FxCopDataProcessor(@NotNull final InspectionReporter reporter) {
    myReporter = reporter;
  }

  public void processData(@NotNull final DataProcessorContext context) throws Exception {
    final AgentRunningBuild currentBuild = context.getBuild();

    final String checkoutDir = currentBuild.getCheckoutDirectory().toString();

    final SimpleBuildLogger logger = currentBuild.getBuildLogger();
    final FxCopFileProcessor fileProcessor =
      new FxCopFileProcessor(context.getFile(), checkoutDir, logger, myReporter);

    myReporter.markBuildAsInspectionsBuild();
    fileProcessor.processReport();
  }

  @NotNull
  public String getType() {
    return "FxCop";
  }
}

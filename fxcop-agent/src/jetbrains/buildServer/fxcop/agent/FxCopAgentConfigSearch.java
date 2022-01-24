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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Provides fxCop info based on system parameter `FxCopRoot`.
 */
public class FxCopAgentConfigSearch implements FxCopSearch {
  @NotNull
  @Override
  public Collection<File> getHintPaths(@NotNull final BuildAgentConfiguration config) {
    final String fxCopRoot = config.getBuildParameters().getAllParameters().get(FxCopConstants.FXCOP_ROOT_PROPERTY);
    if (StringUtil.isEmpty(fxCopRoot)) {
      return Collections.emptyList();
    }

    return Collections.singleton(new File(fxCopRoot, FxCopConstants.FXCOPCMD_BINARY));
  }
}

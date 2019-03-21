/*
 * Copyright 2000-2018 JetBrains s.r.o.
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

package jetbrains.buildServer.fxcop.common;

import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Evgeniy.Koshkin
 * Date: 25.10.11
 * Time: 13:28
 */
public enum FxCopVersion {

  not_specified("not_specified", "Any Detected") {
    @Override
    public Requirement createRequirement() {
      return new Requirement(FxCopConstants.FXCOPCMD_FILE_VERSION_PROPERTY, null, RequirementType.EXISTS);
    }
  },
  v1_35("1.35", "1.35"),
  v1_36("9.0", "1.36 (9.0)"),
  v10_0("10.0", "10.0"),
  v12_0("12.0", "12.0"),
  v14_0("14.0", "14.0"),
  v15_0("15.0", "15.0"),
  v16_0("16.0", "16.0");

  private final String myTechnicalVersionPrefix;
  private final String myDisplayName;

  FxCopVersion(final String technicalVersionPrefix, final String displayName) {
    myTechnicalVersionPrefix = technicalVersionPrefix;
    myDisplayName = displayName;
  }

  @NotNull
  public String getTechnicalVersionPrefix() {
    return myTechnicalVersionPrefix;
  }

  @NotNull
  public String getDisplayName() {
    return myDisplayName;
  }

  @Nullable
  public Requirement createRequirement(){
    return new Requirement(FxCopConstants.FXCOPCMD_FILE_VERSION_PROPERTY, getTechnicalVersionPrefix(), RequirementType.STARTS_WITH);
  }
}

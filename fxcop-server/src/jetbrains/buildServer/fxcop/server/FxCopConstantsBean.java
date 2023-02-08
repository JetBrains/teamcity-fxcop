/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

package jetbrains.buildServer.fxcop.server;

import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.fxcop.common.FxCopVersion;
import org.jetbrains.annotations.NotNull;

public class FxCopConstantsBean {
  @NotNull
  public String getWhatToInspectKey() {
    return FxCopConstants.SETTINGS_WHAT_TO_INSPECT;
  }

  @NotNull
  public String getFilesKey() {
    return FxCopConstants.SETTINGS_FILES;
  }

  @NotNull
  public String getFilesExcludeKey() {
    return FxCopConstants.SETTINGS_FILES_EXCLUDE;
  }

  @NotNull
  public String getProjectKey() {
    return FxCopConstants.SETTINGS_PROJECT;
  }

  @NotNull
  public String getWhatToInspectProjectValue() {
    return FxCopConstants.WHAT_TO_INSPECT_PROJECT;
  }

  @NotNull
  public String getWhatToInspectFilesValue() {
    return FxCopConstants.WHAT_TO_INSPECT_FILES;
  }

  @NotNull
  public String getAddtionalOptionsKey() {
    return FxCopConstants.SETTINGS_ADDITIONAL_OPTIONS;
  }

  @NotNull
  public String getReportXsltKey() {
    return FxCopConstants.SETTINGS_REPORT_XSLT;
  }

  @NotNull
  public String getRootKey() {
    return FxCopConstants.SETTINGS_FXCOP_ROOT;
  }

  @NotNull
  public String getVersionKey() {
    return FxCopConstants.SETTINGS_FXCOP_VERSION;
  }

  @NotNull
  public String getSearchDirsKey() {
    return FxCopConstants.SETTINGS_SEARCH_DIRS;
  }

  @NotNull
  public String getSearchInGacKey() {
    return FxCopConstants.SETTINGS_SEARCH_IN_GAC;
  }

  @NotNull
  public String getIgnoreGeneratedCodeKey() {
    return FxCopConstants.SETTINGS_IGNORE_GENERATED_CODE;
  }

  @NotNull
  public String getFailOnAnalysisErrorKey() {
    return FxCopConstants.SETTINGS_FAIL_ON_ANALYSIS_ERROR;
  }

  @NotNull
  public FxCopVersion[] getAvaliableVersions() {
    return FxCopVersion.values();
  }

  @NotNull
  public String getDetectionModeKey() {
    return FxCopConstants.SETTINGS_DETECTION_MODE;
  }

  @NotNull
  public String getDetectionModeAuto() {
    return FxCopConstants.DETECTION_MODE_AUTO;
  }

  @NotNull
  public String getDetectionModeManual() {
    return FxCopConstants.DETECTION_MODE_MANUAL;
  }
}

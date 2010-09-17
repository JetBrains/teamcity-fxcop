/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.Constants;

public interface FxCopConstants {
  String FXCOP_ROOT_PROPERTY = Constants.SYSTEM_PREFIX + "FxCopRoot";
  String FXCOPCMD_FILE_VERSION_PROPERTY = Constants.SYSTEM_PREFIX + "FxCopCmdFileVersion";

  String RUNNER_TYPE = "FxCop";
  String FXCOPCMD_BINARY = "FxCopCmd.exe";

  String SETTINGS_WHAT_TO_INSPECT = "fxcop.what";
  String SETTINGS_FILES = "fxcop.files";
  String SETTINGS_FILES_EXCLUDE = "fxcop.files_exclude";
  String SETTINGS_PROJECT = "fxcop.project";
  String SETTINGS_ADDITIONAL_OPTIONS = "fxcop.addon_options";
  String SETTINGS_REPORT_XSLT = "fxcop.report_xslt";
  String SETTINGS_FXCOP_ROOT = "fxcop.root";
  String SETTINGS_SEARCH_DIRS = "fxcop.search_in_dirs";
  String SETTINGS_SEARCH_IN_GAC = "fxcop.search_in_gac";
  String SETTINGS_IGNORE_GENERATED_CODE = "fxcop.ignore_generated_code";
  String SETTINGS_FAIL_ON_ANALYSIS_ERROR = "fxcop.fail_on_analysis_error";
  String SETTINGS_ERROR_LIMIT = "fxcop.fail.error.limit";
  String SETTINGS_WARNING_LIMIT = "fxcop.fail.warning.limit";

  String WHAT_TO_INSPECT_FILES = "files";
  String WHAT_TO_INSPECT_PROJECT = "project";

  String RUNNER_DISPLAY_NAME = "FxCop";
  String RUNNER_DESCRIPTION = "FxCop static code analysis tool runner (.NET)";

  String REPORT_FILE = "fxcop-report.html";
  String OUTPUT_FILE = "fxcop-result.xml";
}

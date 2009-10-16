/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
import java.util.List;
import java.util.Map;
import java.util.Vector;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopCommandLineBuilder {
  @NotNull
  public static String getExecutablePath(Map<String, String> runParameters) throws RunBuildException {
    final String fxcopRootRelative = runParameters.get(FxCopConstants.SETTINGS_FXCOP_ROOT);
    if (StringUtil.isEmpty(fxcopRootRelative)) {
      throw new RunBuildException("FxCop root not specified in build settings");
    }

    return new File(fxcopRootRelative, FxCopConstants.FXCOPCMD_BINARY).getPath();
  }

  @NotNull
  public static List<String> getArguments(Map<String, String> runParameters, List<String> files) throws RunBuildException {
    List<String> arguments = new Vector<String>();

    arguments.add("/forceoutput");

    // Search in GAC
    if (isParameterEnabled(runParameters, FxCopConstants.SETTINGS_SEARCH_IN_GAC)) {
      arguments.add("/gac");
    }

    // Ignore generated code
    if (isParameterEnabled(runParameters, FxCopConstants.SETTINGS_IGNORE_GENERATED_CODE)) {
      arguments.add("/ignoregeneratedcode");
    }

    // Search in dirs
    final String searchDirsString = runParameters.get(FxCopConstants.SETTINGS_SEARCH_DIRS);
    if (searchDirsString != null) {
      for (String file : StringUtil.splitCommandArgumentsAndUnquote(searchDirsString)) {
        arguments.add("/d:" + file);
      }
    }

    // Additional options
    final String additionalOptions = runParameters.get(FxCopConstants.SETTINGS_ADDITIONAL_OPTIONS);
    if (additionalOptions != null) {
      arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(additionalOptions));
    }

    // Files to be processed
    final String what = runParameters.get(FxCopConstants.SETTINGS_WHAT_TO_INSPECT);
    if (FxCopConstants.WHAT_TO_INSPECT_PROJECT.equals(what)) {
      final String project = runParameters.get(FxCopConstants.SETTINGS_PROJECT);
      if (project != null) {
        arguments.add("/project:" + project);
      }
    } else if (FxCopConstants.WHAT_TO_INSPECT_FILES.equals(what)) {
      if (files != null) {
        for (String file : files) {
          arguments.add("/f:" + file);
        }
      }
    } else {
      throw new RunBuildException("Unknown target to inspect: " + what);
    }

    // Output file
    arguments.add("/out:" + FxCopConstants.OUTPUT_DIR + File.separator + FxCopConstants.OUTPUT_FILE);

    return arguments;
  }

  private static boolean isParameterEnabled(final Map<String, String> runParameters, final String key) {
    return runParameters.containsKey(key) && runParameters.get(key)
      .equals(Boolean.TRUE.toString());
  }
}

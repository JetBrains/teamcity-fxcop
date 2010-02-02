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

package jetbrains.buildServer.fxcop.server;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;

public class FxCopRunTypePropertiesProcessor implements PropertiesProcessor {
  public Collection<InvalidProperty> process(Map<String, String> properties) {
    List<InvalidProperty> result = new Vector<InvalidProperty>();

    final String files = properties.get(FxCopConstants.SETTINGS_FILES);
    final String project = properties.get(FxCopConstants.SETTINGS_PROJECT);

    if (PropertiesUtil.isEmptyOrNull(project) && PropertiesUtil.isEmptyOrNull(files)) {
      result.add(
        new InvalidProperty(
          FxCopConstants.SETTINGS_FILES,
          "Files or project option must be specified"));
    }

    final String fxcopRoot = properties.get(
      FxCopConstants.SETTINGS_FXCOP_ROOT);
    if (PropertiesUtil.isEmptyOrNull(fxcopRoot)) {
      result.add(
        new InvalidProperty(
          FxCopConstants.SETTINGS_FXCOP_ROOT,
          "FxCop installation root must be specified"));
    }

    if (!PropertiesUtil.isEmptyOrNull(properties.get(FxCopConstants.SETTINGS_ERROR_LIMIT))) {
      Integer value = PropertiesUtil.parseInt(properties.get(FxCopConstants.SETTINGS_ERROR_LIMIT));
      if (value == null || value < 0) {
        result.add(new InvalidProperty(FxCopConstants.SETTINGS_ERROR_LIMIT, "Errors limit must be a positive number or zero"));
      }
    }

    if (!PropertiesUtil.isEmptyOrNull(properties.get(FxCopConstants.SETTINGS_WARNING_LIMIT))) {
      Integer value = PropertiesUtil.parseInt(properties.get(FxCopConstants.SETTINGS_WARNING_LIMIT));
      if (value == null || value < 0) {
        result.add(new InvalidProperty(FxCopConstants.SETTINGS_WARNING_LIMIT, "Warnings limit must be a positive number or zero"));
      }
    }

    return result;
  }
}

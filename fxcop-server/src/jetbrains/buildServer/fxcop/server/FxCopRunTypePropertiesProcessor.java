

package jetbrains.buildServer.fxcop.server;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopRunTypePropertiesProcessor implements PropertiesProcessor {

  @NotNull private final Map<String, String> myDefaultParameters;

  public FxCopRunTypePropertiesProcessor(@NotNull Map<String, String> defaultParameters) {
    myDefaultParameters = defaultParameters;
  }

  public Collection<InvalidProperty> process(Map<String, String> properties) {
    List<InvalidProperty> result = new Vector<InvalidProperty>();

    final String files = properties.get(FxCopConstants.SETTINGS_FILES);
    final String project = properties.get(FxCopConstants.SETTINGS_PROJECT);

    if (PropertiesUtil.isEmptyOrNull(project) && PropertiesUtil.isEmptyOrNull(files)) {
      result.add(new InvalidProperty(FxCopConstants.SETTINGS_FILES, "Files or project option must be specified"));
    }

    if(FxCopConstants.DETECTION_MODE_MANUAL.equals(properties.get(FxCopConstants.SETTINGS_DETECTION_MODE))){
      final String fxcopRoot = properties.get(FxCopConstants.SETTINGS_FXCOP_ROOT);
      if (PropertiesUtil.isEmptyOrNull(fxcopRoot)) {
        result.add(new InvalidProperty(FxCopConstants.SETTINGS_FXCOP_ROOT, "FxCop installation root must be specified"));
      }
    }

    if (FxCopConstants.DETECTION_MODE_MANUAL.equals(properties.get(FxCopConstants.SETTINGS_DETECTION_MODE))) {
      resetProperty(properties, FxCopConstants.SETTINGS_FXCOP_VERSION);
    } else {
      resetProperty(properties, FxCopConstants.SETTINGS_FXCOP_ROOT);
    }

    if (FxCopConstants.WHAT_TO_INSPECT_PROJECT.equals(properties.get(FxCopConstants.SETTINGS_WHAT_TO_INSPECT))) {
      resetProperty(properties, FxCopConstants.SETTINGS_FILES);
      resetProperty(properties, FxCopConstants.SETTINGS_FILES_EXCLUDE);
    } else {
      resetProperty(properties, FxCopConstants.SETTINGS_PROJECT);
    }

    return result;
  }

  private void resetProperty(@NotNull Map<String, String> properties, @NotNull String key) {
    if (myDefaultParameters.containsKey(key)) {
      properties.put(key, myDefaultParameters.get(key));
    } else {
      properties.remove(key);
    }
  }
}
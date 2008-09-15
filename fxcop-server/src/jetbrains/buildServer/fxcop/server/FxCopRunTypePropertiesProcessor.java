package jetbrains.buildServer.fxcop.server;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.PropertiesUtil;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 11.09.2008
 * Time: 12:16:01
 */
public class FxCopRunTypePropertiesProcessor implements PropertiesProcessor {
  public Collection<InvalidProperty> process(Map properties) {
    List<InvalidProperty> result = new Vector<InvalidProperty>();

    final String files = (String)properties.get(
      FxCopConstants.SETTINGS_FILES);
    final String project = (String)properties.get(
      FxCopConstants.SETTINGS_PROJECT);
    if (PropertiesUtil.isEmptyOrNull(project) && PropertiesUtil.isEmptyOrNull(files)) {
      result.add(
        new InvalidProperty(
          FxCopConstants.SETTINGS_FILES,
          "Files or project option must be specified"));
    }

    final String fxcopRoot = (String)properties.get(
      FxCopConstants.SETTINGS_FXCOP_ROOT);
    if (PropertiesUtil.isEmptyOrNull(fxcopRoot)) {
      result.add(
        new InvalidProperty(
          FxCopConstants.SETTINGS_FXCOP_ROOT,
          "FxCop installation root must be specified"));
    }

    return result;
  }
}

package jetbrains.buildServer.fxcop.server;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 28.08.2008
 * Time: 20:34:56
 */
public class FxCopRunType extends RunType {
  public FxCopRunType(final RunTypeRegistry runTypeRegistry) {
    runTypeRegistry.registerRunType(this);
  }

  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new FxCopRunTypePropertiesProcessor();
  }

  public String getDescription() {
    return FxCopConstants.RUNNER_DESCRIPTION;
  }

  public String getEditRunnerParamsJspFilePath() {
    return "fxcopRunParams.jsp";
  }

  public String getViewRunnerParamsJspFilePath() {
    return "viewFxcopRunParams.jsp";
  }

  public Map<String, String> getDefaultRunnerProperties() {
    Map<String, String> map = new HashMap<String, String>();
    setUpDefaultParams(map);

    return map;
  }

  public String getType() {
    return FxCopConstants.RUNNER_TYPE;
  }

  public String getDisplayName() {
    return FxCopConstants.RUNNER_DISPLAY_NAME;
  }

  private static void setUpDefaultParams(Map<String, String> parameters) {
    parameters.put(
      FxCopConstants.SETTINGS_FXCOP_ROOT,
      "%" + FxCopConstants.FXCOP_ROOT_PROPERTY + "%");
    parameters.put(
      FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
  }
}

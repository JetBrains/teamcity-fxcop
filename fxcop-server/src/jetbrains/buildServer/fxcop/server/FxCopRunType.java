

package jetbrains.buildServer.fxcop.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.fxcop.common.FxCopVersion;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

public class FxCopRunType extends RunType {
  private final PluginDescriptor myPluginDescriptor;
  private final Map<String, String> myDefaultParameters;

  public FxCopRunType(final RunTypeRegistry runTypeRegistry, final PluginDescriptor pluginDescriptor) {
    myPluginDescriptor = pluginDescriptor;
    runTypeRegistry.registerRunType(this);
    myDefaultParameters = new HashMap<>();
    myDefaultParameters.put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_AUTO);
    myDefaultParameters.put(FxCopConstants.SETTINGS_FXCOP_VERSION, FxCopVersion.not_specified.getTechnicalVersionPrefix());
    myDefaultParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
    myDefaultParameters.put(FxCopConstants.SETTINGS_SEARCH_IN_GAC, "true");
    myDefaultParameters.put(FxCopConstants.SETTINGS_FAIL_ON_ANALYSIS_ERROR, "true");
  }

  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return new FxCopRunTypePropertiesProcessor(myDefaultParameters);
  }

  @NotNull
  @Override
  public String getDescription() {
    return FxCopConstants.RUNNER_DESCRIPTION;
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return myPluginDescriptor.getPluginResourcesPath("editFxCopRunParams.jsp");
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return myPluginDescriptor.getPluginResourcesPath("viewFxcopRunParams.jsp");
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return myDefaultParameters;
  }

  @Override
  @NotNull
  public String getType() {
    return FxCopConstants.RUNNER_TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return FxCopConstants.RUNNER_DISPLAY_NAME;
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull final Map<String, String> parameters) {
    StringBuilder result = new StringBuilder();
    String what = parameters.get(FxCopConstants.SETTINGS_WHAT_TO_INSPECT);
    if (what == null || FxCopConstants.WHAT_TO_INSPECT_FILES.equals(what)) {
      result.append("Assemblies: ").append(StringUtil.emptyIfNull(parameters.get(FxCopConstants.SETTINGS_FILES)));
    } else {
      result.append("FxCop project: ").append(StringUtil.emptyIfNull(parameters.get(FxCopConstants.SETTINGS_PROJECT)));
    }
    return result.toString();
  }

  @NotNull
  @Override
  public List<Requirement> getRunnerSpecificRequirements(@NotNull final Map<String, String> runParameters) {
    return FxCopRequirementsUtil.getFxCopRequirements(runParameters);
  }
}
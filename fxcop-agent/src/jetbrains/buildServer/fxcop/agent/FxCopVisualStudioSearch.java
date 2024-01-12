

package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.config.AgentParametersSupplier;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Search for fxCop in detected Visual Studio installations.
 */
public class FxCopVisualStudioSearch implements FxCopSearch {
  static final String FXCOP_RELATIVE_PATH = "../../Team Tools/Static Analysis Tools/FxCop/";
  private static final String FXCOP_EXE_RELATIVE_PATH = FXCOP_RELATIVE_PATH + FxCopConstants.FXCOPCMD_BINARY;
  private static final Pattern VISUAL_STUDIO_PATTERN = Pattern.compile("VS[\\d]+_Path");

  @NotNull
  @Override
  public Collection<File> getHintPaths(@NotNull final BuildAgentConfiguration config, @Nullable AgentParametersSupplier dotNetParametersSupplier) {
    if (dotNetParametersSupplier == null){
      return Collections.emptyList();
    }

    final Map<String, String> parameters = dotNetParametersSupplier.getParameters();
    final TreeSet<String> visualStudio = new TreeSet<String>();

    for (String key : parameters.keySet()) {
      if (VISUAL_STUDIO_PATTERN.matcher(key).find()) {
        visualStudio.add(key);
      }
    }

    return CollectionsUtil.filterAndConvertCollection(visualStudio.descendingSet(), new Converter<File, String>() {
      @Override
      public File createFrom(@NotNull final String name) {
        return new File(parameters.get(name), FXCOP_EXE_RELATIVE_PATH);
      }
    }, new Filter<String>() {
      @Override
      public boolean accept(@NotNull final String name) {
        return StringUtil.isNotEmpty(parameters.get(name));
      }
    });
  }
}
package jetbrains.buildServer.fxcop.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.fxcop.common.FxCopVersion;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.fxcop.common.FxCopConstants.*;
import static jetbrains.buildServer.fxcop.common.FxCopVersion.not_specified;

/**
 * Created by IntelliJ IDEA.
 * User: Evgeniy.Koshkin
 * Date: 12.01.12
 * Time: 16:37
 */
public class FxCopRequirementsUtil {
  @NotNull
  public static List<Requirement> getFxCopRequirements(final Map<String, String> runParameters) {
    final List<Requirement> list = new ArrayList<Requirement>();
    final String detectionMode = runParameters.get(SETTINGS_DETECTION_MODE);
    if (detectionMode != null && detectionMode.equals(DETECTION_MODE_AUTO)) {
      list.add(new Requirement(FXCOP_ROOT_PROPERTY, null, RequirementType.EXISTS));

      final String specifiedFxCopVersion = runParameters.get(SETTINGS_FXCOP_VERSION);
      if (specifiedFxCopVersion == null) {
        list.add(not_specified.createRequirement());
      } else {
        for (FxCopVersion version : FxCopVersion.values()) {
          if (version.getTechnicalVersionPrefix().equals(specifiedFxCopVersion)) {
            final Requirement requirement = version.createRequirement();
            if (requirement != null) list.add(requirement);
            break;
          }
        }
      }
    }
    return list;
  }
}

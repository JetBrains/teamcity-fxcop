package jetbrains.buildServer.fxcop.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.fxcop.common.FxCopVersion;
import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.requirements.RequirementType;

/**
 * Created by IntelliJ IDEA.
 * User: Evgeniy.Koshkin
 * Date: 12.01.12
 * Time: 16:37
 */
public class FxCopRequirementsUtil {
  public static List<Requirement> getFxCopRequirements(final Map<String, String> runParameters) {
    List<Requirement> list = new ArrayList<Requirement>();
    final String detectionMode = runParameters.get(FxCopConstants.SETTINGS_DETECTION_MODE);
    if(detectionMode != null && detectionMode.equals(FxCopConstants.DETECTION_MODE_AUTO)){
      list.add(new Requirement(FxCopConstants.FXCOP_ROOT_PROPERTY, null, RequirementType.EXISTS));

      final String specifiedFxCopVersion = runParameters.get(FxCopConstants.SETTINGS_FXCOP_VERSION);
      if (specifiedFxCopVersion == null) {
        list.add(FxCopVersion.not_specified.createRequirement());
      }
      else{
        for(FxCopVersion version : FxCopVersion.values()){
          if(version.getTechnicalVersionPrefix().equals(specifiedFxCopVersion)) {
            final Requirement requirement = version.createRequirement();
            if(requirement != null) list.add(requirement);
            break;
          }
        }
      }
    }
    return list;
  }
}

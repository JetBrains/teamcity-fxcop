

package jetbrains.buildServer.fxcop.server;

import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.requirements.Requirement;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Evgeniy.Koshkin
 * Date: 12.01.12
 * Time: 16:35
 */
@Test
public class FxCopRequirementsUtilTest {
  public void testAutoDetectionMode(){
    final HashMap<String, String> params = new HashMap<String, String>();
    params.put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_AUTO);

    HashMap<String, String> agentProperties = new HashMap<String, String>();

    doTest(params, agentProperties, false);

    agentProperties.put(FxCopConstants.FXCOP_ROOT_PROPERTY, "foo");
    doTest(params, agentProperties, false);

    agentProperties.put(FxCopConstants.FXCOPCMD_FILE_VERSION_PROPERTY, "foo");
    doTest(params, agentProperties, true);
  }

  public void testManualDetectionMode(){
    final HashMap<String, String> params = new HashMap<String, String>();
    params.put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_MANUAL);
    doTest(params, new HashMap<String, String>(), true);
  }

  private static void doTest(Map<String, String> runnerParameters, Map<String, String> agentProperties, boolean shouldMatch){
    for(Requirement requirement : FxCopRequirementsUtil.getFxCopRequirements(runnerParameters)){
      final boolean match = requirement.match(agentProperties, true);
      if(match) continue;
      if(!shouldMatch) return;
      else Assert.fail("Requirement was not matched");
    }
    if(!shouldMatch) Assert.fail("All requirements were matched");
  }
}
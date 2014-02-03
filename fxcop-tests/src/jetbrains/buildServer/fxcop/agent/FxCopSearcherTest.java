package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.util.Win32RegistryAccessor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class FxCopSearcherTest extends BaseTestCase {

  private Mockery m;

  private Win32RegistryAccessor myAccessor;

  private BuildAgentConfiguration myConfig;

  private FxCopSearcher mySearcher;

  private BuildAgentSystemInfo myInfo;

  private BuildParametersMap myBuildParameters;

  private File myFxCopRoot;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myAccessor = m.mock(Win32RegistryAccessor.class);
    myConfig = m.mock(BuildAgentConfiguration.class);
    myInfo = m.mock(BuildAgentSystemInfo.class);
    myBuildParameters = m.mock(BuildParametersMap.class);
    myFxCopRoot = createTempDir();
    mySearcher = new FxCopSearcher(myAccessor);
  }

  @Test
  @TestFor(issues = "TW-34034")
  public void testDetectFromAgentConfig() throws Exception {
    final Map<String, String> allParams = new HashMap<String, String>() {{
      put(FxCopConstants.FXCOP_ROOT_PROPERTY, myFxCopRoot.getPath());
    }};

    m.checking(new Expectations() {{
      oneOf(myConfig).getSystemInfo();
      will(returnValue(myInfo));

      oneOf(myInfo).isWindows();
      will(returnValue(true));

      oneOf(myConfig).getBuildParameters();
      will(returnValue(myBuildParameters));

      oneOf(myBuildParameters).getAllParameters();
      will(returnValue(allParams));

      oneOf(myConfig).addCustomProperty(FxCopConstants.FXCOP_ROOT_PROPERTY, myFxCopRoot.getPath());

    }});
    mySearcher.search(myConfig);

  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  @TestFor(issues = "TW-34034")
  public void testDetectFromWinRegistry() throws Exception {
    final Map<String, String> allParams = new HashMap<String, String>();
    final File binary = new File(myFxCopRoot, "fxcop.exe");
    binary.createNewFile();
    registerAsTempFile(binary);

    m.checking(new Expectations() {{
      oneOf(myConfig).getSystemInfo();
      will(returnValue(myInfo));

      oneOf(myInfo).isWindows();
      will(returnValue(true));

      oneOf(myConfig).getBuildParameters();
      will(returnValue(myBuildParameters));

      oneOf(myBuildParameters).getAllParameters();
      will(returnValue(allParams));

      oneOf(myAccessor).readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, ".fxcop", "");
      will(returnValue(".fxcop_placeholder"));

      oneOf(myAccessor).readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, ".fxcop_placeholder\\shell\\open\\command", "");
      will(returnValue(binary.getPath()));

      oneOf(myConfig).addCustomProperty(FxCopConstants.FXCOP_ROOT_PROPERTY, myFxCopRoot.getPath());

    }});
    mySearcher.search(myConfig);

  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  @TestFor(issues = "TW-34034")
  public void testDetectFromVs2010() throws Exception {
    final Map<String, String> allParams = new HashMap<String, String>();


    final File rootDir = createTempDir();
    final File vs2010dir = new File(rootDir, "1/2/");
    vs2010dir.mkdirs();
    registerAsTempFile(vs2010dir);
    final File fxCopDir = new File(vs2010dir, FxCopSearcher.FXCOP_RELATIVE_PATH);
    fxCopDir.mkdirs();
    registerAsTempFile(fxCopDir);
    final File fxCopBinary = new File(fxCopDir, FxCopConstants.FXCOPCMD_BINARY);
    fxCopBinary.createNewFile();
    registerAsTempFile(fxCopBinary);

    final Map<String, String> configParams = new HashMap<String, String>() {{
      put(FxCopSearcher.VS_2010_PATH, vs2010dir.getPath());
    }};

    m.checking(new Expectations() {{
      oneOf(myConfig).getSystemInfo();
      will(returnValue(myInfo));

      oneOf(myInfo).isWindows();
      will(returnValue(true));

      oneOf(myConfig).getBuildParameters();
      will(returnValue(myBuildParameters));

      oneOf(myBuildParameters).getAllParameters();
      will(returnValue(allParams));

      oneOf(myAccessor).readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, ".fxcop", "");
      will(returnValue(null));

      oneOf(myConfig).getConfigurationParameters();
      will(returnValue(configParams));

      oneOf(myConfig).addCustomProperty(FxCopConstants.FXCOP_ROOT_PROPERTY, fxCopBinary.getParentFile().getCanonicalPath());

    }});

    mySearcher.search(myConfig);
  }


  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    m.assertIsSatisfied();
  }
}

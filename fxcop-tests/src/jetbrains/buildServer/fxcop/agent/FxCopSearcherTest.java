

package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.config.AgentParametersSupplier;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.util.FileUtil;
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

  private Win32RegistryAccessor myRegistryAccessor;

  private BuildAgentConfiguration myConfig;

  private FxCopSearcher mySearcher;

  private BuildAgentSystemInfo myInfo;

  private BuildParametersMap myBuildParameters;

  private AgentParametersSupplier myDotNetParametersSupplier;

  private File myFxCopRoot;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myRegistryAccessor = m.mock(Win32RegistryAccessor.class);
    myConfig = m.mock(BuildAgentConfiguration.class);
    myInfo = m.mock(BuildAgentSystemInfo.class);
    myBuildParameters = m.mock(BuildParametersMap.class);
    ExtensionHolder extensionHolder = m.mock(ExtensionHolder.class);
    myDotNetParametersSupplier = m.mock(AgentParametersSupplier.class);
    final String fxCopPath = TestUtils.getTestDataPath("searcher/fxCop");
    myFxCopRoot = FileUtil.getCanonicalFile(new File(fxCopPath));

    m.checking(new Expectations(){{
      oneOf(extensionHolder).getExtension(AgentParametersSupplier.class, FxCopConstants.DOTNET_SUPPLIER_NAME);
      will(returnValue(myDotNetParametersSupplier));
    }});

    mySearcher = new FxCopSearcher(myRegistryAccessor, myConfig, extensionHolder);
  }

  @Test
  @TestFor(issues = "TW-34034")
  public void testDetectFromAgentConfig() {
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
    }});
    final Map<String, String> systemProperties = mySearcher.search();

    assertEquals(myFxCopRoot.getPath(), systemProperties.get(FxCopConstants.FXCOP_ROOT_NAME));
    assertEquals("8.0.760.198", systemProperties.get(FxCopConstants.FXCOPCMD_FILE_VERSION_NAME));
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  @TestFor(issues = "TW-34034")
  public void testDetectFromWinRegistry() {
    final Map<String, String> allParams = new HashMap<String, String>();
    final File binary = new File(myFxCopRoot, FxCopConstants.FXCOPCMD_BINARY);

    m.checking(new Expectations() {{
      oneOf(myConfig).getSystemInfo();
      will(returnValue(myInfo));

      oneOf(myInfo).isWindows();
      will(returnValue(true));

      oneOf(myConfig).getBuildParameters();
      will(returnValue(myBuildParameters));

      allowing(myConfig).getConfigurationParameters();
      will(returnValue(allParams));

      oneOf(myBuildParameters).getAllParameters();
      will(returnValue(allParams));

      oneOf(myRegistryAccessor).readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, ".fxcop", "");
      will(returnValue(".fxcop_placeholder"));

      oneOf(myRegistryAccessor).readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, ".fxcop_placeholder\\shell\\open\\command", "");
      will(returnValue(binary.getPath()));
    }});
    final Map<String, String> systemProperties = mySearcher.search();

    assertEquals(myFxCopRoot.getPath(), systemProperties.get(FxCopConstants.FXCOP_ROOT_NAME));
    assertEquals("8.0.760.198", systemProperties.get(FxCopConstants.FXCOPCMD_FILE_VERSION_NAME));

  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  @TestFor(issues = "TW-34034")
  public void testDetectFromVs2010() throws Exception {
    final Map<String, String> allParams = new HashMap<String, String>();

    final File rootDir = createTempDir();
    final File vs2010dir = new File(rootDir, "vs2010");
    vs2010dir.mkdirs();
    registerAsTempFile(vs2010dir);
    final File fxCopDir = new File(vs2010dir, FxCopVisualStudioSearch.FXCOP_RELATIVE_PATH);
    fxCopDir.mkdirs();
    registerAsTempFile(fxCopDir);
    final File fxCopBinary = new File(fxCopDir, FxCopConstants.FXCOPCMD_BINARY);
    FileUtil.copy(new File(myFxCopRoot, FxCopConstants.FXCOPCMD_BINARY), fxCopBinary);
    registerAsTempFile(fxCopBinary);

    final Map<String, String> configParams = new HashMap<String, String>() {{
      put("VS2010_Path", vs2010dir.getPath());
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

      oneOf(myRegistryAccessor).readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, ".fxcop", "");
      will(returnValue(null));

      oneOf(myDotNetParametersSupplier).getParameters();
      will(returnValue(configParams));
    }});

    final Map<String, String> systemProperties = mySearcher.search();

    assertEquals(fxCopBinary.getParentFile().getCanonicalPath(), systemProperties.get(FxCopConstants.FXCOP_ROOT_NAME));
    assertEquals("8.0.760.198", systemProperties.get(FxCopConstants.FXCOPCMD_FILE_VERSION_NAME));
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  @TestFor(issues = "TW-56574")
  public void testDetectFromMsBuild() throws Exception {
    final Map<String, String> allParams = new HashMap<String, String>();

    final File rootDir = createTempDir();
    final File msBuildDir = new File(rootDir, "msBuild");
    msBuildDir.mkdirs();
    registerAsTempFile(msBuildDir);
    final File fxCopDir = new File(msBuildDir, FxCopMsBuildSearch.FXCOP_RELATIVE_PATH);
    fxCopDir.mkdirs();
    registerAsTempFile(fxCopDir);
    final File fxCopBinary = new File(fxCopDir, FxCopConstants.FXCOPCMD_BINARY);
    FileUtil.copy(new File(myFxCopRoot, FxCopConstants.FXCOPCMD_BINARY), fxCopBinary);
    registerAsTempFile(fxCopBinary);

    final Map<String, String> configParams = new HashMap<String, String>() {{
      put("MSBuildTools15.0_x86_Path", msBuildDir.getPath());
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

      oneOf(myRegistryAccessor).readRegistryText(Win32RegistryAccessor.Hive.CLASSES_ROOT, Bitness.BIT32, ".fxcop", "");
      will(returnValue(null));

      allowing(myDotNetParametersSupplier).getParameters();
      will(returnValue(configParams));
    }});

    final Map<String, String> systemProperties = mySearcher.search();

    assertEquals(fxCopBinary.getParentFile().getCanonicalPath(), systemProperties.get(FxCopConstants.FXCOP_ROOT_NAME));
    assertEquals("8.0.760.198", systemProperties.get(FxCopConstants.FXCOPCMD_FILE_VERSION_NAME));
  }


  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    m.assertIsSatisfied();
  }
}
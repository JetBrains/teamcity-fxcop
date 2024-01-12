

package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.NullBuildProgressLogger;
import jetbrains.buildServer.agent.SimpleBuildLogger;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.StringUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class FxCopCommandLineBuilderImplTest extends BaseTestCase {
  private Map<String, String> myRunParameters;
  private Map<String, String> myBuildParameters;
  private File myXmlReportFile;
  private String myExpectedPostfix;
  private final String myExpectedPrefix = "[/forceoutput] ";
  private final SimpleBuildLogger myLogger = new NullBuildProgressLogger();

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myRunParameters = new HashMap<String, String>();
    myBuildParameters = new HashMap<String, String>();
    myXmlReportFile = new File("C:\\a\\b");
    myExpectedPostfix = "[/out:" + myXmlReportFile.getPath() + "] ";
  }

  private void assertCmdArgs(final String expected) throws Exception {
    final String filesSetting = myRunParameters.get(FxCopConstants.SETTINGS_FILES);
    final List<String> files = filesSetting != null
                               ? StringUtil.splitCommandArgumentsAndUnquote(filesSetting)
                               : new ArrayList<String>();

    final FxCopCommandLineBuilder commandLineBuilder = new FxCopCommandLineBuilder(myRunParameters, myBuildParameters, myXmlReportFile,
                                                                                   myLogger);
    final List<String> args = commandLineBuilder.getArguments(files);

    StringBuilder result = new StringBuilder();
    for (String chunk : args) {
      result.append("[").append(chunk).append("] ");
    }
    assertEquals(myExpectedPrefix + expected + myExpectedPostfix, result.toString());
  }

  private void assertExecutablePath(final String expected) throws Exception {
    final FxCopCommandLineBuilder commandLineBuilder = new FxCopCommandLineBuilder(myRunParameters, myBuildParameters, myXmlReportFile,
                                                                                   myLogger);
    final String executable = commandLineBuilder.getExecutablePath();

    assertEquals(expected, executable);
  }

  @Test(expectedExceptions = RunBuildException.class)
  public void testManualDetectModeNoRoot() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_MANUAL);
    assertCmdArgs("");
  }

  public void testManualDetectModeCmd1() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_MANUAL);
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "a");
    assertExecutablePath("a" + File.separator + "FxCopCmd.exe");
  }

  public void testManualDetectModeCmd2() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_MANUAL);
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "/c/d/");
    assertExecutablePath(new File("/c/d", "FxCopCmd.exe").getPath());
  }

  public void testAutoDetectMode_NoCustomRoot() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_AUTO);
    myBuildParameters.put(FxCopConstants.FXCOP_ROOT_PROPERTY, "/c/d/");
    assertExecutablePath(new File("/c/d", "FxCopCmd.exe").getPath());
  }

  public void testAutoDetectMode_CustomRootSpecified() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_DETECTION_MODE, FxCopConstants.DETECTION_MODE_AUTO);
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "/c/d/");
    myBuildParameters.put(FxCopConstants.FXCOP_ROOT_PROPERTY, "/b/c/d/");
    assertExecutablePath(new File("/b/c/d", "FxCopCmd.exe").getPath());
  }

  @Test(expectedExceptions = RunBuildException.class)
  public void testNoWhat() throws Exception {
    assertCmdArgs("");
  }

  @Test(expectedExceptions = RunBuildException.class)
  public void testUnknownWhat() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, "trash");
    assertCmdArgs("");
  }

  public void testWhatNoProject() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    assertCmdArgs("");
  }

  public void testWhatNoFiles() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
    assertCmdArgs("");
  }

  public void testWhatProject() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    myRunParameters.put(FxCopConstants.SETTINGS_PROJECT, "someproject");
    assertCmdArgs("[/project:someproject] ");
  }

  public void testWhatFiles() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
    myRunParameters.put(FxCopConstants.SETTINGS_FILES, "file1*.dll file2");
    assertCmdArgs("[/f:file1*.dll] [/f:file2] ");
  }

  public void testFilesQuotes() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
    myRunParameters.put(FxCopConstants.SETTINGS_FILES, "\"some dir \\file1*.dll\" file2");
    assertCmdArgs("[/f:some dir \\file1*.dll] [/f:file2] ");
  }

  public void testAddonOptions() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    myRunParameters.put(FxCopConstants.SETTINGS_ADDITIONAL_OPTIONS, "bla-bla qqq /d:\"JJJ KKK\"");
    assertCmdArgs("[bla-bla] [qqq] [/d:\"JJJ KKK\"] ");
  }

  public void testSearchInGAC() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    myRunParameters.put(FxCopConstants.SETTINGS_SEARCH_IN_GAC, "true");
    assertCmdArgs("[/gac] ");
  }

  public void testSearchInDirs() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    myRunParameters.put(FxCopConstants.SETTINGS_SEARCH_DIRS, "\"a b\" c");
    assertCmdArgs("[/d:a b] [/d:c] ");
  }

  public void testIgnoreGeneratedCode() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    myRunParameters.put(FxCopConstants.SETTINGS_IGNORE_GENERATED_CODE, "true");
    assertCmdArgs("[/ignoregeneratedcode] ");
  }
}
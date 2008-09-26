/*
 * Copyright (c) 2008, JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.fxcop.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class FxCopCommandLineBuilderImplTest extends BaseTestCase {
  private Map<String, String> myRunParameters;
  private FxCopCommandLineBuilder myCmdBuilder;
  private final String myOutParam =
    "[/out:" + FxCopConstants.OUTPUT_DIR +
    File.separator + FxCopConstants.OUTPUT_FILE + "] ";

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    myRunParameters = new HashMap<String, String>();
    myCmdBuilder = new FxCopCommandLineBuilderImpl();
    super.setUp();
  }

  private void assertCmdLine(final String expected) throws Exception {
    final GeneralCommandLine cmdLine = new GeneralCommandLine();
    myCmdBuilder.buildCommandLine(cmdLine, myRunParameters);

    StringBuilder result = new StringBuilder();

    for (String chunk : cmdLine.getCommands()) {
      result.append("[").append(chunk).append("] ");
    }
    assertEquals(expected, result.toString());
  }

  @Test(expectedExceptions = RunBuildException.class)
  public void testNoRoot() throws Exception {
    assertCmdLine("");
  }

  @Test(expectedExceptions = RunBuildException.class)
  public void testNoWhat() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "/myroot");
    assertCmdLine("");
  }

  @Test(expectedExceptions = RunBuildException.class)
  public void testUnknownWhat() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "/myroot");
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, "trash");
    assertCmdLine("");
  }

  public void testWhatNoProject() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "/myroot");
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    assertCmdLine("[C:\\myroot\\FxCopCmd.exe] " + myOutParam);
  }

  public void testWhatNoFiles() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "/myroot");
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
    assertCmdLine("[C:\\myroot\\FxCopCmd.exe] " + myOutParam);
  }

  public void testWhatProject() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "/myroot");
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    myRunParameters.put(FxCopConstants.SETTINGS_PROJECT, "someproject");
    assertCmdLine("[C:\\myroot\\FxCopCmd.exe] [/project:someproject] " + myOutParam);
  }

  public void testWhatFiles() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "/myroot");
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_FILES);
    myRunParameters.put(FxCopConstants.SETTINGS_FILES, "file1*.dll file2");
    assertCmdLine("[C:\\myroot\\FxCopCmd.exe] [/f:file1*.dll] [/f:file2] " + myOutParam);
  }

  public void testAddonOptions() throws Exception {
    myRunParameters.put(FxCopConstants.SETTINGS_FXCOP_ROOT, "/myroot");
    myRunParameters.put(FxCopConstants.SETTINGS_WHAT_TO_INSPECT, FxCopConstants.WHAT_TO_INSPECT_PROJECT);
    myRunParameters.put(FxCopConstants.SETTINGS_ADDITIONAL_OPTIONS, "bla-bla qqq");
    assertCmdLine("[C:\\myroot\\FxCopCmd.exe] [bla-bla] [qqq] " + myOutParam);
  }
}

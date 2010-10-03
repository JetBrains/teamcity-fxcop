/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.fxcop.agent;

import java.io.*;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.agent.SimpleBuildLogger;
import jetbrains.buildServer.agent.inspections.InspectionInstance;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.inspections.InspectionTypeInfo;
import jetbrains.buildServer.agent.inspections.InspectionReporterListener;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

@Test
public class FxCopFileProcessorTest extends BaseTestCase {
  private String getTestDataPath(final String fileName) {
    File file = new File("testData/" + fileName);

    if (!file.exists()) {
      file = new File("svnrepo/fxcop/testData/" + fileName);
    }

    return file.getAbsolutePath();
  }

  static private String readFile(@NotNull final File file) throws IOException {
    final FileInputStream inputStream = new FileInputStream(file);
    try {
      final BufferedInputStream bis = new BufferedInputStream(inputStream);
      final byte[] bytes = new byte[(int)file.length()];
      bis.read(bytes);
      bis.close();

      return new String(bytes);
    }
    finally {
      inputStream.close();
    }
  }

  private void runTest(final String fileName) throws Exception {
    final String prefix = getTestDataPath(fileName);
    final String logFile = prefix + ".log";
    final String resultsFile = prefix + ".tmp";
    final String goldFile = prefix + ".gold";

    new File(resultsFile).delete();
    new File(logFile).delete();

    final StringBuilder results = new StringBuilder();

    final SimpleBuildLogger logger = new SimpleBuildLoggerMock(results);
    final InspectionReporter reporter = createFakeReporter(results);

    final FxCopFileProcessor processor = new FxCopFileProcessor(
      new File(prefix), "C:/Work\\Decompiler", logger, reporter);
    processor.processReport();
    results.append("Errors: ").append(processor.getErrorsCount()).append("\r\n");
    results.append("Warnings: ").append(processor.getWarningsCount()).append("\r\n");

    final File goldf = new File(goldFile);
    if (!goldf.exists() || !readFile(goldf).equals(results.toString())) {
      final FileWriter resultsWriter = new FileWriter(resultsFile);
      resultsWriter.write(results.toString());
      resultsWriter.close();

      assertEquals(readFile(goldf), results.toString());
    }
  }

  private InspectionReporter createFakeReporter(final StringBuilder results) {
    return new InspectionReporter() {
      public void reportInspection(@NotNull final InspectionInstance inspection) {
        results.append(inspection.toString()).append("\n");
      }

      public void reportInspectionType(@NotNull final InspectionTypeInfo inspectionType) {
        results.append(inspectionType.toString()).append("\n");
      }

      public void markBuildAsInspectionsBuild() {
      }

      public void flush() {
      }

      public void addListener(@NotNull final InspectionReporterListener listener) {
      }
    };
  }

  public void testSmoke() throws Exception {
    runTest("smoke.xml");
  }

  public void testTargets() throws Exception {
    runTest("targets.xml");
  }

  public void testExceptions() throws Exception {
    runTest("exceptions.xml");
  }
}

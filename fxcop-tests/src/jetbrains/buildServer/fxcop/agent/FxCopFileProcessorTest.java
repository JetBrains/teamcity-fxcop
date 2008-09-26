package jetbrains.buildServer.fxcop.agent;

import java.io.*;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.agent.inspections.InspectionInstance;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.inspections.InspectionTypeInfo;
import jetbrains.buildServer.fxcop.agent.loggers.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 15.08.2008
 * Time: 20:51:13
 */
@Test
public class FxCopFileProcessorTest extends BaseTestCase {
  private String getTestDataPath(final String fileName) {
    return "testData/" + fileName;
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
    
    final SimpleLogger logger = createFakeLogger(results);    
    final InspectionReporter reporter = createFakeReporter(results);

    final FxCopFileProcessor processor = new FxCopFileProcessor(
      new File(prefix), "C:\\Work\\Decompiler", logger, reporter);
    processor.processReport();

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

      public void flush() {
      }
    };
  }

  private SimpleLogger createFakeLogger(final StringBuilder results) {
    return new SimpleLogger() {
      public void info(@NotNull final String message) {
        results.append("INFO: ").append(message).append("\n");
      }

      public void warning(@NotNull final String message) {
        results.append("WARNING: ").append(message).append("\n");
      }

      public void error(@NotNull final String message) {
        results.append("ERROR: ").append(message).append("\n");
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

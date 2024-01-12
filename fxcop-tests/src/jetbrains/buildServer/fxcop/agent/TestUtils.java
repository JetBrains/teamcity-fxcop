

package jetbrains.buildServer.fxcop.agent;

import java.io.File;

public class TestUtils {
  public static String getTestDataPath(final String fileName) {
    File file = new File("testData/", fileName);

    if (!file.exists()) {
      file = new File("external-repos/fxcop/testData/", fileName);
    }

    return file.getAbsolutePath();
  }
}
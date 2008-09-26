package jetbrains.buildServer.fxcop.agent;

import java.io.FilenameFilter;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 26.09.2008
 * Time: 15:52:12
 */
public class ExtensionFilenameFilter implements FilenameFilter {
  private String myFilteredExtension;

  public ExtensionFilenameFilter(final String filteredExtension) {
    myFilteredExtension = filteredExtension;
  }

  public boolean accept(final File dir, final String filename) {
    return filename.toLowerCase().endsWith(myFilteredExtension.toLowerCase());
  }
}

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

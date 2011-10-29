/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.fxcop.server;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import org.jetbrains.annotations.NotNull;

public class FxCopReportTab extends ViewLogTab {
  private static final String TAB_TITLE = "FxCop";
  private static final String TAB_CODE = "fxcopReportTab";
  
  private static final String TAB_STARTPAGE = FxCopConstants.REPORT_FILE;

  public FxCopReportTab(@NotNull final PagePlaces pagePlaces,
                        @NotNull final SBuildServer server) {
    super(TAB_TITLE, TAB_CODE, pagePlaces, server);
    setIncludeUrl("/artifactsViewer.jsp");
  }

  @Override
  protected void fillModel(@NotNull final Map<String, Object> model,
                           @NotNull final HttpServletRequest request,
                           @NotNull final SBuild build) {
    model.put("basePath", "");
    model.put("startPage", TAB_STARTPAGE);
  }

  @Override
  protected boolean isAvailable(@NotNull final HttpServletRequest request, @NotNull final SBuild build) {
    if (!super.isAvailable(request, build)) return false;

    final BuildArtifact artifact = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getArtifact(TAB_STARTPAGE);
    return artifact != null && !artifact.isDirectory();
  }
}

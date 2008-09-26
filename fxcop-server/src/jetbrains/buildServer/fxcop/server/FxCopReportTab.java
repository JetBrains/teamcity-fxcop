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

package jetbrains.buildServer.fxcop.server;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.ArtifactsInfo;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 11.09.2008
 * Time: 13:08:21
 */
public class FxCopReportTab extends ViewLogTab {
  private static String TAB_TITLE = "FxCop";
  private static String TAB_CODE = "fxcopReportTab";
  
  private static String TAB_STARTPAGE = FxCopConstants.REPORT_FILE;

  public FxCopReportTab(final PagePlaces pagePlaces, final SBuildServer server) {
    super(TAB_TITLE, TAB_CODE, pagePlaces, server);
    setIncludeUrl("/artifactsViewer.jsp");
  }

  protected void fillModel(final Map model, final HttpServletRequest request, final SBuild build) {
    model.put("basePath", "");
    model.put("startPage", TAB_STARTPAGE);
  }

  public boolean isAvailable(@NotNull final HttpServletRequest request) {
    final SBuild build = getBuild(request);
    if (build == null) return false;

    final String projectId = build.getProjectId();
    if (projectId == null) return false;

    final ArtifactsInfo info = new ArtifactsInfo(build);
    return info.getSize(TAB_STARTPAGE) >= 0;
  }
}

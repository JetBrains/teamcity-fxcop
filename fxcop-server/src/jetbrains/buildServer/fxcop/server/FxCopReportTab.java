/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
import jetbrains.buildServer.fxcop.common.ArtifactsUtil;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import jetbrains.buildServer.web.reportTabs.ReportTabUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopReportTab extends ViewLogTab {
  private static final String TAB_TITLE = "FxCop";
  private static final String TAB_CODE = "fxcopReportTab";

  public FxCopReportTab(@NotNull PagePlaces pagePlaces,
                        @NotNull SBuildServer server) {
    super(TAB_TITLE, TAB_CODE, pagePlaces, server);
    setIncludeUrl("/artifactsViewer.jsp");
  }

  @Override
  protected void fillModel(@NotNull Map<String, Object> model,
                           @NotNull HttpServletRequest request,
                           @NotNull SBuild build) {
    model.put("startPage", getAvailableReportPage(build));
  }

  @Override
  protected boolean isAvailable(@NotNull HttpServletRequest request, @NotNull SBuild build) {
    return super.isAvailable(request, build) && (ReportTabUtil.isAvailable(build, ArtifactsUtil.getInternalArtifactPath(FxCopConstants.REPORT_FILE))|| ReportTabUtil.isAvailable(build, FxCopConstants.REPORT_FILE));
  }

  private String getAvailableReportPage(final SBuild build) {
    final String internalArtifactPath = ArtifactsUtil.getInternalArtifactPath(FxCopConstants.REPORT_FILE);
    return ReportTabUtil.isAvailable(build, internalArtifactPath) ? internalArtifactPath : FxCopConstants.REPORT_FILE;
  }
}

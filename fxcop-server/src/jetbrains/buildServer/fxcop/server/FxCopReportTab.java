

package jetbrains.buildServer.fxcop.server;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.fxcop.common.ArtifactsUtil;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.ArtifactsViewTab;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.ReportTabsIsolationProtection;
import jetbrains.buildServer.web.reportTabs.ReportTabUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopReportTab extends ArtifactsViewTab {
  private static final String TAB_TITLE = "FxCop";
  private static final String TAB_CODE = "fxcopReportTab";

  public FxCopReportTab(@NotNull PagePlaces pagePlaces,
                        @NotNull SBuildServer server,
                        @NotNull ReportTabsIsolationProtection reportTabsIsolationProtection) {
    super(TAB_TITLE, TAB_CODE, pagePlaces, server, reportTabsIsolationProtection);
    setIncludeUrl("/artifactsViewer.jsp");
  }

  @Override
  protected void fillModel(@NotNull Map<String, Object> model,
                           @NotNull HttpServletRequest request,
                           @NotNull SBuild build) {
    super.fillModel(model, request, build);
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
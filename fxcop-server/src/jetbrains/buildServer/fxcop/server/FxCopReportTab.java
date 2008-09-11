package jetbrains.buildServer.fxcop.server;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.ArtifactsInfo;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import static jetbrains.buildServer.web.reportTabs.ReportTabsProvider.BASE_PATH;
import static jetbrains.buildServer.web.reportTabs.ReportTabsProvider.START_PAGE;
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
  
  private static String TAB_BASEPATH = FxCopConstants.REPORT_DIR;
  private static String TAB_STARTPAGE = FxCopConstants.REPORT_FILE;

  public FxCopReportTab(final PagePlaces pagePlaces, final SBuildServer server) {
    super(TAB_TITLE, TAB_CODE, pagePlaces, server);
    setIncludeUrl("/artifactsViewer.jsp");
  }

  protected void fillModel(final Map model, final HttpServletRequest request, final SBuild build) {
    model.put(BASE_PATH, TAB_BASEPATH);
    model.put(START_PAGE, TAB_STARTPAGE);
  }

  public boolean isAvailable(@NotNull final HttpServletRequest request) {
    final SBuild build = getBuild(request);
    if (build == null) return false;

    final String projectId = build.getProjectId();
    if (projectId == null) return false;

    final ArtifactsInfo info = new ArtifactsInfo(build);
    return info.getSize(TAB_BASEPATH) >= 0 || info.getSize(TAB_BASEPATH + "/" + TAB_STARTPAGE) >= 0;
  }
}

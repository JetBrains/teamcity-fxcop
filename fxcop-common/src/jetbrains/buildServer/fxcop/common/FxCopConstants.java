package jetbrains.buildServer.fxcop.common;

import jetbrains.buildServer.agent.Constants;

/**
 * Created by IntelliJ IDEA.
 * User: Leonid.Shalupov
 * Date: 28.08.2008
 * Time: 18:30:20
 */
public interface FxCopConstants {
  String FXCOP_ROOT_PROPERTY = Constants.SYSTEM_PREFIX + "FxCopRoot";
  String RUNNER_TYPE = "FxCop";
  String FXCOPCMD_BINARY = "FxCopCmd.exe";
  String FXCOP_REPORT_XSL = "Xml/FxCopReport.xsl";

  String SETTINGS_FXCOP_ROOT = "fxcop.root";
  String SETTINGS_ADDITIONAL_OPTIONS = "fxcop.addon_options";
  String SETTINGS_FILES = "fxcop.files";
  String SETTINGS_PROJECT = "fxcop.project";

  String RUNNER_DISPLAY_NAME = "FxCop";
  String RUNNER_DESCRIPTION = "FxCop static code analysis tool runner (.NET)";
  
  String REPORT_DIR = "fxcop-html-report";
  String REPORT_FILE = "report.html";
}

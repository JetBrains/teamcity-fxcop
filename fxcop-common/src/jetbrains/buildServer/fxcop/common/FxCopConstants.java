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

  String SETTINGS_WHAT_TO_INSPECT = "fxcop.what";
  String SETTINGS_FILES = "fxcop.files";
  String SETTINGS_PROJECT = "fxcop.project";
  String SETTINGS_ADDITIONAL_OPTIONS = "fxcop.addon_options";
  String SETTINGS_REPORT_XSLT = "fxcop.report_xslt";
  String SETTINGS_FXCOP_ROOT = "fxcop.root";

  String WHAT_TO_INSPECT_FILES = "files";
  String WHAT_TO_INSPECT_PROJECT = "project";

  String RUNNER_DISPLAY_NAME = "FxCop";
  String RUNNER_DESCRIPTION = "FxCop static code analysis tool runner (.NET)";

  String OUTPUT_DIR = "05A1B22A-DE6E-49ae-AA30-DC52A074EF22";
  
  String REPORT_FILE = "fxcop-report.html";
  String OUTPUT_FILE = "fxcop-result.xml";
}

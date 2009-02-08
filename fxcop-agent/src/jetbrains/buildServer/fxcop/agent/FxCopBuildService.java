/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopBuildService extends CommandLineBuildService {
  private final ArtifactsWatcher myArtifactsWatcher;
  private InspectionReporter myInspectionReporter;

  public FxCopBuildService(ArtifactsWatcher artifactsWatcher, final InspectionReporter inspectionReporter) {
    myArtifactsWatcher = artifactsWatcher;
    myInspectionReporter = inspectionReporter;
  }

  @Override
  public void beforeProcessStarted() throws RunBuildException {
    getBuild().getBuildLogger().progressMessage("Running FxCop");

    final File outDir = getOutputDirectory();
    FileUtil.delete(outDir);
    outDir.mkdirs();
  }

  private File getOutputDirectory() {
    return new File(getBuild().getWorkingDirectory(), FxCopConstants.OUTPUT_DIR);
  }

  private File getOutputFile(String shortName) {
    return new File(getOutputDirectory(), shortName);
  }

  @Override
  public void afterProcessFinished() throws RunBuildException {
    if (!getOutputFile(FxCopConstants.OUTPUT_FILE).exists()) {
      getLogger().error("Output xml from fxcop not found");
      getLogger().buildFailureDescription("FxCop failed");
    }
    myArtifactsWatcher.addNewArtifactsPath(FxCopConstants.OUTPUT_DIR + "/*.xml");

    try {
      ImportInspectionResults();
      GenerateHtmlReport();
    } catch (Exception e) {
      getLogger().error("Exception while importing fxcop results: " + e);
      getLogger().buildFailureDescription("FxCop results import error");
    }
  }

  private void ImportInspectionResults() throws Exception {
    final String workingRoot = getBuild().getWorkingDirectory().toString();
    final Map<String, String> runParameters = getBuild().getRunnerParameters();

    getLogger().progressMessage("Importing inspection results");

    final FxCopFileProcessor fileProcessor =
      new FxCopFileProcessor(getOutputFile(FxCopConstants.OUTPUT_FILE),
                             workingRoot, getLogger(), myInspectionReporter);

    fileProcessor.processReport();

    final int errors = fileProcessor.getErrorsCount();
    final int warnings = fileProcessor.getWarningsCount();

    boolean limitReached = false;

    final Integer errorLimit = PropertiesUtil.parseInt(runParameters.get(FxCopConstants.SETTINGS_ERROR_LIMIT));
    if (errorLimit != null && errors > errorLimit) {
      getLogger().error("Errors limit reached: found " + errors + " errors, limit " + errorLimit);
      limitReached = true;
    }

    final Integer warningLimit = PropertiesUtil.parseInt(runParameters.get(FxCopConstants.SETTINGS_WARNING_LIMIT));
    if (warningLimit != null && warnings > warningLimit) {
      getLogger().error("Warnings limit reached: found " + warnings + " warnings, limit " + warningLimit);
      limitReached = true;
    }

    final String buildStatus = generateBuildStatus(errors, warnings);
    getLogger().message("##teamcity[buildStatus status='" +
                        (limitReached ? "FAILURE" : "SUCCESS") +
                        "' text='" + buildStatus + "']");
  }

  private String generateBuildStatus(int errors, int warnings) {
    return "Errors: " + errors + ", warnings: " + warnings;
  }

  private void GenerateHtmlReport() throws TransformerException {
    final String fxcopReportXslt = getBuild().getRunnerParameters().get(FxCopConstants.SETTINGS_REPORT_XSLT);
    if (StringUtil.isEmptyOrSpaces(fxcopReportXslt)) {
      getLogger().message("Skipped html report generation since not requested");
      return;
    }

    final File xsltFile = new File(fxcopReportXslt);
    if (!xsltFile.exists()) {
      getLogger().warning(xsltFile.getAbsolutePath() + " not found => won't generate html report");
      return;
    }

    getLogger().progressMessage("Generating HTML report");

    final File reportFile = getOutputFile(FxCopConstants.REPORT_FILE);

    Source xmlSource = new StreamSource(getOutputFile(FxCopConstants.OUTPUT_FILE));
    Source xsltSource = new StreamSource(xsltFile);

    TransformerFactory transformerFactory =
      TransformerFactory.newInstance();
    Transformer trans = transformerFactory.newTransformer(xsltSource);

    trans.transform(xmlSource, new StreamResult(reportFile));

    myArtifactsWatcher.addNewArtifactsPath(FxCopConstants.OUTPUT_DIR + "/*.html");
  }

  @NotNull
  @Override
  public BuildFinishedStatus getRunResult(final int exitCode) {
    if (exitCode == 0) {
      return BuildFinishedStatus.FINISHED_SUCCESS;
    }

    final EnumSet<FxCopReturnCode> errors = FxCopReturnCode.decodeReturnCode(exitCode);
    StringBuilder exitCodeStr = new StringBuilder("FxCop return code contains flags:");
    for (FxCopReturnCode rc : errors) {
      exitCodeStr.append(" ").append(rc.name());
    }

    getLogger().warning(exitCodeStr.toString());

    if (errors.contains(FxCopReturnCode.BUILD_BREAKING_MESSAGE)) {
      getLogger().buildFailureDescription("Return code contains 'Build breaking message'");
    }

    boolean fail = false;
    if (errors.contains(FxCopReturnCode.BUILD_BREAKING_MESSAGE)) {
      fail = true;
    }

    boolean failOnAnalysisErrors = isParameterEnabled(
      getBuild().getRunnerParameters(),
      FxCopConstants.SETTINGS_FAIL_ON_ANALYSIS_ERROR);
    if (failOnAnalysisErrors &&
        (errors.contains(FxCopReturnCode.ANALYSIS_ERROR) ||
         errors.contains(FxCopReturnCode.ASSEMBLY_LOAD_ERROR) ||
         errors.contains(FxCopReturnCode.ASSEMBLY_REFERENCES_ERROR) ||
         errors.contains(FxCopReturnCode.PROJECT_LOAD_ERROR) ||
         errors.contains(FxCopReturnCode.RULE_LIBRARY_LOAD_ERROR) ||
         errors.contains(FxCopReturnCode.UNKNOWN_ERROR) ||
         errors.contains(FxCopReturnCode.OUTPUT_ERROR))) {
      fail = true;
    }

    return fail
           ? BuildFinishedStatus.FINISHED_FAILED
           : BuildFinishedStatus.FINISHED_SUCCESS;
  }

  private static boolean isParameterEnabled(final Map<String, String> runParameters, final String key) {
    return runParameters.containsKey(key) && runParameters.get(key)
      .equals(Boolean.TRUE.toString());
  }

  @NotNull
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    return new SimpleProgramCommandLine(getBuild(),
                                        FxCopCommandLineBuilder.getExecutablePath(getBuild().getRunnerParameters()),
                                        FxCopCommandLineBuilder.getArguments(getBuild().getRunnerParameters()));
  }
}

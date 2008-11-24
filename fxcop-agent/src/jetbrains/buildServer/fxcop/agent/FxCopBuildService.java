package jetbrains.buildServer.fxcop.agent;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
import jetbrains.buildServer.fxcop.common.FxCopConstants;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class FxCopBuildService extends CommandLineBuildService {
  private final ArtifactsWatcher myArtifactsWatcher;
  private final FxCopDataProcessor myDataProcessor;

  public FxCopBuildService(final AgentRunningBuild build, ArtifactsWatcher artifactsWatcher, FxCopDataProcessor dataProcessor) {
    super(build);
    myArtifactsWatcher = artifactsWatcher;
    myDataProcessor = dataProcessor;
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
    getLogger().progressMessage("Importing inspection results");

    myDataProcessor.processData(getOutputFile(FxCopConstants.OUTPUT_FILE), new HashMap<String, String>());
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

    return errors.contains(FxCopReturnCode.BUILD_BREAKING_MESSAGE)
           ? BuildFinishedStatus.FINISHED_FAILED
           : BuildFinishedStatus.FINISHED_SUCCESS;
  }

  @NotNull
  public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
    return new SimpleProgramCommandLine(getBuild(),
        FxCopCommandLineBuilder.getExecutablePath(getBuild().getRunnerParameters()),
        FxCopCommandLineBuilder.getArguments(getBuild().getRunnerParameters()));
  }
}

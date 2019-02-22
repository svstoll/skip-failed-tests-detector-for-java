package ch.svenstoll.mbm.skipfailedtestdetectorforjava.model;

import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.StringUtility;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;

public final class BuildAnalysisResult {

  public static final String[] CSV_HEADERS = {
      "project",
      "branch_t1",
      "branch_t2",
      "build_id_t1",
      "build_id_t2",
      "trigger_commit_t1",
      "trigger_commit_t2",
      "num_test_methods_extracted_t1",
      "num_test_methods_extracted_t2",
      "delta_run_vs_extracted_t1",
      "delta_run_vs_extracted_t2",
      "num_test_methods_not_in_t1",
      "num_test_methods_not_in_t2",
      "test_methods_not_in_t2",
      "delta_tests_run",
      "delta_tests_ok",
      "delta_tests_failed",
      "delta_tests_skipped",
      "legacy_smell_warning",
      "num_failed_tests_not_extracted",
      "num_removed_failed_tests",
      "removed_failed_tests",
      "analysis_ok"};

  private final String project;
  private final String branchT1;
  private final String branchT2;
  private final Long buildIdT1;
  private final long buildIdT2;
  private final String triggerCommitT1;
  private final String triggerCommitT2;
  private final Integer numTestMethodsExtractedT1;
  private final Integer numTestMethodsExtractedT2;
  private final Integer deltaRunVsExtractedT1;
  private final Integer deltaRunVsExtractedT2;
  private final Integer numTestMethodsNotInT1;
  private final Integer numTestMethodsNotInT2;
  private final String testMethodsNotInT2;
  private final Integer deltaTestsRun;
  private final Integer deltaTestsOk;
  private final Integer deltaTestsFailed;
  private final Integer deltaTestsSkipped;
  private final Boolean legacySmellWarning;
  private final Integer numFailedMethodsNotExtracted;
  private final Integer numRemovedFailedTests;
  private final String removedFailedTests;
  private final boolean analysisOk;

  private BuildAnalysisResult(String project,
                             String branchT1,
                             String branchT2,
                             Long buildIdT1,
                             long buildIdT2,
                             String triggerCommitT1,
                             String triggerCommitT2,
                             Integer numTestMethodsExtractedT1,
                             Integer numTestMethodsExtractedT2,
                             Integer deltaRunVsExtractedT1,
                             Integer deltaRunVsExtractedT2,
                             Integer numTestMethodsNotInT1,
                             Integer numTestMethodsNotInT2,
                             String testMethodsNotInT2,
                             Integer deltaTestsRun,
                             Integer deltaTestsOk,
                             Integer deltaTestsFailed,
                             Integer deltaTestsSkipped,
                             Boolean legacySmellWarning,
                             Integer numFailedMethodsNotExtracted,
                             Integer numRemovedFailedTests,
                             String removedFailedTests,
                             boolean analysisOk) {
    if (StringUtility.isNullOrEmpty(project) || StringUtility.isNullOrEmpty(branchT2)) {
      throw new IllegalArgumentException("Project name and branch of follow-up build must not be null or empty.");
    }

    this.project = project;
    this.branchT1 = branchT1;
    this.branchT2 = branchT2;
    this.buildIdT1 = buildIdT1;
    this.buildIdT2 = buildIdT2;
    this.triggerCommitT1 = triggerCommitT1;
    this.triggerCommitT2 = triggerCommitT2;
    this.numTestMethodsExtractedT1 = numTestMethodsExtractedT1;
    this.numTestMethodsExtractedT2 = numTestMethodsExtractedT2;
    this.deltaRunVsExtractedT1 = deltaRunVsExtractedT1;
    this.deltaRunVsExtractedT2 = deltaRunVsExtractedT2;
    this.numTestMethodsNotInT1 = numTestMethodsNotInT1;
    this.numTestMethodsNotInT2 = numTestMethodsNotInT2;
    this.testMethodsNotInT2 = testMethodsNotInT2;
    this.deltaTestsRun = deltaTestsRun;
    this.deltaTestsOk = deltaTestsOk;
    this.deltaTestsFailed = deltaTestsFailed;
    this.deltaTestsSkipped = deltaTestsSkipped;
    this.legacySmellWarning = legacySmellWarning;
    this.numFailedMethodsNotExtracted = numFailedMethodsNotExtracted;
    this.numRemovedFailedTests = numRemovedFailedTests;
    this.removedFailedTests = removedFailedTests;
    this.analysisOk = analysisOk;
  }

  public void printToCsv(CSVPrinter csvPrinter) throws IOException {
    csvPrinter.printRecord(
        getProject(),
        getBranchT1(),
        getBranchT2(),
        getBuildIdT1(),
        getBuildIdT2(),
        getTriggerCommitT1(),
        getTriggerCommitT2(),
        getNumTestMethodsExtractedT1(),
        getNumTestMethodsExtractedT2(),
        getDeltaRunVsExtractedT1(),
        getDeltaRunVsExtractedT2(),
        getNumTestMethodsNotInT1(),
        getNumTestMethodsNotInT2(),
        getTestMethodsNotInT2(),
        getDeltaTestsRun(),
        getDeltaTestsOk(),
        getDeltaTestsFailed(),
        getDeltaTestsSkipped(),
        getLegacySmellWarning(),
        getNumFailedMethodsNotExtracted(),
        getNumRemovedFailedTests(),
        getRemovedFailedTests(),
        isAnalysisOk());
  }

  public String getProject() {
    return project;
  }

  public String getBranchT1() {
    return branchT1;
  }

  public String getBranchT2() {
    return branchT2;
  }

  public Long getBuildIdT1() {
    return buildIdT1;
  }

  public Long getBuildIdT2() {
    return buildIdT2;
  }

  public String getTriggerCommitT1() {
    return triggerCommitT1;
  }

  public String getTriggerCommitT2() {
    return triggerCommitT2;
  }

  public Integer getNumTestMethodsExtractedT1() {
    return numTestMethodsExtractedT1;
  }

  public Integer getNumTestMethodsExtractedT2() {
    return numTestMethodsExtractedT2;
  }

  public Integer getDeltaRunVsExtractedT1() {
    return deltaRunVsExtractedT1;
  }

  public Integer getDeltaRunVsExtractedT2() {
    return deltaRunVsExtractedT2;
  }

  public Integer getNumTestMethodsNotInT1() {
    return numTestMethodsNotInT1;
  }

  public Integer getNumTestMethodsNotInT2() {
    return numTestMethodsNotInT2;
  }

  public String getTestMethodsNotInT2() {
    return testMethodsNotInT2;
  }

  public Integer getDeltaTestsRun() {
    return deltaTestsRun;
  }

  public Integer getDeltaTestsOk() {
    return deltaTestsOk;
  }

  public Integer getDeltaTestsFailed() {
    return deltaTestsFailed;
  }

  public Integer getDeltaTestsSkipped() {
    return deltaTestsSkipped;
  }

  public Boolean getLegacySmellWarning() {
    return legacySmellWarning;
  }

  public Integer getNumFailedMethodsNotExtracted() {
    return numFailedMethodsNotExtracted;
  }

  public Integer getNumRemovedFailedTests() {
    return numRemovedFailedTests;
  }

  public String getRemovedFailedTests() {
    return removedFailedTests;
  }

  public boolean isAnalysisOk() {
    return analysisOk;
  }

  public static final class BuildAnalysisBuilder implements AnalysisOkBuildAnalysisBuilder,
      ProjectBuildAnalysisBuilder, BranchBuildAnalysisBuilder, BuildIdT2BuildAnalysisBuilder,
      TriggerCommitT2BuildAnalysisBuilder, FinalBuildAnalysisBuilder {

    private String project;
    private String branchT1;
    private String branchT2;
    private Long buildIdT1 = null;
    private long buildIdT2;
    private Long jobIdT1 = null;
    private long jobIdT2;
    private String triggerCommitT1;
    private String triggerCommitT2 = null;
    private Integer numTestMethodsExtractedT1 = null;
    private Integer numTestMethodsExtractedT2 = null;
    private Integer deltaRunVsExtractedT1 = null;
    private Integer deltaRunVsExtractedT2 = null;
    private Integer numTestMethodsNotInT1 = null;
    private Integer numTestMethodsNotInT2 = null;
    private String testMethodsNotInT2 = null;
    private Integer deltaTestsRun = null;
    private Integer deltaTestsOk = null;
    private Integer deltaTestsFailed = null;
    private Integer deltaTestsSkipped = null;
    private Boolean legacySmellWarning = null;
    private Integer numFailedMethodsNotExtracted;
    private Integer numRemovedFailedTests = null;
    private String removedFailedTests = null;
    private boolean analysisOk;

    private BuildAnalysisBuilder() {
    }

    public static AnalysisOkBuildAnalysisBuilder aBuildAnalysisResult() {
      return new BuildAnalysisBuilder();
    }

    @Override
    public ProjectBuildAnalysisBuilder withAnalysisOk(boolean analysisOk) {
      this.analysisOk = analysisOk;
      return this;
    }

    @Override
    public BranchBuildAnalysisBuilder withProject(String project) {
      this.project = project;
      return this;
    }

    @Override
    public BuildIdT2BuildAnalysisBuilder withBranchT2(String branchT2) {
      this.branchT2 = branchT2;
      return this;
    }

    @Override
    public TriggerCommitT2BuildAnalysisBuilder withBuildIdT2(long buildIdT2) {
      this.buildIdT2 = buildIdT2;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder withTriggerCommitT2(String triggerCommitT2) {
      this.triggerCommitT2 = triggerCommitT2;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andBranchT1(String branchT1) {
      this.branchT1 = branchT1;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andBuildIdT1(Long buildIdT1) {
      this.buildIdT1 = buildIdT1;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andJobIdT1(Long jobIdT1) {
      this.jobIdT1 = jobIdT1;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andTriggerCommitT1(String triggerCommitT1) {
      this.triggerCommitT1 = triggerCommitT1;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andNumTestMethodsExtractedT1(Integer numTestMethodsExtractedT1) {
      this.numTestMethodsExtractedT1 = numTestMethodsExtractedT1;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andNumTestMethodsExtractedT2(Integer numTestMethodsExtractedT2) {
      this.numTestMethodsExtractedT2 = numTestMethodsExtractedT2;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andDeltaRunVsExtractedT1(Integer deltaRunVsExtractedT1) {
      this.deltaRunVsExtractedT1 = deltaRunVsExtractedT1;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andDeltaRunVsExtractedT2(Integer deltaRunVsExtractedT2) {
      this.deltaRunVsExtractedT2 = deltaRunVsExtractedT2;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andNumTestMethodsNotInT1(Integer numTestMethodsNotInT1) {
      this.numTestMethodsNotInT1 = numTestMethodsNotInT1;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andNumTestMethodsNotInT2(Integer numTestMethodsNotInT2) {
      this.numTestMethodsNotInT2 = numTestMethodsNotInT2;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andTestMethodsNotInT2(String testMethodsNotInT2) {
      this.testMethodsNotInT2 = testMethodsNotInT2;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andDeltaTestsRun(Integer deltaTestsRun) {
      this.deltaTestsRun = deltaTestsRun;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andDeltaTestsOk(Integer deltaTestsOk) {
      this.deltaTestsOk = deltaTestsOk;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andDeltaTestsFailed(Integer deltaTestsFailed) {
      this.deltaTestsFailed = deltaTestsFailed;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andDeltaTestsSkipped(Integer deltaTestsSkipped) {
      this.deltaTestsSkipped = deltaTestsSkipped;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andLegacySmellWarning(Boolean legacySmellWarning) {
      this.legacySmellWarning = legacySmellWarning;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andNumFailedMethodsNotExtracted(Integer numFailedMethodsNotExtracted) {
      this.numFailedMethodsNotExtracted = numFailedMethodsNotExtracted;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andNumRemovedFailedTests(Integer numRemovedFailedTests) {
      this.numRemovedFailedTests = numRemovedFailedTests;
      return this;
    }

    @Override
    public FinalBuildAnalysisBuilder andRemovedFailedTests(String removedFailedTests) {
      this.removedFailedTests = removedFailedTests;
      return this;
    }

    @Override
    public BuildAnalysisResult create() {
      return new BuildAnalysisResult(
          project,
          branchT1,
          branchT2,
          buildIdT1,
          buildIdT2,
          triggerCommitT1,
          triggerCommitT2,
          numTestMethodsExtractedT1,
          numTestMethodsExtractedT2,
          deltaRunVsExtractedT1,
          deltaRunVsExtractedT2,
          numTestMethodsNotInT1,
          numTestMethodsNotInT2,
          testMethodsNotInT2,
          deltaTestsRun,
          deltaTestsOk,
          deltaTestsFailed,
          deltaTestsSkipped,
          legacySmellWarning,
          numFailedMethodsNotExtracted,
          numRemovedFailedTests,
          removedFailedTests,
          analysisOk);
    }
  }

  public interface AnalysisOkBuildAnalysisBuilder {
    ProjectBuildAnalysisBuilder withAnalysisOk(boolean analysisOk);
  }

  public interface ProjectBuildAnalysisBuilder {
    BranchBuildAnalysisBuilder withProject(String project);
  }

  public interface BranchBuildAnalysisBuilder {
    BuildIdT2BuildAnalysisBuilder withBranchT2(String branchT2);
  }

  public interface BuildIdT2BuildAnalysisBuilder {
    TriggerCommitT2BuildAnalysisBuilder withBuildIdT2(long buildIdT2);
  }

  public interface TriggerCommitT2BuildAnalysisBuilder {
    FinalBuildAnalysisBuilder withTriggerCommitT2(String triggerCommitT2);
  }

  public interface FinalBuildAnalysisBuilder {

    FinalBuildAnalysisBuilder andBranchT1(String branchT1);

    FinalBuildAnalysisBuilder andBuildIdT1(Long buildIdT1);

    FinalBuildAnalysisBuilder andJobIdT1(Long jobIdT1);

    FinalBuildAnalysisBuilder andTriggerCommitT1(String triggerCommitT1);

    FinalBuildAnalysisBuilder andNumTestMethodsExtractedT1(Integer numTestMethodsExtractedT1);

    FinalBuildAnalysisBuilder andNumTestMethodsExtractedT2(Integer numTestMethodsExtractedT2);

    FinalBuildAnalysisBuilder andDeltaRunVsExtractedT1(Integer deltaRunVsExtractedT1);

    FinalBuildAnalysisBuilder andDeltaRunVsExtractedT2(Integer deltaRunVsExtractedT2);

    FinalBuildAnalysisBuilder andNumTestMethodsNotInT1(Integer numTestMethodsNotInT1);

    FinalBuildAnalysisBuilder andNumTestMethodsNotInT2(Integer numTestMethodsNotInT2);

    FinalBuildAnalysisBuilder andTestMethodsNotInT2(String testMethodsNotInT2);

    FinalBuildAnalysisBuilder andDeltaTestsRun(Integer deltaTestsRun);

    FinalBuildAnalysisBuilder andDeltaTestsOk(Integer deltaTestsOk);

    FinalBuildAnalysisBuilder andDeltaTestsFailed(Integer deltaTestsFailed);

    FinalBuildAnalysisBuilder andDeltaTestsSkipped(Integer deltaTestsSkipped);

    FinalBuildAnalysisBuilder andLegacySmellWarning(Boolean legacySmellWarning);

    FinalBuildAnalysisBuilder andNumFailedMethodsNotExtracted(Integer numFailedMethodsNotExtracted);

    FinalBuildAnalysisBuilder andNumRemovedFailedTests(Integer numRemovedFailedTests);

    FinalBuildAnalysisBuilder andRemovedFailedTests(String removedFailedTests);

    BuildAnalysisResult create();
  }
}

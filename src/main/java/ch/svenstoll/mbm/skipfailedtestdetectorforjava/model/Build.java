package ch.svenstoll.mbm.skipfailedtestdetectorforjava.model;

import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.StringUtility;

import java.util.*;

public class Build {

  private final ProjectBranchKey projectBranchKey;
  private final long buildId;
  private final long jobId;
  private final Long prevBuildId;
  private final String triggerCommit;
  private final String status;
  private final Integer numTestsRun;
  private final Integer numTestsOk;
  private final Integer numTestsFailed;
  private final Integer numTestsSkipped;
  private final List<String> failedMethods;
  private Map<BasicClassData, List<BasicMethodData>> methodsByClass;
  private Boolean extractionSuccessful;

  private Build(
      ProjectBranchKey projectBranchKey,
      long buildId,
      long jobId,
      Long prevBuildId,
      String triggerCommit,
      String status,
      Integer numTestsRun,
      Integer numTestsOk,
      Integer numTestsFailed,
      Integer numTestsSkipped,
      List<String> failedMethods,
      Map<BasicClassData,List<BasicMethodData>> methodsByClass,
      Boolean extractionSuccessful) {
    if (projectBranchKey == null) {
      throw new IllegalArgumentException("The projectBranchKey must not be null.");
    }
    if (StringUtility.isNullOrEmpty(triggerCommit)) {
      throw new IllegalArgumentException("The triggerCommit must not be null or empty.");
    }

    this.projectBranchKey = projectBranchKey;
    this.buildId = buildId;
    this.jobId = jobId;
    this.prevBuildId = prevBuildId;
    this.triggerCommit = triggerCommit;
    this.status = status;
    this.numTestsRun = numTestsRun;
    this.numTestsOk = numTestsOk;
    this.numTestsFailed = numTestsFailed;
    this.numTestsSkipped = numTestsSkipped;
    this.failedMethods = failedMethods;
    this.methodsByClass = methodsByClass;
    this.extractionSuccessful = extractionSuccessful;
  }

  /**
   * Constructor for a deep copy.
   *
   * @param build The object to copy.
   */
  public Build(Build build) {
    String project = build.getProjectBranchKey().getProjectName();
    String branch = build.getProjectBranchKey().getBranch();

    List<String> failedMethodsCopy = null;
    if (build.getFailedMethods() != null) {
      failedMethodsCopy = new ArrayList<>(build.getFailedMethods());
    }

    this.projectBranchKey = new ProjectBranchKey(project, branch);
    this.buildId = build.buildId;
    this.jobId = build.jobId;
    this.prevBuildId = build.prevBuildId;
    this.triggerCommit = build.triggerCommit;
    this.status = build.status;
    this.numTestsRun = build.numTestsRun;
    this.numTestsOk = build.numTestsOk;
    this.numTestsFailed = build.numTestsFailed;
    this.numTestsSkipped = build.numTestsSkipped;
    this.failedMethods = failedMethodsCopy;
    this.methodsByClass = copyMethodsByClassFromBuild(build);
    this.extractionSuccessful = build.extractionSuccessful;
  }

  private Map<BasicClassData, List<BasicMethodData>> copyMethodsByClassFromBuild(Build build) {
    if (build.getMethodsByClass() == null) {
      return null;
    }

    Map<BasicClassData, List<BasicMethodData>> methodsByClassCopy = new HashMap<>();
    for (Map.Entry<BasicClassData, List<BasicMethodData>> entry : build.getMethodsByClass().entrySet()) {
      BasicClassData keyCopy = null;
      if (entry.getKey() != null) {
        keyCopy = new BasicClassData(entry.getKey());
      }

      List<BasicMethodData> valueCopy = null;
      if (entry.getValue() != null) {
        valueCopy = new ArrayList<>();
        for (BasicMethodData basicMethodData : entry.getValue()) {
          valueCopy.add(new BasicMethodData(basicMethodData));
        }
      }

      methodsByClassCopy.put(keyCopy, valueCopy);
    }

    return methodsByClassCopy;
  }

  public ProjectBranchKey getProjectBranchKey() {
    return projectBranchKey;
  }

  public long getBuildId() {
    return buildId;
  }

  public long getJobId() {
    return jobId;
  }

  public Long getPrevBuildId() {
    return prevBuildId;
  }

  public String getTriggerCommit() {
    return triggerCommit;
  }

  public String getStatus() {
    return status;
  }

  public Integer getNumTestsRun() {
    return numTestsRun;
  }

  public Integer getNumTestsOk() {
    return numTestsOk;
  }

  public Integer getNumTestsFailed() {
    return numTestsFailed;
  }

  public Integer getNumTestsSkipped() {
    return numTestsSkipped;
  }

  public Boolean getExtractionSuccessful() {
    return extractionSuccessful;
  }

  public void setExtractionSuccessful(Boolean extractionSuccessful) {
    this.extractionSuccessful = extractionSuccessful;
  }

  public List<String> getFailedMethods() {
    return failedMethods;
  }

  public Map<BasicClassData, List<BasicMethodData>> getMethodsByClass() {
    return methodsByClass;
  }

  public void setMethodsByClass(Map<BasicClassData, List<BasicMethodData>> methodsByClass) {
    this.methodsByClass = methodsByClass;
  }

  @Override
  public String toString() {
    return projectBranchKey.toString() + "#" + getBuildId();
  }

  public static class BuildBuilder implements ProjectBranchBuildBuilder, BuildIdBuildBuilder,
      JobIdBuildBuilder, PrevBuildIdBuildBuilder, TriggerCommitBuildBuilder, FinalBuildBuilder {

    private ProjectBranchKey projectBranchKey;
    private long buildId;
    private long jobId;
    private Long prevBuildId;
    private String triggerCommit;
    private String status = null;
    private Integer numTestsRun = null;
    private Integer numTestsOk = null;
    private Integer numTestsFailed = null;
    private Integer numTestsSkipped = null;
    private List<String> failedMethods = null;
    private Map<BasicClassData, List<BasicMethodData>> methodsByClass = null;
    private Boolean extractionSuccessful = null;

    private BuildBuilder() {
    }

    public static ProjectBranchBuildBuilder aBuild() {
      return new BuildBuilder();
    }

    @Override
    public BuildIdBuildBuilder withProjectBranch(ProjectBranchKey projectBranchKey) {
      this.projectBranchKey = projectBranchKey;
      return this;
    }

    @Override
    public JobIdBuildBuilder withBuildId(long buildId) {
      this.buildId = buildId;
      return this;
    }

    @Override
    public PrevBuildIdBuildBuilder withJobId(long jobId) {
      this.jobId = jobId;
      return this;
    }

    @Override
    public TriggerCommitBuildBuilder withPrevBuildId(Long prevBuildId) {
      this.prevBuildId = prevBuildId;
      return this;
    }

    @Override
    public FinalBuildBuilder withTriggerCommit(String triggerCommit) {
      this.triggerCommit = triggerCommit;
      return this;
    }

    @Override
    public FinalBuildBuilder andStatus(String status) {
      this.status = status;
      return this;
    }

    @Override
    public FinalBuildBuilder andNumTestsRun(Integer numTestsRun) {
      this.numTestsRun = numTestsRun;
      return this;
    }

    @Override
    public FinalBuildBuilder andNumTestsOk(Integer numTestsOk) {
      this.numTestsOk = numTestsOk;
      return this;
    }

    @Override
    public FinalBuildBuilder andNumTestsFailed(Integer numTestsFailed) {
      this.numTestsFailed = numTestsFailed;
      return this;
    }

    @Override
    public FinalBuildBuilder andNumTestsSkipped(Integer numTestsSkipped) {
      this.numTestsSkipped = numTestsSkipped;
      return this;
    }

    @Override
    public FinalBuildBuilder andFailedMethods(List<String> failedMethods) {
      this.failedMethods = failedMethods;
      return this;
    }

    @Override
    public FinalBuildBuilder andMethodsByClass(Map<BasicClassData, List<BasicMethodData>> methodsByClass) {
      this.methodsByClass = methodsByClass;
      return this;
    }

    @Override
    public FinalBuildBuilder andExtractionSuccessful(Boolean extractionSuccessful) {
      this.extractionSuccessful = extractionSuccessful;
      return this;
    }

    @Override
    public Build create() {
      return new Build(
          projectBranchKey,
          buildId,
          jobId,
          prevBuildId,
          triggerCommit,
          status,
          numTestsRun,
          numTestsOk,
          numTestsFailed,
          numTestsSkipped,
          failedMethods,
          methodsByClass,
          extractionSuccessful);
    }
  }

  public interface ProjectBranchBuildBuilder {
    BuildIdBuildBuilder withProjectBranch(ProjectBranchKey projectBranchKey);
  }

  public interface BuildIdBuildBuilder {
    JobIdBuildBuilder withBuildId(long buildId);
  }

  public interface JobIdBuildBuilder {
    PrevBuildIdBuildBuilder withJobId(long buildNumber);
  }

  public interface PrevBuildIdBuildBuilder {
    TriggerCommitBuildBuilder withPrevBuildId(Long prevBuildId);
  }

  public interface TriggerCommitBuildBuilder {
    FinalBuildBuilder withTriggerCommit(String triggerCommit);
  }

  public interface FinalBuildBuilder {
    FinalBuildBuilder andStatus(String status);

    FinalBuildBuilder andNumTestsRun(Integer numTestsRun);

    FinalBuildBuilder andNumTestsOk(Integer numTestsOk);

    FinalBuildBuilder andNumTestsFailed(Integer numTestsFailed);

    FinalBuildBuilder andNumTestsSkipped(Integer numTestsSkipped);

    FinalBuildBuilder andFailedMethods(List<String> failedMethods);

    FinalBuildBuilder andMethodsByClass(Map<BasicClassData, List<BasicMethodData>> methodsByClass);

    FinalBuildBuilder andExtractionSuccessful(Boolean extractionSuccessful);

    Build create();
  }
}

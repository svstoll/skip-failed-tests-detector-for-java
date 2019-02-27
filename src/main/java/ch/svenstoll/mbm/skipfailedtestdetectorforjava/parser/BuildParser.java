package ch.svenstoll.mbm.skipfailedtestdetectorforjava.parser;

import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.Build;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.ProjectBranchKey;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.CollectionUtility;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.NumberUtility;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.StringUtility;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class BuildParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildParser.class);
  private static final String[] HEADERS = {
      "gh_project_name",
      "git_branch",
      "tr_build_id",
      "tr_prev_build",
      "git_trigger_commit",
      "tr_status",
      "tr_log_num_tests_run",
      "tr_log_num_tests_ok",
      "tr_log_num_tests_skipped",
      "tr_log_num_tests_failed",
      "tr_log_tests_failed"};

  public List<Build> parseBuildsFile(String buildsFilePath) {
    List<Build> builds = new ArrayList<>();
    Set<String> involvedProjects = new HashSet<>();
    Set<String> projectsWithExtractionErrors = new HashSet<>();
    try (Reader reader = Files.newBufferedReader(Paths.get(buildsFilePath))) {
      CSVFormat csvFormat = CSVFormat.DEFAULT
          .withHeader(HEADERS)
          .withSkipHeaderRecord()
          .withIgnoreEmptyLines()
          .withIgnoreSurroundingSpaces();
      CSVParser csvParser = new CSVParser(reader, csvFormat);

      for (CSVRecord record : csvParser.getRecords()) {
        Build build = transformRecordToBuild(record);

        String project = build.getProjectBranchKey().getProjectName();
        involvedProjects.add(project);
        if (hasExtractionErrors(build)) {
          projectsWithExtractionErrors.add(project);
        }
        else {
          builds.add(build);
        }
      }
    } catch (Exception e) {
      LOGGER.error("Failed to parse the input file.", e);
    }

    List<Build> validBuilds = builds.stream()
        .filter(build -> !projectsWithExtractionErrors.contains(build.getProjectBranchKey().getProjectName()))
        .collect(Collectors.toList());
    if (projectsWithExtractionErrors.size() >= 1) {
      int numRemovedBuilds = builds.size() - validBuilds.size();
      LOGGER.warn("{} projects contain invalid failed methods. All builds ({}) from these " +
          "projects will be excluded.", projectsWithExtractionErrors.size(), numRemovedBuilds);
    }

    long numOfBuildsWithFailedTests = validBuilds.stream()
        .filter(build -> build.getNumTestsFailed() != null && build.getNumTestsFailed() > 0)
        .count();
    LOGGER.info("Parsed {} builds from {} projects. {} of these builds have at least 1 failed test.",
        validBuilds.size(),
        involvedProjects.size() - projectsWithExtractionErrors.size(),
        numOfBuildsWithFailedTests);

    return validBuilds;
  }

  public Build transformRecordToBuild(CSVRecord csvRecord) {
    String project = csvRecord.get("gh_project_name");
    String branch = csvRecord.get("git_branch");
    String buildId = csvRecord.get("tr_build_id");
    String prevBuildId = csvRecord.get("tr_prev_build");
    String triggerCommit = csvRecord.get("git_trigger_commit");
    String status = csvRecord.get("tr_status");
    String numTestsRun = csvRecord.get("tr_log_num_tests_run");
    String numTestsOk = csvRecord.get("tr_log_num_tests_ok");
    String numTestsFailed = csvRecord.get("tr_log_num_tests_failed");
    String numTestsSkipped = csvRecord.get("tr_log_num_tests_skipped");
    String failedMethods = csvRecord.get("tr_log_tests_failed");

    return Build.BuildBuilder.aBuild()
        .withProjectBranch(new ProjectBranchKey(project, branch))
        .withBuildId(Long.parseLong(buildId))
        .withPrevBuildId(NumberUtility.parseLongSafely(prevBuildId))
        .withTriggerCommit(triggerCommit)
        .andStatus(status)
        .andNumTestsRun(NumberUtility.parseIntegerSafely(numTestsRun))
        .andNumTestsOk(NumberUtility.parseIntegerSafely(numTestsOk))
        .andNumTestsFailed(NumberUtility.parseIntegerSafely(numTestsFailed))
        .andNumTestsSkipped(NumberUtility.parseIntegerSafely(numTestsSkipped))
        .andFailedMethods((StringUtility.fromConcatenatedStringsToList(failedMethods, "#")))
        .create();
  }

  private boolean hasExtractionErrors(Build build) {
    if (CollectionUtility.isNullOrEmpty(build.getFailedMethods())) {
      return false;
    }

    boolean hasExtractionErrors = false;
    for (String method : build.getFailedMethods()) {
      if (method.isEmpty() || !Character.isLowerCase(method.charAt(0)) || method.contains(" ")) {
        hasExtractionErrors = true;
      }
    }
    return hasExtractionErrors;
  }
}

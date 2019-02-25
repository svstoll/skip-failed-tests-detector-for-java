package ch.svenstoll.mbm.skipfailedtestdetectorforjava.comparator;

import ch.svenstoll.mbm.skipfailedtestdetectorforjava.extractor.MethodExtractor;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.Build;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.BuildAnalysisResult;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.BasicMethodData;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.BooleanUtility;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.CollectionUtility;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.NumberUtility;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.StringUtility;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.BooleanUtility.nvl;

public class BuildComparator {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildComparator.class);

  private final String outputFolderPath;

  public BuildComparator(String outputFolderPath) {
    if (StringUtility.isNullOrEmpty(outputFolderPath)) {
      throw new IllegalArgumentException("The outputFolderPath must not be null or empty.");
    }
    this.outputFolderPath = outputFolderPath;
  }

  public void compareBuilds(List<Build> builds) {
    String allResultsFilePath = outputFolderPath + "/Results/all_results.csv";
    String smellResultsFilePath = outputFolderPath + "/Results/smell_results.csv";
    CSVFormat allResultsCsvFormat = initializeResultFilePathAndCsvFormat(allResultsFilePath);
    CSVFormat smellResultsCsvFormat = initializeResultFilePathAndCsvFormat(smellResultsFilePath);
    MethodExtractor extractor = new MethodExtractor(outputFolderPath);

    try (FileWriter allResultsOut = new FileWriter(allResultsFilePath, true);
         FileWriter smellResultsOut = new FileWriter(smellResultsFilePath, true);
         CSVPrinter allResultsPrinter = new CSVPrinter(allResultsOut, allResultsCsvFormat);
         CSVPrinter smellResultsPrinter = new CSVPrinter(smellResultsOut, smellResultsCsvFormat)) {
      prepareBuildComparison(builds);
      Map<Long, Build> buildsByBuildId = generateBuildByBuildIdMap(builds);

      Build lastAnalyzedBuild = null; // Used as a simple cache to prevent unnecessary extractions.
      for (Build buildT2 : builds) {
        Build buildT1 = null;
        if (lastAnalyzedBuild != null && buildT2.getPrevBuildId() != null) {
          buildT1 = lastAnalyzedBuild.getBuildId() == buildT2.getPrevBuildId() ? lastAnalyzedBuild : null;
        }
        if (buildT1 == null) {
          buildT1 = buildsByBuildId.get(buildT2.getPrevBuildId());
        }

        // Creating deep copies of the builds is essential here. Otherwise, the memory usage of the
        // build list will keep on growing which can cause an out of memory exception if the there
        // are thousands of builds to compare.
        Build buildT1Copy = buildT1 != null ? new Build(buildT1) : null;
        Build buildT2Copy = new Build(buildT2);

        extractor.extractMethodsForBuild(buildT1Copy);
        extractor.extractMethodsForBuild(buildT2Copy);

        compareConsecutiveBuilds(buildT1Copy, buildT2Copy, allResultsPrinter, smellResultsPrinter);
        lastAnalyzedBuild = buildT2Copy;

        allResultsPrinter.flush();
        smellResultsPrinter.flush();
      }
    }
    catch (IOException e) {
      LOGGER.error("Could not write build comparison to file.", e);
    }
  }

  private CSVFormat initializeResultFilePathAndCsvFormat(String filePath) {
    CSVFormat csvFormat = CSVFormat.DEFAULT.withNullString("null");
    if (!Files.exists(Paths.get(filePath))) {
      csvFormat = csvFormat.withHeader(BuildAnalysisResult.CSV_HEADERS);

      if (!Files.exists(Paths.get(filePath).getParent())) {
        File resultFolder = new File(Paths.get(filePath).getParent().toString());
        resultFolder.mkdirs();
      }
    }
    return csvFormat;
  }

  private void prepareBuildComparison(List<Build> builds) {
    // Sort by project, branch, build ID and job ID
    builds.sort((build1, build2) -> {
      String projectBranch1 = build1.getProjectBranchKey().toString();
      String projectBranch2 = build2.getProjectBranchKey().toString();
      int projectBranchComparison = projectBranch1.compareTo(projectBranch2);
      if (projectBranchComparison != 0) {
        return projectBranchComparison;
      }

      long buildId1 = build1.getBuildId();
      long buildId2 = build2.getBuildId();
      return Long.compare(buildId1, buildId2);
    });
  }

  private Map<Long, Build> generateBuildByBuildIdMap(List<Build> builds) {
    Map<Long, Build> buildsByBuildId = new HashMap<>();
    for (Build build : builds) {
      Build prev = buildsByBuildId.put(build.getBuildId(), build);
      if (prev != null) {
        throw new IllegalStateException("Build IDs are not unique.");
      }
    }
    return buildsByBuildId;
  }

  private void compareConsecutiveBuilds(Build buildT1, Build buildT2, CSVPrinter allResultsPrinter, CSVPrinter smellResultsPrinter) throws IOException {
    if (buildT2 == null) {
      throw new IllegalArgumentException("The follow-up build must not be null.");
    }

    if (buildT1 == null) {
      LOGGER.warn(buildT2.toString() + " does not have a previous build.");
      String project = buildT2.getProjectBranchKey().getProjectName();
      String branch = buildT2.getProjectBranchKey().getBranch();
      BuildAnalysisResult.BuildAnalysisBuilder.aBuildAnalysisResult()
          .withAnalysisOk(false)
          .withProject(project)
          .withBranchT2(branch)
          .withBuildIdT2(buildT2.getBuildId())
          .withTriggerCommitT2(buildT2.getTriggerCommit())
          .create()
          .printToCsv(allResultsPrinter);
      return;
    }

    BuildAnalysisResult result = calculateBuildAnalysisResult(buildT1, buildT2);
    result.printToCsv(allResultsPrinter);

    if (result.isAnalysisOk() && (BooleanUtility.nvl(result.getLegacySmellWarning())
        || (result.getNumRemovedFailedTests() != null && result.getNumRemovedFailedTests() >= 1))) {
      result.printToCsv(smellResultsPrinter);
    }
  }

  private BuildAnalysisResult calculateBuildAnalysisResult(Build buildT1, Build buildT2) {
    Set<BasicMethodData> allMethodsT1 = collectAllMethodsForBuild(buildT1);
    Set<BasicMethodData> allMethodsT2 = collectAllMethodsForBuild(buildT2);

    Set<BasicMethodData> testMethodsT1 = allMethodsT1.stream()
        .filter(BasicMethodData::isTestMethod).collect(Collectors.toSet());
    Set<BasicMethodData> testMethodsT2 = allMethodsT2.stream()
        .filter(BasicMethodData::isTestMethod).collect(Collectors.toSet());

    Integer deltaRunVsExtractedT1 = NumberUtility.calculateDelta(buildT1.getNumTestsRun(), testMethodsT1.size());
    Integer deltaRunVsExtractedT2 = NumberUtility.calculateDelta(buildT2.getNumTestsRun(), testMethodsT2.size());

    Map<BasicMethodData, Boolean> testMethodsNotInT1 = calculateTestMethodsInAButNotInB(testMethodsT2, testMethodsT1, allMethodsT1);
    Map<BasicMethodData, Boolean> testMethodsNotInT2 = calculateTestMethodsInAButNotInB(testMethodsT1, testMethodsT2, allMethodsT2);

    String concatenatedMethodsNotInT2 = StringUtility.concatStrings(testMethodsNotInT2.keySet().stream()
        .map(BasicMethodData::getQualifiedName)
        .collect(Collectors.toList()), "#");

    Integer deltaNumTestsRun = calculateDeltaNumTestsRun(buildT1, buildT2);
    Integer deltaNumTestsOk = calculateDeltaNumTestsOk(buildT1, buildT2);
    Integer deltaNumTestsFailed = calculateDeltaNumTestsFailed(buildT1, buildT2);
    Integer deltaNumTestsSkipped = calculateDeltaNumTestsSkipped(buildT1, buildT2);
    List<String> removedFailedMethods = calculateRemovedFailedMethods(buildT1.getFailedMethods(), testMethodsNotInT2);

    return BuildAnalysisResult.BuildAnalysisBuilder.aBuildAnalysisResult()
        .withAnalysisOk(calculateAnalysisOk(buildT1, buildT2))
        .withProject(buildT2.getProjectBranchKey().getProjectName())
        .withBranchT2(buildT2.getProjectBranchKey().getBranch())
        .withBuildIdT2(buildT2.getBuildId())
        .withTriggerCommitT2(buildT2.getTriggerCommit())
        .andBranchT1(buildT1.getProjectBranchKey().getBranch())
        .andBuildIdT1(buildT1.getBuildId())
        .andTriggerCommitT1(buildT1.getTriggerCommit())
        .andNumTestMethodsExtractedT1(testMethodsT1.size())
        .andNumTestMethodsExtractedT2(testMethodsT2.size())
        .andDeltaRunVsExtractedT1(deltaRunVsExtractedT1)
        .andDeltaRunVsExtractedT2(deltaRunVsExtractedT2)
        .andNumTestMethodsNotInT1(testMethodsNotInT1.size())
        .andNumTestMethodsNotInT2(testMethodsNotInT2.size())
        .andTestMethodsNotInT2(concatenatedMethodsNotInT2)
        .andDeltaTestsRun(deltaNumTestsRun)
        .andDeltaTestsOk(deltaNumTestsOk)
        .andDeltaTestsFailed(deltaNumTestsFailed)
        .andDeltaTestsSkipped(deltaNumTestsSkipped)
        .andLegacySmellWarning(calculateLegacySmellWarning(buildT1.getStatus(), deltaNumTestsRun, deltaNumTestsFailed, deltaNumTestsSkipped))
        .andNumFailedMethodsNotExtracted(calculateNumFailedMethodsNotExtracted(buildT1.getFailedMethods(), testMethodsT1))
        .andNumRemovedFailedTests(removedFailedMethods.size())
        .andRemovedFailedTests(StringUtility.concatStrings(removedFailedMethods, "#"))
        .create();
  }

  private Set<BasicMethodData> collectAllMethodsForBuild(Build build) {
    Set<BasicMethodData> allMethods = new HashSet<>();
    if (build.getMethodsByClass() != null) {
      allMethods = build.getMethodsByClass().values().stream()
          .flatMap(List::stream).collect(Collectors.toSet());
    }
    return allMethods;
  }

  /**
   * Calculates test methods that are in build A but not in build B. If a test method is not in
   * {@code testMethodsB} it will be checked whether the method is contained in {@code allMethodsB}.
   * This can be used to detect if a test method was only skipped and not removed entirely (by
   * adding @Ignore or removing @Test for instance).
   *
   * @param testMethodsA The set of test methods from build A
   * @param testMethodsB The set of test methods from build B
   * @param allMethodsB The set of all method from build B
   * @return a map where the keys are test methods that are not in {@code testMethodsB} and the
   *         values indicate whether these test methods are contained in {@code allMethodsB}
   *         instead.
   */
  private Map<BasicMethodData, Boolean> calculateTestMethodsInAButNotInB(Set<BasicMethodData> testMethodsA,
                                                                         Set<BasicMethodData> testMethodsB,
                                                                         Set<BasicMethodData> allMethodsB) {
    Map<BasicMethodData, Boolean> testMethodsNotInB = new HashMap<>();
    for (BasicMethodData method : testMethodsA) {
      if (!testMethodsB.contains(method)) {
        testMethodsNotInB.put(method, allMethodsB.contains(method));
      }
    }
    return testMethodsNotInB;
  }

  private Integer calculateDeltaNumTestsRun(Build buildT1, Build buildT2) {
    return NumberUtility.calculateDelta(buildT2.getNumTestsRun(), buildT1.getNumTestsRun());
  }

  private Integer calculateDeltaNumTestsOk(Build buildT1, Build buildT2) {
    return NumberUtility.calculateDelta(buildT2.getNumTestsOk(), buildT1.getNumTestsOk());
  }

  private Integer calculateDeltaNumTestsFailed(Build buildT1, Build buildT2) {
    return NumberUtility.calculateDelta(buildT2.getNumTestsFailed(), buildT1.getNumTestsFailed());
  }

  private Integer calculateDeltaNumTestsSkipped(Build buildT1, Build buildT2) {
    return NumberUtility.calculateDelta(buildT2.getNumTestsSkipped(), buildT1.getNumTestsSkipped());
  }

  private Boolean calculateLegacySmellWarning(String buildT1Status, Integer deltaNumTestsRun, Integer deltaNumTestsFailed, Integer deltaNumTestsSkipped) {
    if (buildT1Status == null || deltaNumTestsRun == null || deltaNumTestsFailed == null || deltaNumTestsSkipped == null) {
      return null;
    }
    boolean buildT1Passed = buildT1Status.equalsIgnoreCase("passed");
    boolean skippedTestMethods = deltaNumTestsFailed < 0 && (deltaNumTestsRun < 0 || deltaNumTestsSkipped > 0);

    return !buildT1Passed && skippedTestMethods;
  }

  private int calculateNumFailedMethodsNotExtracted(List<String> failedMethods, Set<BasicMethodData> testMethods) {
    if (CollectionUtility.isNullOrEmpty(failedMethods)) {
      return 0;
    }

    int numFailedMethodsNotExtracted = failedMethods.size();
    for (String failedMethod : failedMethods) {
      for (BasicMethodData testMethod : testMethods) {
        if (testMethod.getQualifiedName().contains(failedMethod)) {
          numFailedMethodsNotExtracted--;
          break;
        }
      }
    }

    return numFailedMethodsNotExtracted;
  }

  private List<String> calculateRemovedFailedMethods(List<String> failedMethodsT1,
                                                     Map<BasicMethodData, Boolean> testMethodsNotInT2) {
    if (CollectionUtility.isNullOrEmpty(failedMethodsT1)) {
      return Collections.emptyList();
    }

    List<String> removedFailedMethods = new ArrayList<>();
    for (BasicMethodData removedMethod : testMethodsNotInT2.keySet()) {
      for (String failedMethod : failedMethodsT1) {
        if (removedMethod.getQualifiedName().contains(failedMethod)) {
          removedFailedMethods.add(removedMethod.getQualifiedName());
          break;
        }
      }
    }
    return removedFailedMethods;
  }

  private boolean calculateAnalysisOk(Build buildT1, Build buildT2) {
    return nvl(buildT1.getExtractionSuccessful()) && nvl(buildT2.getExtractionSuccessful());
  }
}

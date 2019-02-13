package ch.svenstoll.mbm.skipfailedtestdetectorforjava;

import ch.svenstoll.mbm.skipfailedtestdetectorforjava.comparator.BuildComparator;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.Build;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.parser.BuildParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * <p>Running this program allows you to detect if a Junit test method that failed in a certain
 * build has been removed in the next build. The detection of this Continuous Integration
 * anti-pattern is achieved by parsing Java methods for each build and comparing the results
 * together with other build information contained in the input data.</p>
 *
 * <p>This detector is an alternative approach to the one used by CI-ODOR, a tool for automated
 * reporting of anti-patterns and decay in Continuous Integration proposed by Carmine Vassallo,
 * Sebastian Proksch, Harald Gall and Massimiliano Di Penta in their research paper
 * <a href="https://2019.icse-conferences.org/event/icse-2019-technical-papers-automated-reporting-of-anti-patterns-and-decay-in-continuous-integration">
 * Automated Reporting of Anti-Patterns and Decay in Continuous Integration</a></p>
 *
 * <p><strong>Limitations</strong><br>
 * This detector only works for Java projects that are hosted on
 * <a href="https://github.com">GitHub</a>, use Junit as testing framework and
 * <a href="https://travis-ci.org">Travis CI</a> as Continuous Integration platform.</p>
 *
 * <p>Additionally, there is no guarantee that all test methods will be detected. The analysis
 * solely relies on the available source code files. Therefore, test methods that are inherited
 * from a dependency will not be detected if they are not overridden (given that the method
 * declares {@code @Test} again or starts with {@code test}).</p>
 *
 * <p><strong>Input Data</strong><br>
 * It is recommended to use data from
 * <a href="https://travistorrent.testroots.org">TravisTorrent</a> as input for this program.
 * Running this program requires you to provide a comma-separated CSV file with the following
 * header: {@code gh_project_name,git_branch,tr_build_id,tr_prev_build,tr_job_id,tr_job_id,
 * git_trigger_commit,tr_status,tr_log_num_tests_run,tr_log_num_tests_ok,tr_log_num_tests_skipped,
 * tr_log_num_tests_failed,tr_log_tests_failed}</p>
 *
 * <p>The variables in the header are explained
 * <a href="https://travistorrent.testroots.org/page_dataformat">here</a> The values for
 * {@code gh_project}, {@code git_branch}, {@code tr_build_id}, {@code tr_job_id},
 * and {@code git_trigger_commit} must not be empty. Additionally,
 * {@code tr_build_id}, {@code tr_prev_build} and {@code tr_job_id} are required to be valid
 * numbers.</p>
 *
 * <p><strong>Output Data</strong><br>
 * The results of the analysis are two CSV files:
 * <ul>
 *   <li>A file containing all results of consecutive builds of the same project and branch</li>
 *   <li>A file containing only the results for cases where a failed test has been skipped in
 *       the next build or the following formula is true:
 *       {@code deltaBreaks < 0 && (deltaRuns < 0 || deltaSkipped > 0)}. The latter condition
 *       refers to the legacy smell warning that was used in CI-ODOR.</li>
 * </ul></p>
 *
 * <p><strong>Warning</strong><br>
 * This program will try to clone the GitHub repositories of the projects specified in the input
 * file. They will be downloaded to the specified output folder.</p>
 *
 * @see <a href="https://2019.icse-conferences.org/event/icse-2019-technical-papers-automated-reporting-of-anti-patterns-and-decay-in-continuous-integration">
 *   Automated Reporting of Anti-Patterns and Decay in Continuous Integration</a>
 * @see <a href="https://github.com">GitHub</a>
 * @see <a href="https://travis-ci.org">Travis CI</a>
 * @see <a href="https://travistorrent.testroots.org/">Travis Torrent</a>
 * @see <a href="https://travistorrent.testroots.org/page_dataformat/"> Travis Torrent: Data Format</a>
 */
public class SkipFailedTestsDetector {

  private static Logger logger;


  /**
   * The entry point to the program.
   *
   * @param args
   *     [0]: Path to a valid CSV input file, [1]: Path to the desired output folder
   *
   */
  public static void main(String[] args) {
    validateArgs(args);

    // The logger is initialized here, because the output location is only known at execution time.
    System.setProperty("SkipFailedTestsDetectorOutputFolder", args[1]);
    logger = LoggerFactory.getLogger(SkipFailedTestsDetector.class);

    List<Build> builds = new BuildParser().parseBuildsFile(args[0]);
    BuildComparator comparator = new BuildComparator(args[1]);
    comparator.compareBuilds(builds);

    logger.info("The analysis has finished. Check \"{}\" for the log file and your results.", args[1]);
  }

  private static void validateArgs(String[] args) {
    if (args.length != 2) {
      System.out.println("Make sure to run this program with the following arguments:");
      System.out.println("[1] Path to a valid CSV input file");
      System.out.println("[2] Path to the desired output folder");
      System.out.println();
      System.out.println("The CSV input file must be comma-separated and must contain the following headers:");
      System.out.println("gh_project_name,git_branch,tr_build_id,tr_prev_build,tr_job_id," +
          "git_trigger_commit,tr_status,tr_log_num_tests_run,tr_log_num_tests_ok," +
          "tr_log_num_tests_skipped,tr_log_num_tests_failed,tr_log_tests_failed");
      System.out.println();
      System.out.println("The values for \"gh_project\", \"git_branch\", \"tr_build_id\", " +
          "\"tr_job_id\" and \"git_trigger_commit\" must not be empty. Additionally, " +
          "\"tr_build_id\", \"tr_prev_build\" and \"tr_job_id\" are required to be valid numbers.");
      System.out.println();
      System.out.println("Visit https://travistorrent.testroots.org/page_dataformat/ for a more " +
          "detailed description of all the variables needed in the input file.");
      System.exit(1);
    }

    try {
      Path inputFilePath = Paths.get(args[0]);
      if (!Files.exists(inputFilePath)) {
        System.out.println("The specified input file does not exist.");
        System.exit(1);
      }
      else if (!args[0].toLowerCase().endsWith("csv")) {
        System.out.println("The specified input file is not a CSV file.");
        System.exit(1);
      }
    }
    catch (InvalidPathException e) {
      System.out.println("The specified input file path is invalid.");
      System.exit(1);
    }

    try {
      Path outputFolderPath = Paths.get(args[1]);
      if (Files.exists(outputFolderPath) && !Files.isDirectory(outputFolderPath)) {
        System.out.println("The specified output folder is an already existing file.");
        System.exit(1);
      }
    }
    catch (InvalidPathException e) {
      System.out.println("The specified output folder path is invalid.");
      System.exit(1);
    }
  }
}

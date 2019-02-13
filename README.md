# Skip Failed Tests Detector
Running this program allows you to detect if a Junit test method that failed in a certain build has been removed in the 
next build. The detection of this Continuous Integration anti-pattern is achieved by parsing Java methods for each build
and comparing the results together with other build information contained in the input data.

This detector is an alternative approach to the one used by CI-ODOR, a tool for automated reporting of anti-patterns and
decay in Continuous Integration proposed by Carmine Vassallo, Sebastian Proksch, Harald Gall and Massimiliano Di Penta
in their research paper [Automated Reporting of Anti-Patterns and Decay in Continuous Integration](https://2019.icse-conferences.org/event/icse-2019-technical-papers-automated-reporting-of-anti-patterns-and-decay-in-continuous-integration).

## Limitations
This detector only works for Java projects that are hosted on [GitHub](https://github.com/), use Junit as testing
framework and [Travis CI](https://travis-ci.org/) as Continuous Integration platform.

Additionally, there is no guarantee that all test methods will be detected. The analysis solely relies on the available
source code files. Therefore, test methods that are inherited from a dependency will not be detected if they are not
overridden (given that the method declares `@Test` again or starts with `test`). 

## Input Data
It is recommended to use data from [TravisTorrent](https://travistorrent.testroots.org/) as input for this program.
Running this program requires you to provide a comma-separated CSV file with the following header: 
`gh_project_name,git_branch,tr_build_id,tr_prev_build,tr_job_id,git_trigger_commit,tr_status,tr_log_num_tests_run,tr_log_num_tests_ok,tr_log_num_tests_skipped,tr_log_num_tests_failed,tr_log_tests_failed`

The variables in the header are explained [here](https://travistorrent.testroots.org/page_dataformat/).
The values for `gh_project`, `git_branch`, `tr_build_id`, `tr_job_id` and `git_trigger_commit` must not be empty.
Additionally, `tr_build_id`, `tr_prev_build` and `tr_job_id` are required to be valid numbers.<br>

## Output Data
The results of the analysis are two CSV files:
* A file containing all results of consecutive builds of the same project and branch</li>
* A file containing only the results for cases where a failed test has been skipped in the next build or the following
formula is true: `deltaBreaks < 0 && (deltaRuns < 0 || deltaSkipped > 0)`. The latter condition refers to the legacy
smell warning that was used in CI-ODOR.

## Warning  
This program will try to clone the GitHub repositories of the projects specified in the input file. They will be
downloaded to the specified output folder.

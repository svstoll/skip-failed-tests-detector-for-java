-- The following query was used to extract the TravisTorrent data set (see builds.csv).
SELECT
  gh_project_name,
  git_branch,
  tr_build_id,
  tr_prev_build,
  git_trigger_commit,
  STRING_AGG(tr_status) as tr_status,
  max(tr_log_num_tests_run) as tr_log_num_tests_run,
  max(tr_log_num_tests_ok) as tr_log_num_tests_ok,
  max(tr_log_num_tests_skipped) as tr_log_num_tests_skipped,
  max(tr_log_num_tests_failed) as tr_log_num_tests_failed,
  STRING_AGG(tr_log_tests_failed) as tr_log_tests_failed
FROM `travistorrent-bq.data.2017_02_08`
WHERE
    gh_lang = 'java'
    AND tr_log_analyzer = 'java-maven'
    AND tr_log_frameworks = 'junit'
    AND tr_log_num_tests_run IS NOT NULL
    AND tr_log_num_tests_ok IS NOT NULL
    AND tr_log_num_tests_failed IS NOT NULL
    AND tr_log_num_tests_skipped IS NOT NULL
    AND (
        (tr_log_tests_failed IS NULL AND tr_log_num_tests_failed = 0)
        OR
        (tr_log_tests_failed IS NOT NULL AND tr_log_num_tests_failed >= 1))
GROUP BY gh_project_name, git_branch, tr_build_id, tr_prev_build, git_trigger_commit
HAVING count(*) = 1
ORDER BY gh_project_name ASC, git_branch ASC, tr_build_id ASC;
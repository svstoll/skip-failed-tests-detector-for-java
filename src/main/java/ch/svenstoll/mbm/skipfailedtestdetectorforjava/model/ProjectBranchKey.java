package ch.svenstoll.mbm.skipfailedtestdetectorforjava.model;

import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.StringUtility;

import java.util.Objects;

public class ProjectBranchKey {

  private final String projectName;
  private final String branch;

  public ProjectBranchKey(String projectName, String branch) {
    if (StringUtility.isNullOrEmpty(projectName) || StringUtility.isNullOrEmpty(branch)) {
      throw new IllegalArgumentException("Project name and branch must not be null or empty.");
    }

    this.projectName = projectName;
    this.branch = branch;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getBranch() {
    return branch;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProjectBranchKey that = (ProjectBranchKey) o;
    return projectName.equals(that.projectName) && branch.equals(that.branch);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectName, branch);
  }

  @Override
  public String toString() {
    return projectName + "#" + branch;
  }
}

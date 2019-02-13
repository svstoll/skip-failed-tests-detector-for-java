package ch.svenstoll.mbm.skipfailedtestdetectorforjava.model;

import java.util.Objects;

public class BasicClassData {

  private final String qualifiedName;
  private final String simpleName;
  private String parentClass = null;

  public BasicClassData(String qualifiedName, String simpleName) {
    this.qualifiedName = qualifiedName;
    this.simpleName = simpleName;
  }

  /**
   * Constructor for a deep copy.
   *
   * @param basicClassData The object to copy.
   */
  public BasicClassData(BasicClassData basicClassData) {
    this (basicClassData.qualifiedName, basicClassData.simpleName);
    this.parentClass = basicClassData.parentClass;
  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public String getParentClass() {
    return parentClass;
  }

  public void setParentClass(String parentClass) {
    this.parentClass = parentClass;
  }

  @Override
  public String toString() {
    return qualifiedName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BasicClassData that = (BasicClassData) o;
    return qualifiedName.equals(that.qualifiedName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(qualifiedName);
  }
}

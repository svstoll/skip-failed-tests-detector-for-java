package ch.svenstoll.mbm.skipfailedtestdetectorforjava.model;

import java.util.Objects;

public class BasicMethodData {

  private final String qualifiedName;
  private final String signature;
  private final BasicClassData basicClassData;
  private Boolean isTestMethod = null;
  private Boolean isChildMethod = null;
  private Boolean isAbstractMethod = null;

  public BasicMethodData(String qualifiedName, String signature, BasicClassData basicClassData) {
    this.qualifiedName = qualifiedName;
    this.signature = signature;
    this.basicClassData = basicClassData;
  }

  /**
   * Constructor for a deep copy.
   *
   * @param basicMethodData The object to copy.
   */
  public BasicMethodData(BasicMethodData basicMethodData) {
    this.qualifiedName = basicMethodData.qualifiedName;
    this.signature = basicMethodData.signature;
    this.basicClassData = new BasicClassData(basicMethodData.basicClassData);
    this.isTestMethod = basicMethodData.isTestMethod;
    this.isChildMethod = basicMethodData.isChildMethod;
    this.isAbstractMethod = basicMethodData.isAbstractMethod;
  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  public String getSignature() {
    return signature;
  }

  public BasicClassData getBasicClassData() {
    return basicClassData;
  }

  public Boolean isTestMethod() {
    return isTestMethod;
  }

  public void setIsTestMethod(Boolean testMethod) {
    this.isTestMethod = testMethod;
  }

  public Boolean isChildMethod() {
    return isChildMethod;
  }

  public void setIsChildMethod(Boolean extended) {
    this.isChildMethod = extended;
  }

  public Boolean isAbstractMethod() {
    return isAbstractMethod;
  }

  public void setIsAbstract(Boolean isAbstract) {
    this.isAbstractMethod = isAbstract;
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
    BasicMethodData that = (BasicMethodData) o;
    return qualifiedName.equals(that.qualifiedName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(qualifiedName);
  }
}

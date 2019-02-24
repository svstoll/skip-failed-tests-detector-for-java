package ch.svenstoll.mbm.skipfailedtestdetectorforjava.extractor;

import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.BasicClassData;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.BasicMethodData;

import java.util.List;
import java.util.Map;

public class MethodVisitorArgument {

  private final String packageName;
  private final Map<BasicClassData, List<BasicMethodData>> methodsByClass;
  private final boolean hasJUnitImports;

  public MethodVisitorArgument(String packageName,
                               Map<BasicClassData, List<BasicMethodData>> methodsByClass,
                               boolean hasJUnitImports) {
    this.packageName = packageName;
    this.methodsByClass = methodsByClass;
    this.hasJUnitImports = hasJUnitImports;
  }

  public String getPackageName() {
    return packageName;
  }

  public Map<BasicClassData, List<BasicMethodData>> getMethodsByClass() {
    return methodsByClass;
  }

  public boolean hasJUnitImports() {
    return hasJUnitImports;
  }
}

package ch.svenstoll.mbm.skipfailedtestdetectorforjava.extractor;

import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.BasicClassData;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.BasicMethodData;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.StringUtility;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

public class MethodVisitor extends VoidVisitorAdapter<MethodVisitorArgument> {

  @Override
  public void visit(MethodDeclaration declaration, MethodVisitorArgument arg) {
    if (!declaration.getParentNode().isPresent()) {
      return;
    }

    BasicClassData classData = calculateBasicClassData(arg.getPackageName(), declaration.getParentNode().get());
    String methodSignature = declaration.getSignature().asString();
    String qualifiedMethodName = classData.getQualifiedName() + "." + methodSignature;

    BasicMethodData methodData = new BasicMethodData(qualifiedMethodName, methodSignature,
        classData);
    methodData.setIsChildMethod(isChildMethod(declaration));
    methodData.setIsTestMethod(isTestMethod(declaration));
    methodData.setIsAbstract(declaration.isAbstract());

    final Map<BasicClassData, List<BasicMethodData>> methodsByClass = arg.getMethodsByClass();
    addBasicMethodData(methodsByClass, classData, methodData);

    super.visit(declaration, arg);
  }

  private synchronized void addBasicMethodData(
      Map<BasicClassData, List<BasicMethodData>> methodsByClass,
      BasicClassData classData,
      BasicMethodData methodData) {
    List<BasicMethodData> methods = methodsByClass.get(classData);
    if (methods == null) {
      methods = new ArrayList<>();
      methods.add(methodData);
      methodsByClass.put(classData, methods);
    }
    else {
      methods.add(methodData);
    }
  }

  private boolean isChildMethod(MethodDeclaration declaration) {
    return declaration.getAnnotationByName("Override").isPresent();
  }

  private boolean isTestMethod(MethodDeclaration declaration) {
    if (declaration.getAnnotationByName("Ignore").isPresent()) {
      return false;
    }

    return declaration.getAnnotationByName("Test").isPresent()
        || declaration.getNameAsString().toLowerCase().startsWith("test");
  }

  private BasicClassData calculateBasicClassData(String packageName, Node node) {
    if (node == null) {
      throw new IllegalArgumentException();
    }

    Node currentNode = node;
    String simpleClassName = getSimpleClassName(node);
    String parentClassName = getParentClassName(node);
    StringBuilder qualifiedClassName = new StringBuilder(calculatePartOfQualifiedClassName(node));
    while (currentNode.getParentNode().isPresent()) {
      currentNode = currentNode.getParentNode().get();
      if (simpleClassName == null) {
        simpleClassName = getSimpleClassName(node);
      }
      if (parentClassName == null) {
        parentClassName = getParentClassName(node);
      }

      String qualifiedClassNamePart = calculatePartOfQualifiedClassName(currentNode);
      if (!StringUtility.isNullOrEmpty(qualifiedClassNamePart)) {
        if (qualifiedClassName.length() == 0) {
          qualifiedClassName.insert(0, qualifiedClassNamePart);
        }
        else {
          qualifiedClassName.insert(0, qualifiedClassNamePart + ".");
        }
      }
    }

    if (!StringUtility.isNullOrEmpty(packageName)) {
      qualifiedClassName.insert(0, packageName + ".");
    }

    BasicClassData classData = new BasicClassData(qualifiedClassName.toString(), simpleClassName);
    classData.setParentClass(parentClassName);
    return classData;
  }

  private String getSimpleClassName(Node node) {
    if (node == null) {
      return null;
    }

    if (node instanceof ClassOrInterfaceDeclaration) {
      ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) node;
      return declaration.getName().getIdentifier();
    }
    return null;
  }

  private String getParentClassName(Node node) {
    if (node == null) {
      return null;
    }

    if (node instanceof ClassOrInterfaceDeclaration) {
      ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) node;
      NodeList<ClassOrInterfaceType> extendedTypes = declaration.getExtendedTypes();
      if (extendedTypes.isNonEmpty()) {
        return extendedTypes.get(0).getName().getIdentifier();
      }
    }
    return null;
  }

  private String calculatePartOfQualifiedClassName(Node node) {
    if (node == null) {
      return "";
    }
    String name = "";
    if (node instanceof ClassOrInterfaceDeclaration) {
      name = ((ClassOrInterfaceDeclaration) node).getName().getIdentifier();
    }
    else if (node instanceof ObjectCreationExpr) {
      ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) node;
      if (objectCreationExpr.getType() != null) {
        name = objectCreationExpr.getType().getName().getIdentifier();
      }
    }

    return name;
  }
}

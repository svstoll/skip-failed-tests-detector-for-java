package ch.svenstoll.mbm.skipfailedtestdetectorforjava.extractor;

import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.BasicClassData;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.BasicMethodData;
import ch.svenstoll.mbm.skipfailedtestdetectorforjava.model.Build;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.TokenMgrException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ch.svenstoll.mbm.skipfailedtestdetectorforjava.utility.BooleanUtility.nvl;

public class MethodExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodExtractor.class);

  private final String outputFolderPath;

  public MethodExtractor(String outputFolderPath) {
    this.outputFolderPath = outputFolderPath;
  }

  /**
   * All methods contained in Java files where the word {@code test} (case ignored) occurs in the
   * file path will be extracted.
   *
   * @param build
   *     The build for which methods will be extracted.
   */
  public void extractMethodsForBuild(Build build) {
    if (build == null || build.getExtractionSuccessful() != null) {
      return;
    }

    String projectName = build.getProjectBranchKey().getProjectName();
    Git git;
    try {
      git = cloneProjectRepository(projectName);
    }
    catch (GitAPIException | IOException e) {
      LOGGER.error("Failed to find or clone GitHub repository for {}.", projectName, e);
      build.setExtractionSuccessful(false);
      return;
    }

    extractMethodsForBuildInternal(git, build);
  }

  private Git cloneProjectRepository(String projectName) throws GitAPIException, IOException {
    String projectPath = getProjectPath(projectName);
    if (!Files.exists(Paths.get(projectPath))) {
      LOGGER.info("Cloning repository for {}.", projectName);
      return Git.cloneRepository()
          .setURI(getGitURI(projectName))
          .setDirectory(new File(projectPath))
          .call();
    }
    else {
      return Git.open(new File(projectPath));
    }
  }

  private String getProjectPath(String projectName) {
    String projectPath = projectName.replace("/", "#");
    return outputFolderPath + "/Repositories/" + projectPath;
  }

  private String getGitURI(String projectName) {
    return "https://github.com/" + projectName + ".git";
  }

  private void extractMethodsForBuildInternal(Git git, Build build) {
    try {
      LOGGER.info("Extracting methods for {}.", build);
      // There are cases where files are left behind after a checkout. The working directory must be
      // in a clean state so that checkout will not fail.
      Status status = git.status().call();
      if (status.hasUncommittedChanges() || !status.isClean()) {
        git.gc();
        git.clean().setForce(true).call();
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
        git.stashCreate().setIncludeUntracked(true).call();
      }

      git.checkout().setName(build.getTriggerCommit()).call();

      int availableProcessors = Runtime.getRuntime().availableProcessors();
      final ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);
      final Map<BasicClassData, List<BasicMethodData>> methodsByClass = new ConcurrentHashMap<>();

      Files.walk(Paths.get(git.getRepository().getWorkTree().toString()))
          .filter(path -> path.toString().toLowerCase().contains("test"))
          .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".java"))
          .forEach(path -> executorService.execute(() -> extractMethodsFromFile(path, methodsByClass)));
      executorService.shutdown();
      executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

      checkForExtendedTestMethods(methodsByClass);
      build.setMethodsByClass(methodsByClass);
      build.setExtractionSuccessful(true);
    }
    catch (Exception e) {
      LOGGER.error("Error during method extraction for {}.", build, e);
      build.setExtractionSuccessful(false);
    }
  }

  private void extractMethodsFromFile(Path path, final Map<BasicClassData, List<BasicMethodData>> methodsByClass) {
    try (FileInputStream inputStream = new FileInputStream(path.toFile())){
      CompilationUnit compilationUnit = JavaParser.parse(inputStream);
      String packageName = "";
      if (compilationUnit.getPackageDeclaration().isPresent()) {
        packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
      }

      MethodVisitorArgument arg = new MethodVisitorArgument(packageName, methodsByClass, hasJUnitImports(compilationUnit));
      compilationUnit.accept(new MethodVisitor(), arg);
    }
    catch (TokenMgrException | ParseProblemException e) {
      // Because there are cases where projects keep invalid Java files for testing purposes (e.g.
      // SonarQube, Checkstyle or Qulice), these exceptions are simply caught and logged, but will
      // not result in a failed build analysis.
      LOGGER.warn("Could not parse \"{}\".", path.toString());
    }
    catch (IOException e) {
      throw new RuntimeException("I/O exception while trying to extract methods for \"" + path.toString() + "\".", e);
    }
  }

  private boolean hasJUnitImports(CompilationUnit cu) {
    if (cu == null) {
      return false;
    }

    List<ImportDeclaration> imports = cu.getImports();
    for (ImportDeclaration importDeclaration : imports) {
      if (importDeclaration.getName().asString().contains("junit")) {
        return true;
      }
    }
    return false;
  }

  private void checkForExtendedTestMethods(Map<BasicClassData, List<BasicMethodData>> methodsByClass) {
    Map<String, List<BasicClassData>> classDataBySimpleClassName = generateClassDataBySimpleClassNameMap(methodsByClass);
    for (List<BasicMethodData> methods : methodsByClass.values()) {
      for (BasicMethodData method : methods) {
        if (!nvl(method.isTestMethod()) && !nvl(method.isAbstractMethod()) && nvl(method.isChildMethod())) {
          findExtendedTestMethods(method, classDataBySimpleClassName, methodsByClass);
        }
      }
    }
  }

  private void findExtendedTestMethods(BasicMethodData method,
                                       Map<String, List<BasicClassData>> classDataBySimpleClassName,
                                       Map<BasicClassData, List<BasicMethodData>> methodsByClass) {
    List<BasicMethodData> methodChain = new ArrayList<>();
    methodChain.add(method);

    String simpleParentClassName = method.getBasicClassData().getParentClass();
    List<BasicClassData> potentialParentClasses = classDataBySimpleClassName.get(simpleParentClassName);
    while (potentialParentClasses != null && potentialParentClasses.size() > 0) {
      List<BasicClassData> newPotentialParentClasses = null;
      parentClassLoop: for (BasicClassData potentialParentClass : potentialParentClasses) {
        List<BasicMethodData> potentialParentClassMethods = methodsByClass.get(potentialParentClass);
        for (BasicMethodData potentialParentClassMethod : potentialParentClassMethods) {
          if (method.getSignature().equals(potentialParentClassMethod.getSignature())) {
            methodChain.add(potentialParentClassMethod);
            newPotentialParentClasses = classDataBySimpleClassName.get(potentialParentClass.getParentClass());
            break parentClassLoop;
          }
        }
      }

      if (newPotentialParentClasses != null && newPotentialParentClasses.equals(potentialParentClasses)) {
        LOGGER.info("Loop detected while searching for potential parent classes for: {}", newPotentialParentClasses);
        potentialParentClasses = null;
      }
      else {
        potentialParentClasses = newPotentialParentClasses;
      }
    }

    boolean testMethodEncountered = false;
    for (int i = methodChain.size() - 1; i >= 0; i--) {
      if (methodChain.get(i).isTestMethod()) {
        testMethodEncountered = true;
      }
      else if (testMethodEncountered) {
        methodChain.get(i).setIsTestMethod(true);
      }
    }
  }

  private Map<String, List<BasicClassData>> generateClassDataBySimpleClassNameMap(Map<BasicClassData, List<BasicMethodData>> methodsByClass) {
    Map<String, List<BasicClassData>> classDataBySimpleClassName = new HashMap<>();

    for (BasicClassData classData : methodsByClass.keySet()) {
      if (classData.getSimpleName() == null) {
        continue;
      }
      List<BasicClassData> classes = classDataBySimpleClassName.get(classData.getSimpleName());
      if (classes == null) {
        classes = new ArrayList<>();
        classes.add(classData);
        classDataBySimpleClassName.put(classData.getSimpleName(), classes);
      }
      else {
        classes.add(classData);
      }
    }

    return classDataBySimpleClassName;
  }
}

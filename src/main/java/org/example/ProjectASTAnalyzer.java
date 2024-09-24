package org.example;

import org.eclipse.jdt.core.dom.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

public class ProjectASTAnalyzer {

    // Variables pour stocker les résultats de l'analyse
    private static int classCount = 0;
    private static int totalLinesOfCode = 0;
    private static int totalMethods = 0;
    private static Set<String> packages = new HashSet<>();
    private static Map<String, Integer> methodCountsByClass = new HashMap<>();
    private static Map<String, Integer> attributeCountsByClass = new HashMap<>();
    private static Map<String, Integer> linesOfCodeByMethod = new HashMap<>();
    private static List<String> classList = new ArrayList<>();
    private static int maxParameters = 0; // Suivre le nombre maximal de paramètres
    private static Map<String, List<String>> callGraph = new HashMap<>(); // Graphe d'appel

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the value for X (minimum number of methods to filter classes): ");
        int X = scanner.nextInt();
        scanner.close();

        String projectDir = "/home/kawthar/Téléchargements/visitorDesignPattern";
        String outputFile = "/home/kawthar/Analyseur_syntaxique/src/main/java/org/example/analysis_report.txt";
        String dotFilePath = "/home/kawthar/Analyseur_syntaxique/src/main/java/org/example/call_graph.dot"; // Chemin pour le fichier DOT

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            List<Path> javaFiles = getAllJavaFiles(projectDir);

            for (Path javaFile : javaFiles) {
                writer.println("============================================");
                writer.println("Analyzing file: " + javaFile.toString());
                writer.println("============================================\n");
                String sourceCode = new String(Files.readAllBytes(javaFile));
                analyzeSourceCode(sourceCode, writer);
            }

            // Ajouter les résultats d'analyse au fichier de sortie
            appendAnalysisResults(writer, X);
            writeCallGraph(writer); // Écrire le graphe d'appel
            writeCallGraphToDotFile(dotFilePath); // Écrire le fichier DOT pour visualisation
        }
    }

    public static List<Path> getAllJavaFiles(String projectDir) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(projectDir))) {
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }

    public static void analyzeSourceCode(String sourceCode, PrintWriter writer) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(sourceCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        String[] lines = sourceCode.split("\n");
        totalLinesOfCode += lines.length;

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                classCount++;
                String className = node.getName().toString();
                classList.add(className);

                if (cu.getPackage() != null) {
                    String packageName = cu.getPackage().getName().toString();
                    packages.add(packageName);
                }

                FieldDeclaration[] fields = node.getFields();
                int attributeCount = fields.length;
                attributeCountsByClass.put(className, attributeCount);

                writer.println("\nClass: " + className);
                writer.println("Attributes:");
                if (attributeCount > 0) {
                    for (FieldDeclaration field : fields) {
                        List<VariableDeclarationFragment> fragments = field.fragments();
                        for (VariableDeclarationFragment fragment : fragments) {
                            writer.println("    - " + fragment.getName() + " (" + getEncapsulation(field.getModifiers()) + ")");
                        }
                    }
                } else {
                    writer.println("    None");
                }

                MethodDeclaration[] methods = node.getMethods();
                int methodCount = methods.length;
                totalMethods += methodCount;
                methodCountsByClass.put(className, methodCount);

                writer.println("Methods:");
                if (methodCount > 0) {
                    for (MethodDeclaration method : methods) {
                        String methodName = method.getName().toString();
                        writer.println("    - " + methodName);
                        writer.println("        Called methods:");
                        int methodLines = method.getBody() != null ? method.getBody().statements().size() : 0;
                        linesOfCodeByMethod.put(methodName, methodLines);

                        // Suivre le max des paramètres
                        maxParameters = Math.max(maxParameters, method.parameters().size());

                        // Collecte des appels de méthode
                        callGraph.putIfAbsent(methodName, new ArrayList<>());

                        method.accept(new ASTVisitor() {
                            boolean calledMethodsExist = false;

                            @Override
                            public boolean visit(MethodInvocation invocation) {
                                calledMethodsExist = true;
                                String calledMethodName = invocation.getName().toString();
                                callGraph.get(methodName).add(calledMethodName); // Ajouter l'appel à la méthode
                                writer.println("            - " + calledMethodName);
                                return super.visit(invocation);
                            }

                            @Override
                            public void endVisit(MethodDeclaration node) {
                                if (!calledMethodsExist) {
                                    writer.println("            None");
                                }
                                super.endVisit(node);
                            }
                        });
                    }
                } else {
                    writer.println("    None");
                }

                writer.println();
                return super.visit(node);
            }
        });
    }

    public static String getEncapsulation(int modifiers) {
        StringBuilder encapsulation = new StringBuilder();

        if (Modifier.isPublic(modifiers)) {
            encapsulation.append("public ");
        }
        if (Modifier.isProtected(modifiers)) {
            encapsulation.append("protected ");
        }
        if (Modifier.isPrivate(modifiers)) {
            encapsulation.append("private ");
        }
        if (Modifier.isStatic(modifiers)) {
            encapsulation.append("static ");
        }
        if (Modifier.isFinal(modifiers)) {
            encapsulation.append("final ");
        }
        if (Modifier.isAbstract(modifiers)) {
            encapsulation.append("abstract ");
        }

        return encapsulation.toString().trim();
    }

    private static void appendAnalysisResults(PrintWriter writer, int X) {
        writer.println("============================================");
        writer.println("Analysis Summary");
        writer.println("============================================");
        writer.println("Total number of classes: " + classCount);
        writer.println("Total lines of code: " + totalLinesOfCode);
        writer.println("Total number of methods: " + totalMethods);
        writer.println("Total number of packages: " + packages.size());

        double averageMethodsPerClass = (classCount > 0) ? (double) totalMethods / classCount : 0;
        writer.printf("Average number of methods per class: %.2f%n", averageMethodsPerClass);

        double averageLinesPerMethod = (totalMethods > 0) ? (double) totalLinesOfCode / totalMethods : 0;
        writer.printf("Average number of lines per method: %.2f%n", averageLinesPerMethod);

        double averageAttributesPerClass = (classCount > 0) ? (double) attributeCountsByClass.values().stream().mapToInt(Integer::intValue).sum() / classCount : 0;
        writer.printf("Average number of attributes per class: %.2f%n", averageAttributesPerClass);

        List<Map.Entry<String, Integer>> topMethodClasses = methodCountsByClass.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
        int top10PercentCount = Math.max(1, topMethodClasses.size() / 10);
        writer.println("Top 10% classes with the highest number of methods:");
        for (int i = 0; i < top10PercentCount; i++) {
            writer.println("    - " + topMethodClasses.get(i).getKey() + ": " + topMethodClasses.get(i).getValue() + " methods");
        }

        List<Map.Entry<String, Integer>> topAttributeClasses = attributeCountsByClass.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());
        int top10PercentAttrCount = Math.max(1, topAttributeClasses.size() / 10);
        writer.println("Top 10% classes with the highest number of attributes:");
        for (int i = 0; i < top10PercentAttrCount; i++) {
            writer.println("    - " + topAttributeClasses.get(i).getKey() + ": " + topAttributeClasses.get(i).getValue() + " attributes");
        }

        // Classes that are in both categories
        Set<String> topClassesWithMostMethods = topMethodClasses.stream()
                .limit(top10PercentCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<String> topClassesWithMostAttributes = topAttributeClasses.stream()
                .limit(top10PercentAttrCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        topClassesWithMostMethods.retainAll(topClassesWithMostAttributes); // Intersection
        writer.println("Classes that are in both categories:");
        for (String className : topClassesWithMostMethods) {
            writer.println("    - " + className);
        }

        // Classes with more than X methods
        writer.println("Classes with more than " + X + " methods:");
        for (Map.Entry<String, Integer> entry : methodCountsByClass.entrySet()) {
            if (entry.getValue() > X) {
                writer.println("    - " + entry.getKey() + ": " + entry.getValue() + " methods");
            }
        }

        // Top 10% methods by lines of code
        List<Map.Entry<String, Integer>> topMethodsByLines = linesOfCodeByMethod.entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Descending order
                .collect(Collectors.toList());
        int top10PercentMethodsCount = Math.max(1, topMethodsByLines.size() / 10);
        writer.println("Top 10% methods with the highest number of lines of code:");
        for (int i = 0; i < top10PercentMethodsCount; i++) {
            writer.println("    - " + topMethodsByLines.get(i).getKey() + ": " + topMethodsByLines.get(i).getValue() + " lines");
        }

        // Maximum number of parameters among all methods
        writer.println("Maximum number of parameters in a method: " + maxParameters);
    }

    private static void writeCallGraph(PrintWriter writer) {
        writer.println("============================================");
        writer.println("Call Graph");
        writer.println("============================================");
        for (Map.Entry<String, List<String>> entry : callGraph.entrySet()) {
            writer.println(entry.getKey() + " calls:");
            for (String calledMethod : entry.getValue()) {
                writer.println("    - " + calledMethod);
            }
        }
    }

    private static void writeCallGraphToDotFile(String dotFilePath) throws IOException {
        try (PrintWriter dotWriter = new PrintWriter(new FileWriter(dotFilePath))) {
            dotWriter.println("digraph CallGraph {");
            for (Map.Entry<String, List<String>> entry : callGraph.entrySet()) {
                for (String calledMethod : entry.getValue()) {
                    dotWriter.printf("    \"%s\" -> \"%s\";%n", entry.getKey(), calledMethod);
                }
            }
            dotWriter.println("}");
        }
    }
}

package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaFileReader {
    public static List<Path> getAllJavaFiles(String projectDir) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(projectDir))) {
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }
}

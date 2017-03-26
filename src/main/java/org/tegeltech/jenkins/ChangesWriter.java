package org.tegeltech.jenkins;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChangesWriter {

    /**
     * Saves the changes to disk.
     */
    public void saveChanges(List<String> changedSources, List<String> changedTests, String changesPath, PrintStream logger) {
        Path path = Paths.get(changesPath);
        logger.println("Writing changes to: " + path);
        List<String> changesContent = Arrays.asList(squash(changedSources), squash(changedTests));
        try {
            Files.write(path, changesContent);
        } catch (IOException e) {
            e.printStackTrace(logger);
        }
    }

    private String squash(List<String> strings) {
        return strings.stream().collect(Collectors.joining(","));
    }

}

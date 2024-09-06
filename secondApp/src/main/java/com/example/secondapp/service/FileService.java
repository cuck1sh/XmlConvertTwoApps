package com.example.secondapp.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileService {
    public static final String NEW_DATA_PATH = "../data/new";
    public static final String PROCESSED_DATA_PATH = "../data/processed";

    @SneakyThrows
    public List<Path> getListFilesPath(Path path) {
        try (Stream<Path> paths = Files.list(path)) {
            return paths.toList();
        } catch (IOException e) {
            Files.createDirectory(path);
            return getListFilesPath(path);
        }

    }

    @SneakyThrows
    public List<String> readFile(Path path) {
        return Files.readAllLines(path);
    }

    @SneakyThrows
    public void modeFile(String filename) {
        try {
            Files.move(
                    Paths.get(NEW_DATA_PATH + filename),
                    Paths.get(PROCESSED_DATA_PATH + filename),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            Files.createDirectory(Paths.get(PROCESSED_DATA_PATH));
            modeFile(filename);
        }
    }

    @SneakyThrows
    public void writeFile(String filename, String content) {
        Path path = Paths.get(PROCESSED_DATA_PATH + filename);
        Files.write(path, content.getBytes());
    }
}

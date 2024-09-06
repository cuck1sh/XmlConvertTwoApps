package com.example.secondapp.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileService {
    private static final String NEW_DIR = "../data/new/";
    private static final String BATCHED_DATA_PATH = "../data/batched/";

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
    public void fileProcessor(String filename) {
        int number = extractRecordCount(filename);

        String fileType = getType(filename);
        String fileDate = getDate(filename);

        String shortFilename = getFileName(fileType, fileDate)
                .orElseThrow(() -> new NoSuchElementException("Не найдена директория для " + filename));
        List<String> records = readLogFile(NEW_DIR + shortFilename);
        String unprocessedRecord = records.get(number - 1);

        String batchedFileName = getBatchedFileName(fileType, fileDate);
        boolean spaceInBatch = check100records(batchedFileName);


        Path pathDir = Paths.get(BATCHED_DATA_PATH);
        if (spaceInBatch) {
            writeRecord(pathDir.resolve(batchedFileName).toFile(), unprocessedRecord);
        } else {
            String newBatchedFileName = renameFile(batchedFileName);
            Files.createFile(pathDir.resolve(newBatchedFileName));
            writeRecord(pathDir.resolve(newBatchedFileName).toFile(), unprocessedRecord);
        }
    }

    private String renameFile(String fileName) {
        return incrementFileName(fileName);
    }

    private Integer getFilesNumber(String fileName) {
        int lastDashIndex = fileName.lastIndexOf('-');
        int dotIndex = fileName.lastIndexOf('.');

        String numberPart = fileName.substring(lastDashIndex + 1, dotIndex);

        return Integer.parseInt(numberPart);
    }

    private String incrementFileName(String fileName) {
        int lastDashIndex = fileName.lastIndexOf('-');
        int dotIndex = fileName.lastIndexOf('.');

        int number = getFilesNumber(fileName);
        String incrementedNumber = String.format("%04d", number + 1);

        return fileName.substring(0, lastDashIndex + 1) + incrementedNumber + fileName.substring(dotIndex);
    }

    @SneakyThrows
    public void writeRecord(File logFile, String unprocessedRecord) {
        try (FileWriter fileWriter = new FileWriter(logFile, true)) {
            fileWriter.write(unprocessedRecord);
            fileWriter.write(System.lineSeparator());
        }
    }

    @SneakyThrows
    private Boolean check100records(String batchedFilename) {
        List<String> records = readLogFile(BATCHED_DATA_PATH + batchedFilename);
        return records.size() < 100;
    }

    @SneakyThrows
    private String getBatchedFileName(String type, String date) {
        Optional<String> file = getListFilesPath(Paths.get(BATCHED_DATA_PATH)).stream()
                .map(e -> e.getFileName().toString())
                .filter(e -> e.startsWith(type) && e.contains(date) && e.endsWith(getLastProcessedBatchNumber(type, date) + ".log"))
                .findFirst();

        if (file.isPresent()) {
            return file.get();
        } else {
            Path pathDir = Paths.get(BATCHED_DATA_PATH);
            String newBatchedFileName = String.format("%s-%s-0001.log", type, date);
            Files.createFile(pathDir.resolve(newBatchedFileName));
            return newBatchedFileName;
        }
    }

    private Integer getLastProcessedBatchNumber(String type, String date) {
        File batchedDir = new File(BATCHED_DATA_PATH);
        File[] batchedFiles = batchedDir.listFiles((dir, name) -> name.startsWith(type) && name.contains(date));

        if (batchedFiles == null || batchedFiles.length == 0) {
            return 0;
        }

        return Arrays.stream(batchedFiles)
                .mapToInt(f -> extractRecordCount(f.getName()))
                .max()
                .orElse(0);
    }

    private Optional<String> getFileName(String type, String date) {
        Path directory = Paths.get(NEW_DIR);
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith(type) && name.contains(date))
                    .findFirst();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public String getType(String filename) {
        String[] parts = filename.split("-");
        return parts[0];
    }

    public String getDate(String filename) {
        String[] parts = filename.split("-");
        return String.format("%s-%s-%s", parts[1], parts[2], parts[3]);
    }

    private static List<String> readLogFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }

    private static int extractRecordCount(String fileName) {
        String[] parts = fileName.split("-");
        log.info("extractRecordCount = {}", parts[4].replace(".log", ""));
        String recordCountStr = parts[4].replace(".log", "");
        return Integer.parseInt(recordCountStr);
    }
}

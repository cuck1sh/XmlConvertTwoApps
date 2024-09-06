package com.example.firstapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionService {
    private final ObjectMapper objectMapper;
    private final MainService mainService;
    public static final String DATA_PATH = "../data/new";

    @SneakyThrows
    public void convert(String xml) {
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode node = xmlMapper.readTree(xml.getBytes());

        String json = objectMapper.writeValueAsString(node);

        writeLog(json);
        log.info("Обработана строка: {}", json);
    }

    public void writeLog(String jsonString) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonString);
        String type = rootNode.path("Type").asText();
        String date = formatDate(rootNode.path("Creation").path("Date").asText());


        Path parentDir = Paths.get(DATA_PATH);
        if (!Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        Optional<String> filePath = getFileName(type, date);
        Path logFilePath;
        File logFile;

        if (filePath.isPresent()) {
            logFilePath = renameFile(parentDir.resolve(filePath.get()));
        } else {
            logFilePath = Files.createFile(parentDir.resolve(type + "-" + date + "-" + "0001.log"));
        }
        logFile = logFilePath.toFile();

        try (FileWriter fileWriter = new FileWriter(logFile, true)) {
            fileWriter.write(jsonString);
            fileWriter.write(System.lineSeparator());
        }
        mainService.sendToRabbit(logFile.getName());
    }

    private Path renameFile(Path sourcePath) {
        String fileName = sourcePath.getFileName().toString();
        String newFileName = incrementFileName(fileName);
        Path targetPath = sourcePath.resolveSibling(newFileName);
        try {
            Files.move(sourcePath, targetPath);
            return targetPath;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NoSuchElementException();
        }
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

    private Optional<String> getFileName(String type, String date) {
        Path directory = Paths.get(DATA_PATH);
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

    private String formatDate(String date) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return offsetDateTime.format(formatter);
    }
}

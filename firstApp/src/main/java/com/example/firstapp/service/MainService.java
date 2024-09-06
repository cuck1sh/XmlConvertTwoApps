package com.example.firstapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainService {
    private final FileService fileService;
    private final RabbitTemplate rabbitTemplate;
    public static final String NEW_DATA_PATH = "../data/new";

    public void sendToRabbit(String fileName) {
        rabbitTemplate.convertAndSend("conversionQueue", fileName);
    }

    public void run() {
        List<Path> paths = fileService.getListFilesPath(Paths.get(NEW_DATA_PATH));
        paths.forEach((e -> {
            List<String> listLines = fileService.readFile(e);
            batching(listLines, 100)
                    .forEach(i -> rabbitTemplate.convertAndSend("conversionQueue", i.toString()));
            fileService.moveFiles(e.getFileName().toString());
        }));
    }

    private List<List<String>> batching(List<String> list, int batch) {
        List<List<String>> parts = new ArrayList<>();
        int listSize = list.size();
        for (int i = 0; i < listSize; i = i + batch) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(listSize, i + batch))));
        }
        return parts;
    }
}


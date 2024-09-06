package com.example.secondapp.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainService {
    private final FileService fileService;

    @SneakyThrows
    public void handleData(String message) {
        fileService.fileProcessor(message);
        log.info("Filename: {} batched", message);
    }
}


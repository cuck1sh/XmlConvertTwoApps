package com.example.firstapp.controller;

import com.example.firstapp.service.ConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConversionController {
    private final ConversionService conversionService;


    @PostMapping
    public HttpStatus getRequest(@RequestBody String request) {
        conversionService.convert(request);
        return HttpStatus.OK;
    }
}

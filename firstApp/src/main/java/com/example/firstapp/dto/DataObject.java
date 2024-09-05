package com.example.firstapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataObject {
    private MethodObject method;
    private ProcessObject process;
    private String layer;
    private CreationObject creation;
    private String type;
}

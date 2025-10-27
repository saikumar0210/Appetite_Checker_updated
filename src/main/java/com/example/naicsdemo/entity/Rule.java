package com.example.naicsdemo.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Rule {
    String parameter;
    String description;
    Character operator;
    String value;
}

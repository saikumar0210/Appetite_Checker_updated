package com.example.naicsdemo.dto;

import lombok.Data;

@Data
public class GeminiRecommendation {
    private String eligibilityReason;
    private String shortRecommendation;
    private boolean needsRecommendation;
}
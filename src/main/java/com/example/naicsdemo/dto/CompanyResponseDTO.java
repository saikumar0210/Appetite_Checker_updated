package com.example.naicsdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyResponseDTO {
    private MatchingData matchingData;
    private AppendedData appendedData;
    private GeminiRecommendation recommendation;
}

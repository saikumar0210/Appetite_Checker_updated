package com.example.naicsdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CompanyRequestDTO {

    @JsonProperty("Matching Data")
    private MatchingData matchingData;

    @JsonProperty("Appended Data")
    private AppendedData appendedData;
}

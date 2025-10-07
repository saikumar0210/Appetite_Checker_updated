package com.example.naicsdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MatchingData {

    @JsonProperty("Request ID")
    private Integer requestId;

    @JsonProperty("Layout")
    private String layout;

    @JsonProperty("Match Method")
    private String matchMethod;

    @JsonProperty("Match Grade")
    private String matchGrade;

    @JsonProperty("Confidence Code")
    private String confidenceCode;

    @JsonProperty("DUNS #")
    private String duns;

    @JsonProperty("Matches Remaining")
    private Integer matchesRemaining;

    @JsonProperty("BEMFAB")
    private String bemfab;
}

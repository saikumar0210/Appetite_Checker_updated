package com.example.naicsdemo.service;

import com.example.naicsdemo.dto.AppendedData;
import com.example.naicsdemo.dto.CompanyResponseDTO;
import com.example.naicsdemo.dto.GeminiRecommendation;
import com.example.naicsdemo.dto.MatchingData;
import com.example.naicsdemo.entity.Company;
import com.example.naicsdemo.repository.CompanyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiService {
    private static final String GEMINI_API_KEY = "AIzaSyAe-VdGRTEa8TNKhghDul4SW36S3_4nnLE";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    @Autowired
    private CompanyRepository companyRepository;

    public String getRecommendations(String payload) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(payload);
        
        validatePayload(node);
        String prompt = buildPrompt(node);
        return callGeminiAPI(prompt);
    }

    public String listModels() throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1/models?key=" + GEMINI_API_KEY;
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    public String getShortRecommendation(String businessType, String naicsCode, String confidenceCode) throws Exception {
        String prompt = String.format(
            "Business: %s, NAICS: %s, Confidence: %s. Why not eligible? Give 1 short reason and 1 brief recommendation (max 50 words total).",
            businessType, naicsCode, confidenceCode
        );
        return callGeminiAPI(prompt);
    }

    public String getCompaniesWithLowConfidence() throws Exception {
        List<Company> companies = companyRepository.findAll();
        List<CompanyResponseDTO> lowConfidenceCompanies = new ArrayList<>();
        
        for (Company c : companies) {
            if (c.getConfidenceCode() != null && Integer.parseInt(c.getConfidenceCode()) < 5) {
                MatchingData md = buildMatchingData(c);
                AppendedData ad = new AppendedData(c);
                GeminiRecommendation recommendation = generateRecommendation(c);
                lowConfidenceCompanies.add(new CompanyResponseDTO(md, ad, recommendation));
            }
        }
        
        return "Found " + lowConfidenceCompanies.size() + " companies with confidence < 5 needing recommendations";
    }

    private MatchingData buildMatchingData(Company company) {
        MatchingData md = new MatchingData();
        md.setRequestId(company.getId().intValue());
        md.setLayout(company.getLayout());
        md.setMatchMethod(company.getMatchMethod());
        md.setMatchGrade(company.getMatchGrade());
        md.setConfidenceCode(company.getConfidenceCode());
        md.setDuns(company.getDuns());
        md.setMatchesRemaining(company.getMatchesRemaining());
        md.setBemfab(company.getBemfab());
        return md;
    }

    private GeminiRecommendation generateRecommendation(Company company) {
        GeminiRecommendation recommendation = new GeminiRecommendation();
        
        if (company.getConfidenceCode() != null && Integer.parseInt(company.getConfidenceCode()) < 5) {
            recommendation.setNeedsRecommendation(true);
            try {
                String geminiResponse = getShortRecommendation(
                    company.getLineOfBusiness(),
                    company.getNaics1Code(),
                    company.getConfidenceCode()
                );
                String[] parts = geminiResponse.split("\\.", 2);
                recommendation.setEligibilityReason(parts.length > 0 ? parts[0] : "Low confidence score");
                recommendation.setShortRecommendation(parts.length > 1 ? parts[1] : "Improve data quality");
            } catch (Exception e) {
                recommendation.setEligibilityReason("Low confidence score");
                recommendation.setShortRecommendation("Improve data accuracy");
            }
        } else {
            recommendation.setNeedsRecommendation(false);
        }
        
        return recommendation;
    }

    private void validatePayload(JsonNode node) throws Exception {
        String[] requiredFields = {"businessType", "naicsCode", "currentScore", "appetiteStatus", "restrictions", "state", "carrierCriteria"};
        StringBuilder missingFields = new StringBuilder();
        
        for (String field : requiredFields) {
            if (node.path(field).isMissingNode() || node.path(field).isNull() ||
                (node.path(field).isTextual() && node.path(field).asText().isEmpty())) {
                missingFields.append(field).append(", ");
            }
        }
        
        if (!node.path("restrictions").isArray() && !node.path("restrictions").isObject()) {
            missingFields.append("restrictions (should be array/object), ");
        }
        if (!node.path("carrierCriteria").isArray() && !node.path("carrierCriteria").isObject()) {
            missingFields.append("carrierCriteria (should be array/object), ");
        }
        
        if (missingFields.length() > 0) {
            throw new Exception("Missing or invalid required fields: " + missingFields.substring(0, missingFields.length() - 2));
        }
    }

    private String buildPrompt(JsonNode node) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Given the following business data, provide actionable recommendations to improve appetite score and eligibility.\n");
        prompt.append("Business Type: ").append(node.path("businessType").asText("")).append("\n");
        prompt.append("NAICS Code: ").append(node.path("naicsCode").asText("")).append("\n");
        prompt.append("Current Score: ").append(node.path("currentScore").asText("")).append("\n");
        prompt.append("Appetite Status: ").append(node.path("appetiteStatus").asText("")).append("\n");
        prompt.append("Restrictions: ").append(node.path("restrictions").toString()).append("\n");
        prompt.append("State: ").append(node.path("state").asText("")).append("\n");
        prompt.append("Carrier Criteria: ").append(node.path("carrierCriteria").toString()).append("\n");
        prompt.append("List recommendations as bullet points.");
        return prompt.toString();
    }

    private String callGeminiAPI(String prompt) throws Exception {
        String geminiPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(geminiPayload, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, entity, String.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Gemini API error: " + response.getBody());
        }
        
        return response.getBody();
    }
}

package com.example.naicsdemo.service;

import com.example.naicsdemo.dto.AppendedData;
import com.example.naicsdemo.dto.CompanyResponseDTO;
import com.example.naicsdemo.dto.GeminiRecommendation;
import com.example.naicsdemo.dto.MatchingData;
import com.example.naicsdemo.entity.Company;
import com.example.naicsdemo.repository.CompanyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeminiService {
    private static final String GEMINI_API_KEY = "AIzaSyC16tY5K3aehafQ1SfwiGA8nrP2vgMByLE";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;


    // Cache for storing recommendations by business key
    // private final Map<String, String> recommendationCache = new ConcurrentHashMap<>();

    @Autowired
    private CompanyRepository companyRepository;

    // public String getRecommendations(Object payload) throws Exception {
    //     ObjectMapper mapper = new ObjectMapper();
    //     String jsonString = mapper.writeValueAsString(payload);
    //     JsonNode node = mapper.readTree(jsonString);
    //     
    //     String prompt = buildSimplePrompt(node);
    //     String response = callGeminiAPI(prompt);
    //     
    //     return response;
    // }

    // public String listModels() throws Exception {
    //     String url = "https://generativelanguage.googleapis.com/v1/models?key=" + GEMINI_API_KEY;
    //     RestTemplate restTemplate = new RestTemplate();
    //     return restTemplate.getForObject(url, String.class);
    // }

    // public void clearRecommendationCache() {
    //     recommendationCache.clear();
    // }

    // public int getCacheSize() {
    //     return recommendationCache.size();
    // }

    // public String testDirectApiCall() throws Exception {
    //     String testPrompt = "Test: Restaurant business with NAICS 722511 and confidence score 3. Why not eligible?";
    //     return callGeminiAPI(testPrompt);
    // }

    // public String getApiStatus() {
    //     return "Real AI Mode (API working)";
    // }

    // public String getShortRecommendation(String businessType, String naicsCode, String confidenceCode) throws Exception {
    //     return getShortRecommendation(businessType, naicsCode, confidenceCode, null);
    // }
    // 
    // public String getShortRecommendation(String businessType, String naicsCode, String confidenceCode, String duns) throws Exception {
    //     Company selectedCompany = findExactCompany(businessType, naicsCode, duns);
    //     
    //     if (selectedCompany != null) {
    //         String prompt = buildCompanySpecificPrompt(selectedCompany, confidenceCode);
    //         String response = callGeminiAPI(prompt);
    //         
    //         return response;
    //     } else {
    //         String prompt = buildGenericIndustryPrompt(businessType, naicsCode, confidenceCode);
    //         return callGeminiAPI(prompt);
    //     }
    // }
    
    // private Company findExactCompany(String businessType, String naicsCode, String duns) {
    //     try {
    //         List<Company> companies = companyRepository.findAll();
    //         System.out.println("Looking for company with businessType: " + businessType + ", naicsCode: " + naicsCode + ", duns: " + duns);
    //         System.out.println("Available companies in database:");
    //         for (Company c : companies) {
    //             System.out.println("- " + c.getCompanyName() + " (NAICS: " + c.getNaics1Code() + ", DUNS: " + c.getDuns() + ")");
    //         }
    //         
    //         // If wrong data is sent, try to find manufacturing company by NAICS 333612
    //         if ("Restaurant".equals(businessType) && "722511".equals(naicsCode)) {
    //             System.out.println("Detected wrong frontend data, searching for manufacturing company...");
    //             for (Company company : companies) {
    //                 if ("333612".equals(company.getNaics1Code()) || 
    //                     (company.getCompanyName() != null && company.getCompanyName().toLowerCase().contains("gear"))) {
    //                     System.out.println("Found manufacturing company: " + company.getCompanyName());
    //                     return company;
    //                 }
    //             }
    //         }
    //         
    //         if (duns != null && !duns.isEmpty()) {
    //             for (Company company : companies) {
    //                 if (duns.equals(company.getDuns())) {
    //                     System.out.println("Found company by DUNS: " + company.getCompanyName());
    //                     return company;
    //                 }
    //             }
    //         }
    //         
    //         if (naicsCode != null) {
    //             for (Company company : companies) {
    //                 if (naicsCode.equals(company.getNaics1Code())) {
    //                     System.out.println("Found company by NAICS match: " + company.getCompanyName());
    //                     return company;
    //                 }
    //             }
    //         }
    //         
    //         System.out.println("No company found, using generic recommendations");
    //         
    //     } catch (Exception e) {
    //         System.out.println("Error finding company: " + e.getMessage());
    //     }
    //     
    //     return null;
    // }
    
    // private String buildCompanySpecificPrompt(Company company, String confidenceCode) {
    //     StringBuilder prompt = new StringBuilder();
    //     
    //     prompt.append("BUSINESS ANALYSIS FOR: ").append(company.getCompanyName()).append("\n");
    //     prompt.append("Industry: ").append(company.getNaics1Description() != null ? company.getNaics1Description() : "General Business").append("\n");
    //     prompt.append("Location: ").append(company.getCity()).append(", ").append(company.getStateProvince()).append("\n");
    //     prompt.append("Employees: ").append(company.getEmployeesTotal()).append("\n");
    //     prompt.append("Revenue: $").append(company.getSalesVolumeUS()).append("\n");
    //     prompt.append("Risk Score: ").append(confidenceCode).append("/10\n\n");
    //     
    //     prompt.append("Generate recommendations in bullet format:\n");
    //     prompt.append("PRIMARY RISK FACTORS:\n");
    //     prompt.append("• [Risk 1 for this specific company]\n");
    //     prompt.append("• [Risk 2 for this specific company]\n");
    //     prompt.append("• [Risk 3 for this specific company]\n\n");
    //     prompt.append("WHY NOT ELIGIBLE:\n");
    //     prompt.append("• [Specific reason for this company]\n\n");
    //     prompt.append("RECOMMENDED ACTIONS:\n");
    //     prompt.append("• Action Required: [Specific action]\n");
    //     prompt.append("• Expected Outcome: [Specific benefit]\n");
    //     prompt.append("• Timeline: [Timeframe]\n\n");
    //     
    //     return prompt.toString();
    // }
    
    // private String buildGenericIndustryPrompt(String businessType, String naicsCode, String confidenceCode) {
    //     StringBuilder prompt = new StringBuilder();
    //     String industry = getNaicsDescription(naicsCode);
    //     
    //     prompt.append("INDUSTRY ANALYSIS: ").append(industry).append("\n");
    //     prompt.append("Business Type: ").append(businessType).append("\n");
    //     prompt.append("Risk Score: ").append(confidenceCode).append("/10\n\n");
    //     
    //     prompt.append("Generate recommendations in bullet format:\n");
    //     prompt.append("PRIMARY RISK FACTORS:\n");
    //     prompt.append("• [Industry risk 1]\n");
    //     prompt.append("• [Industry risk 2]\n");
    //     prompt.append("• [Industry risk 3]\n\n");
    //     prompt.append("WHY NOT ELIGIBLE:\n");
    //     prompt.append("• [Industry reason]\n\n");
    //     prompt.append("RECOMMENDED ACTIONS:\n");
    //     prompt.append("• Action Required: [Industry action]\n");
    //     prompt.append("• Expected Outcome: [Outcome]\n");
    //     prompt.append("• Timeline: [Timeframe]\n\n");
    //     
    //     return prompt.toString();
    // }
    //companies with low confidence
    // public String getCompaniesWithLowConfidence() throws Exception {
    //     List<Company> companies = companyRepository.findAll();
    //     List<CompanyResponseDTO> lowConfidenceCompanies = new ArrayList<>();
    //     
    //     for (Company c : companies) {
    //         if (c.getConfidenceCode() != null && Integer.parseInt(c.getConfidenceCode()) < 5) {
    //             MatchingData md = buildMatchingData(c);
    //             AppendedData ad = new AppendedData(c);
    //             GeminiRecommendation recommendation = generateRecommendation(c);
    //             lowConfidenceCompanies.add(new CompanyResponseDTO(md, ad, recommendation));
    //         }
    //     }
    //     
    //     return "Found " + lowConfidenceCompanies.size() + " companies with confidence < 5 needing recommendations";
    // }

    // private MatchingData buildMatchingData(Company company) {
    //     MatchingData md = new MatchingData();
    //     md.setRequestId(company.getId().intValue());
    //     md.setLayout(company.getLayout());
    //     md.setMatchMethod(company.getMatchMethod());
    //     md.setMatchGrade(company.getMatchGrade());
    //     md.setConfidenceCode(company.getConfidenceCode());
    //     md.setDuns(company.getDuns());
    //     md.setMatchesRemaining(company.getMatchesRemaining());
    //     md.setBemfab(company.getBemfab());
    //     return md;
    // }

    // private GeminiRecommendation generateRecommendation(Company company) {
    //     GeminiRecommendation recommendation = new GeminiRecommendation();
    //     
    //     if (company.getConfidenceCode() != null && Integer.parseInt(company.getConfidenceCode()) < 5) {
    //         recommendation.setNeedsRecommendation(true);
    //         try {
    //             String geminiResponse = getBusinessSpecificRecommendations(
    //                 company.getLineOfBusiness(),
    //                 company.getNaics1Code(),
    //                 company.getConfidenceCode()
    //             );
    //             
    //             // Parse JSON response
    //             try {
    //                 ObjectMapper mapper = new ObjectMapper();
    //                 JsonNode responseJson = mapper.readTree(geminiResponse);
    //                 String text = responseJson.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    //                 
    //                 String[] parts = text.split("\\*\\*Recommendation:\\*\\*");
    //                 String reason = parts.length > 0 ? parts[0].replace("**Reason:**", "").trim() : "Unable to determine reason";
    //                 String rec = parts.length > 1 ? parts[1].trim() : "Unable to generate recommendations";
    //                 
    //                 recommendation.setEligibilityReason(reason);
    //                 recommendation.setShortRecommendation(rec);
    //             } catch (Exception parseError) {
    //                 recommendation.setEligibilityReason("Unable to parse AI response");
    //                 recommendation.setShortRecommendation("Please try again");
    //             }
    //         } catch (Exception e) {
    //             recommendation.setEligibilityReason("AI service unavailable");
    //             recommendation.setShortRecommendation("Please try again later");
    //         }
    //     } else {
    //         recommendation.setNeedsRecommendation(false);
    //     }
    //     
    //     return recommendation;
    // }

    public String getBusinessSpecificRecommendations(String businessType, String naicsCode, String confidenceScore) throws Exception {
        String prompt = buildBusinessRecommendationPrompt(businessType, naicsCode, confidenceScore);
        return callGeminiAPI(prompt);
    }

    private String buildBusinessRecommendationPrompt(String businessType, String naicsCode, String confidenceScore) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("As an insurance underwriting expert, analyze this business and provide detailed recommendations:\n\n");
        prompt.append("BUSINESS: ").append(businessType != null ? businessType : "General Business").append("\n");
        prompt.append("NAICS CODE: ").append(naicsCode != null ? naicsCode : "N/A");
        if (naicsCode != null) {
            prompt.append(" - ").append(getNaicsDescription(naicsCode));
        }
        prompt.append("\n");
        prompt.append("CONFIDENCE SCORE: ").append(confidenceScore != null ? confidenceScore : "Low").append("/10\n\n");
        
        prompt.append("Provide response in this EXACT format with each bullet point on a separate line:\n\n");
        prompt.append("**Reason:** Why this ").append(businessType != null ? businessType.toLowerCase() : "business").append(" is not eligible for insurance coverage\n\n");
        prompt.append("**Recommendation:**\n");
        prompt.append("• First specific recommendation for this industry\n");
        prompt.append("• Second specific recommendation for compliance\n");
        prompt.append("• Third specific recommendation for risk reduction\n");
        prompt.append("• Fourth specific recommendation for eligibility\n\n");
        prompt.append("IMPORTANT: Each bullet point must be on its own line and start with • symbol.\n");
        
        prompt.append("Make each bullet point specific, actionable, and relevant to ").append(getNaicsDescription(naicsCode)).append(" industry. Each point should be a complete sentence with clear action steps.");
        
        return prompt.toString();
    }

    // private String buildSimplePrompt(JsonNode node) {
    //     StringBuilder prompt = new StringBuilder();
    //     String businessType = node.path("businessType").asText("");
    //     String naicsCode = node.path("naicsCode").asText("");
    //     String currentScore = node.path("currentScore").asText("");
    //     
    //     prompt.append("As an insurance underwriting expert, provide industry-specific recommendations for:\n");
    //     prompt.append("Business: ").append(businessType).append("\n");
    //     prompt.append("NAICS Code: ").append(naicsCode).append(" (").append(getNaicsDescription(naicsCode)).append(")\n");
    //     prompt.append("Current Risk Score: ").append(currentScore).append("\n\n");
    //     prompt.append("Provide recommendations in this exact format:\n\n");
    //     prompt.append("**PRIORITY ACTIONS:**\n");
    //     prompt.append("**[Action 1]:** [Brief description]\n");
    //     prompt.append("**[Action 2]:** [Brief description]\n");
    //     prompt.append("**[Action 3]:** [Brief description]\n\n");
    //     prompt.append("**IMPLEMENTATION TIMELINE:**\n");
    //     prompt.append("**Immediate (0-3 months):** [Quick wins]\n");
    //     prompt.append("**Short-term (3-12 months):** [Major improvements]\n");
    //     prompt.append("**Long-term (12+ months):** [Strategic initiatives]\n\n");
    //     prompt.append("Focus on compliance, safety, and business practices. Keep each point concise and actionable.");
    //     return prompt.toString();
    // }


    


    private String getNaicsDescription(String naicsCode) {
        if (naicsCode == null || naicsCode.isEmpty()) return "General Business";
        
        // Common NAICS code mappings
        switch (naicsCode.substring(0, Math.min(3, naicsCode.length()))) {
            case "333": return "Machinery Manufacturing";
            case "722": return "Food Service & Restaurants";
            case "541": return "Professional Services";
            case "238": return "Construction Specialty Trade";
            case "236": return "Construction of Buildings";
            case "445": return "Food & Beverage Retail";
            case "621": return "Healthcare Services";
            case "811": return "Repair & Maintenance";
            case "561": return "Administrative Services";
            case "423": return "Merchant Wholesalers";
            case "484": return "Truck Transportation";
            case "531": return "Real Estate";
            case "713": return "Amusement & Recreation";
            case "812": return "Personal Care Services";
            case "448": return "Clothing & Accessories Retail";
            case "517": return "Telecommunications";
            default: return "Business Services";
        }
    }





    private String callGeminiAPI(String prompt) throws Exception {
        return callRealGeminiAPI(prompt);
    }

    private String callRealGeminiAPI(String prompt) throws Exception {
        String geminiPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}],\"generationConfig\":{\"temperature\":0.0,\"topP\":0.8,\"topK\":40}}";
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(geminiPayload, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                String errorBody = response.getBody();
                System.out.println("Gemini API Error - Status: " + response.getStatusCodeValue() + ", Body: " + errorBody);
                
                // Check if it's a quota exceeded error
                if (errorBody != null && (errorBody.contains("quota") || errorBody.contains("QUOTA_EXCEEDED") || 
                    errorBody.contains("Resource has been exhausted") || response.getStatusCodeValue() == 429)) {
                    return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"**API Quota Exceeded:** The Gemini API quota has been exhausted. Please try again later when the quota resets.\"}]}}]}";
                }
                
                throw new Exception("API Error: " + response.getStatusCodeValue());
            }
        } catch (RestClientException e) {
            // Handle network or client errors
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("quota") || errorMessage.contains("429"))) {
                return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"**API Quota Exceeded:** The Gemini API quota has been exhausted. Please try again later when the quota resets.\"}]}}]}";
            }
            throw e;
        }
    }


}

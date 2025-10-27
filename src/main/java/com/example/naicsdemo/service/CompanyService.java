package com.example.naicsdemo.service;

import com.example.naicsdemo.dto.AppendedData;
import com.example.naicsdemo.dto.CompanyRequestDTO;
import com.example.naicsdemo.dto.CompanyResponseDTO;
import com.example.naicsdemo.dto.GeminiRecommendation;
import com.example.naicsdemo.dto.MatchingData;
import com.example.naicsdemo.entity.Company;
import com.example.naicsdemo.entity.Rule;
import com.example.naicsdemo.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
// NEWLY ADDED IMPORTS FOR SMART MERGE FUNCTIONALITY
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;


    public Company saveCompany(Company company) {
        // Check for existing company by DUNS to prevent duplicates
        if (company.getDuns() != null) {
            Company existing = companyRepository.findByDuns(company.getDuns());
            if (existing != null) {
                return existing; // Return existing instead of creating duplicate
            }
        }
        return companyRepository.save(company);
    }

    public CompanyResponseDTO saveCompanyFromRequest(CompanyRequestDTO requestDTO) {
        Company company = new Company();

        // Map MatchingData to entity
        MatchingData requestMd = requestDTO.getMatchingData();
        if (requestMd != null) {
            company.setDuns(requestMd.getDuns());
            company.setLayout(requestMd.getLayout());
            company.setMatchMethod(requestMd.getMatchMethod());
            company.setMatchGrade(requestMd.getMatchGrade());
            company.setConfidenceCode(requestMd.getConfidenceCode());
            company.setMatchesRemaining(requestMd.getMatchesRemaining());
            company.setBemfab(requestMd.getBemfab());
        }

        // Map AppendedData to entity
        if (requestDTO.getAppendedData() != null) {
            AppendedData a = requestDTO.getAppendedData();
            company.setCompanyName(a.getCompanyName());
            company.setSecondaryBusinessName(a.getSecondaryBusinessName());
            company.setStreetAddress(a.getStreetAddress());
            company.setCity(a.getCity());
            company.setStateProvince(a.getStateProvince());
            company.setZipCode(a.getZipCode());
            company.setCountry(a.getCountry());
            company.setPhone(a.getPhone());
            company.setUrl(a.getUrl());
            company.setCeoTitle(a.getCeoTitle());
            company.setCeoFirstName(a.getCeoFirstName());
            company.setCeoLastName(a.getCeoLastName());
            company.setCeoName(a.getCeoName());
            company.setLineOfBusiness(a.getLineOfBusiness());
            company.setLocationType(a.getLocationType());
            company.setYearStarted(a.getYearStarted());
            company.setEmployeesOnSite(a.getEmployeesOnSite());
            company.setEmployeesTotal(a.getEmployeesTotal());
            company.setSalesVolumeUS(a.getSalesVolumeUS());
            company.setSic4Digit1(a.getSic4Digit1());
            company.setSic4Digit1Description(a.getSic4Digit1Description());
            company.setSic4Digit2(a.getSic4Digit2());
            company.setSic4Digit2Description(a.getSic4Digit2Description());
            company.setSic8Digit1(a.getSic8Digit1());
            company.setSic8Digit1Description(a.getSic8Digit1Description());
            company.setSic8Digit2(a.getSic8Digit2());
            company.setSic8Digit2Description(a.getSic8Digit2Description());
            company.setNaics1Code(a.getNaics1Code());
            company.setNaics1Description(a.getNaics1Description());
            company.setNaics2Code(a.getNaics2Code());
            company.setNaics2Description(a.getNaics2Description());
        }

        Company saved = saveCompany(company);

        // Build MatchingData response
        MatchingData md = new MatchingData();
        md.setRequestId(saved.getId().intValue());
        md.setLayout(saved.getLayout());
        md.setMatchMethod(saved.getMatchMethod());
        md.setMatchGrade(saved.getMatchGrade());
        md.setConfidenceCode(saved.getConfidenceCode());
        md.setDuns(saved.getDuns());
        md.setMatchesRemaining(saved.getMatchesRemaining());
        md.setBemfab(saved.getBemfab());

        return new CompanyResponseDTO(md,
                requestDTO.getAppendedData() != null ? requestDTO.getAppendedData() : new AppendedData(saved),
                null);
    }

    public List<CompanyResponseDTO> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        List<CompanyResponseDTO> response = new ArrayList<>();
        for (Company c : companies) {
            MatchingData md = buildMatchingData(c);
            AppendedData ad = new AppendedData(c);
            response.add(new CompanyResponseDTO(md, ad, null));
        }
        return response;
    }

    public Page<CompanyResponseDTO> getAllCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(pageable);
        List<CompanyResponseDTO> response = new ArrayList<>();
        for (Company c : companies.getContent()) {
            MatchingData md = buildMatchingData(c);
            AppendedData ad = new AppendedData(c);
            response.add(new CompanyResponseDTO(md, ad, null));
        }
        return new PageImpl<>(response, pageable, companies.getTotalElements());
    }

    public long getCompanyCount() {
        return companyRepository.count();
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    public List<CompanyResponseDTO> saveCompaniesFromRequest(List<CompanyRequestDTO> requestDTOs) {
        List<CompanyResponseDTO> responses = new ArrayList<>();
        for (CompanyRequestDTO dto : requestDTOs) {
            responses.add(saveCompanyFromRequest(dto));
        }
        return responses;
    }



    public List<CompanyResponseDTO> searchCompanies(String keyword) {
        List<Company> companies = companyRepository.findAll();
        List<CompanyResponseDTO> response = new ArrayList<>();
        for (Company c : companies) {
            if (matchesKeyword(c, keyword)) {
                MatchingData md = buildMatchingData(c);
                AppendedData ad = new AppendedData(c);
                response.add(new CompanyResponseDTO(md, ad, null));
            }
        }
        return response;
    }

    // NEWLY ADDED - Advanced search method (replaces frontend filtering logic)
    // Supports multiple search criteria: keyword, state, naicsCode, duns
    // Called by /api/companies/search/advanced endpoint
    public List<CompanyResponseDTO> advancedSearch(String keyword, String state, String naicsCode, String duns) {
        List<Company> companies = companyRepository.findAll();
        List<CompanyResponseDTO> response = new ArrayList<>();
        for (Company c : companies) {
            if (matchesAdvancedCriteria(c, keyword, state, naicsCode, duns)) {
                MatchingData md = buildMatchingData(c);
                AppendedData ad = new AppendedData(c);
                response.add(new CompanyResponseDTO(md, ad, null));
            }
        }
        return response;
    }

    private boolean matchesKeyword(Company c, String keyword) {
        if (c.getDuns() != null && c.getDuns().contains(keyword)) return true;
        if (c.getNaics1Code() != null && c.getNaics1Code().contains(keyword)) return true;
        if (c.getNaics1Description() != null && c.getNaics1Description().toLowerCase().contains(keyword.toLowerCase()))
            return true;
        if (c.getNaics2Code() != null && c.getNaics2Code().contains(keyword)) return true;
        if (c.getNaics2Description() != null && c.getNaics2Description().toLowerCase().contains(keyword.toLowerCase()))
            return true;
        if (c.getCompanyName() != null && c.getCompanyName().toLowerCase().contains(keyword.toLowerCase()))
            return true;
        return false;
    }

    // NEWLY ADDED - Advanced criteria matching logic (similar to frontend logic)
    // Replicates the same search behavior that was in React component
    private boolean matchesAdvancedCriteria(Company c, String keyword, String state, String naicsCode, String duns) {
        // Check keyword match (DUNS, NAICS codes, NAICS descriptions) - same as frontend
        boolean keywordMatch = keyword == null || keyword.trim().isEmpty() || matchesKeyword(c, keyword);
        
        // Check state match - same as frontend
        boolean stateMatch = state == null || state.trim().isEmpty() || 
            (c.getStateProvince() != null && c.getStateProvince().equalsIgnoreCase(state));
        
        // Check NAICS code match - additional filtering capability
        boolean naicsMatch = naicsCode == null || naicsCode.trim().isEmpty() ||
            (c.getNaics1Code() != null && c.getNaics1Code().contains(naicsCode)) ||
            (c.getNaics2Code() != null && c.getNaics2Code().contains(naicsCode));
        
        // Check DUNS match - additional filtering capability
        boolean dunsMatch = duns == null || duns.trim().isEmpty() ||
            (c.getDuns() != null && c.getDuns().contains(duns));
        
        return keywordMatch && stateMatch && naicsMatch && dunsMatch;
    }

    // ========================================
    // NEWLY ADDED - SMART MERGE FUNCTIONALITY
    // ========================================
    // Main method: Syncs JSON file data with database automatically
    // Handles: ADD new records, UPDATE changed records, DELETE removed records
    public void smartMergeJsonData(List<CompanyRequestDTO> jsonCompanies) {
        System.out.println("🔄 Starting Smart Merge - Syncing JSON with database...");
        
        // Get all existing companies from database
        List<Company> dbCompanies = companyRepository.findAll();
        
        // Create maps for efficient lookup
        Map<String, Company> dbCompanyMap = dbCompanies.stream()
            .filter(c -> c.getDuns() != null)
            .collect(Collectors.toMap(Company::getDuns, c -> c));
        
        Map<String, CompanyRequestDTO> jsonCompanyMap = jsonCompanies.stream()
            .filter(dto -> dto.getMatchingData() != null && dto.getMatchingData().getDuns() != null)
            .collect(Collectors.toMap(dto -> dto.getMatchingData().getDuns(), dto -> dto));
        
        int added = 0, updated = 0, deleted = 0;
        
        // 1. Add new companies and update existing ones
        for (CompanyRequestDTO jsonDto : jsonCompanies) {
            if (jsonDto.getMatchingData() == null || jsonDto.getMatchingData().getDuns() == null) continue;
            
            String duns = jsonDto.getMatchingData().getDuns();
            Company existingCompany = dbCompanyMap.get(duns);
            
            if (existingCompany == null) {
                // New company - add to database
                saveCompanyFromRequest(jsonDto);
                added++;
            } else {
                // Existing company - check if update needed
                if (isCompanyDataDifferent(existingCompany, jsonDto)) {
                    updateCompanyFromRequest(existingCompany, jsonDto);
                    updated++;
                }
            }
        }
        
        // 2. Delete companies that are no longer in JSON
        for (Company dbCompany : dbCompanies) {
            if (dbCompany.getDuns() != null && !jsonCompanyMap.containsKey(dbCompany.getDuns())) {
                companyRepository.delete(dbCompany);
                deleted++;
            }
        }
        
        System.out.println("✅ Smart Merge completed: " + added + " added, " + updated + " updated, " + deleted + " deleted");
    }
    
    // ========================================
    // NEWLY ADDED - SMART MERGE HELPER METHOD
    // ========================================
    // Detects if company data has changed between JSON and database
    private boolean isCompanyDataDifferent(Company dbCompany, CompanyRequestDTO jsonDto) {
        if (jsonDto.getAppendedData() == null) return false;
        
        AppendedData jsonData = jsonDto.getAppendedData();
        
        // Compare key fields to detect changes
        return !Objects.equals(dbCompany.getCompanyName(), jsonData.getCompanyName()) ||
               !Objects.equals(dbCompany.getStateProvince(), jsonData.getStateProvince()) ||
               !Objects.equals(dbCompany.getNaics1Code(), jsonData.getNaics1Code()) ||
               !Objects.equals(dbCompany.getNaics1Description(), jsonData.getNaics1Description()) ||
               !Objects.equals(dbCompany.getConfidenceCode(), jsonDto.getMatchingData().getConfidenceCode());
    }
    
    // ========================================
    // NEWLY ADDED - SMART MERGE UPDATE METHOD
    // ========================================
    // Updates existing database record with new JSON data
    private void updateCompanyFromRequest(Company existingCompany, CompanyRequestDTO requestDTO) {
        // Update MatchingData fields
        if (requestDTO.getMatchingData() != null) {
            MatchingData md = requestDTO.getMatchingData();
            existingCompany.setLayout(md.getLayout());
            existingCompany.setMatchMethod(md.getMatchMethod());
            existingCompany.setMatchGrade(md.getMatchGrade());
            existingCompany.setConfidenceCode(md.getConfidenceCode());
            existingCompany.setMatchesRemaining(md.getMatchesRemaining());
            existingCompany.setBemfab(md.getBemfab());
        }
        
        // Update AppendedData fields
        if (requestDTO.getAppendedData() != null) {
            AppendedData a = requestDTO.getAppendedData();
            existingCompany.setCompanyName(a.getCompanyName());
            existingCompany.setSecondaryBusinessName(a.getSecondaryBusinessName());
            existingCompany.setStreetAddress(a.getStreetAddress());
            existingCompany.setCity(a.getCity());
            existingCompany.setStateProvince(a.getStateProvince());
            existingCompany.setZipCode(a.getZipCode());
            existingCompany.setCountry(a.getCountry());
            existingCompany.setPhone(a.getPhone());
            existingCompany.setUrl(a.getUrl());
            existingCompany.setCeoTitle(a.getCeoTitle());
            existingCompany.setCeoFirstName(a.getCeoFirstName());
            existingCompany.setCeoLastName(a.getCeoLastName());
            existingCompany.setCeoName(a.getCeoName());
            existingCompany.setLineOfBusiness(a.getLineOfBusiness());
            existingCompany.setLocationType(a.getLocationType());
            existingCompany.setYearStarted(a.getYearStarted());
            existingCompany.setEmployeesOnSite(a.getEmployeesOnSite());
            existingCompany.setEmployeesTotal(a.getEmployeesTotal());
            existingCompany.setSalesVolumeUS(a.getSalesVolumeUS());
            existingCompany.setSic4Digit1(a.getSic4Digit1());
            existingCompany.setSic4Digit1Description(a.getSic4Digit1Description());
            existingCompany.setSic4Digit2(a.getSic4Digit2());
            existingCompany.setSic4Digit2Description(a.getSic4Digit2Description());
            existingCompany.setSic8Digit1(a.getSic8Digit1());
            existingCompany.setSic8Digit1Description(a.getSic8Digit1Description());
            existingCompany.setSic8Digit2(a.getSic8Digit2());
            existingCompany.setSic8Digit2Description(a.getSic8Digit2Description());
            existingCompany.setNaics1Code(a.getNaics1Code());
            existingCompany.setNaics1Description(a.getNaics1Description());
            existingCompany.setNaics2Code(a.getNaics2Code());
            existingCompany.setNaics2Description(a.getNaics2Description());
        }
        
        companyRepository.save(existingCompany);
    }

    // Get companies that are not eligible based on dynamic rules
    public Page<CompanyResponseDTO> getNotEligibleCompanies(Pageable pageable) {
        // Default rule: confidence code < 5
        List<Rule> defaultRules = List.of(
            Rule.builder()
                .parameter("confidenceCode")
                .operator('<')
                .value("5")
                .description("Confidence code less than 5")
                .build()
        );
        
        return getNotEligibleCompanies(defaultRules, pageable);
    }
    
    public Page<CompanyResponseDTO> getNotEligibleCompanies(List<Rule> rules, Pageable pageable) {
        List<Company> allCompanies = companyRepository.findAll();
        List<CompanyResponseDTO> notEligible = new ArrayList<>();
        
        for (Company c : allCompanies) {
            if (!evaluateRules(c, rules)) {
                MatchingData md = buildMatchingData(c);
                AppendedData ad = new AppendedData(c);
                notEligible.add(new CompanyResponseDTO(md, ad, null));
            }
        }
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), notEligible.size());
        List<CompanyResponseDTO> pageContent = start < notEligible.size() ? 
            notEligible.subList(start, end) : new ArrayList<>();
        
        return new PageImpl<>(pageContent, pageable, notEligible.size());
    }

    private boolean evaluateRules(Company company, List<Rule> rules) {
        for (Rule rule : rules) {
            String fieldValue = getFieldValue(company, rule.getParameter());
            if (fieldValue == null) continue;

            switch (rule.getOperator()) {
                case '=':
                    if (!fieldValue.equals(rule.getValue())) return false;
                    break;
                case '~':
                    if (!fieldValue.contains(rule.getValue())) return false;
                    break;
                case '>':
                    try {
                        if (Double.parseDouble(fieldValue) <= Double.parseDouble(rule.getValue())) return false;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
                case '<':
                    try {
                        if (Double.parseDouble(fieldValue) >= Double.parseDouble(rule.getValue())) return false;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }
    
    private String getFieldValue(Company company, String fieldName) {
        switch (fieldName) {
            case "confidenceCode": return company.getConfidenceCode();
            case "matchGrade": return company.getMatchGrade();
            case "employeesTotal": return company.getEmployeesTotal();
            case "salesVolumeUS": return company.getSalesVolumeUS();
            case "yearStarted": return company.getYearStarted();
            case "stateProvince": return company.getStateProvince();
            case "naics1Code": return company.getNaics1Code();
            case "companyName": return company.getCompanyName();
            default: return null;
        }
    }

    public MatchingData buildMatchingData(Company saved) {
        MatchingData md = new MatchingData();
        md.setRequestId(saved.getId().intValue());
        md.setLayout(saved.getLayout());
        md.setMatchMethod(saved.getMatchMethod());
        md.setMatchGrade(saved.getMatchGrade());
        md.setConfidenceCode(saved.getConfidenceCode());
        md.setDuns(saved.getDuns());
        md.setMatchesRemaining(saved.getMatchesRemaining());
        md.setBemfab(saved.getBemfab());
        return md;
    }


    }

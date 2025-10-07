package com.example.naicsdemo.service;

import com.example.naicsdemo.dto.AppendedData;
import com.example.naicsdemo.dto.CompanyRequestDTO;
import com.example.naicsdemo.dto.CompanyResponseDTO;
import com.example.naicsdemo.dto.GeminiRecommendation;
import com.example.naicsdemo.dto.MatchingData;
import com.example.naicsdemo.entity.Company;
import com.example.naicsdemo.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

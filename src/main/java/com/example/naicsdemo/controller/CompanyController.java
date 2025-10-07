package com.example.naicsdemo.controller;

import com.example.naicsdemo.dto.CompanyRequestDTO;
import com.example.naicsdemo.dto.CompanyResponseDTO;
import com.example.naicsdemo.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompanyController {

    private final CompanyService companyService;

    // ✅ Single company insert
    @PostMapping
    public ResponseEntity<CompanyResponseDTO> addCompany(@RequestBody CompanyRequestDTO requestDTO) {
        CompanyResponseDTO response = companyService.saveCompanyFromRequest(requestDTO);
        return ResponseEntity.ok(response);
    }

    // ✅ Bulk insert endpoint
    @PostMapping("/bulk")
    public ResponseEntity<List<CompanyResponseDTO>> addCompanies(@RequestBody List<CompanyRequestDTO> requestDTOs) {
        return ResponseEntity.ok(companyService.saveCompaniesFromRequest(requestDTOs));
    }

    // ✅ Get all companies (simple list for UI)
    @GetMapping("/all")
    public ResponseEntity<List<CompanyResponseDTO>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    // ✅ Get all companies with pagination
    @GetMapping("/all/paginated")
    public ResponseEntity<Page<CompanyResponseDTO>> getAllCompaniesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(companyService.getAllCompanies(PageRequest.of(page, size)));
    }

    // ✅ Search companies
    @GetMapping("/search")
    public ResponseEntity<List<CompanyResponseDTO>> searchCompanies(@RequestParam String keyword) {
        return ResponseEntity.ok(companyService.searchCompanies(keyword));
    }

    // ✅ Test endpoint
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        long count = companyService.getCompanyCount();
        return ResponseEntity.ok("API is working! Total companies: " + count);
    }


}

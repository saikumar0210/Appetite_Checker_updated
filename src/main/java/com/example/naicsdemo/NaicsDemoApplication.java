package com.example.naicsdemo;

import com.example.naicsdemo.dto.CompanyRequestDTO;
import com.example.naicsdemo.service.CompanyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class NaicsDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaicsDemoApplication.class, args);
	}

	@Component
	@Order(1)
	static class DataLoader implements ApplicationRunner {
		private final CompanyService companyService;

		public DataLoader(CompanyService companyService) {
			this.companyService = companyService;
		}

		@Override
		public void run(ApplicationArguments args) throws Exception {
			// ========================================
			// UPDATED FOR SMART MERGE FUNCTIONALITY
			// ========================================
			// Changed from simple count check to Smart Merge sync
			ObjectMapper mapper = new ObjectMapper();
			TypeReference<List<CompanyRequestDTO>> typeReference = new TypeReference<>() {};
			InputStream inputStream = getClass().getResourceAsStream("/companies.json");
			List<CompanyRequestDTO> companies = mapper.readValue(inputStream, typeReference);
			
			// Use Smart Merge to sync JSON data with database
			companyService.smartMergeJsonData(companies);
		}
	}
}

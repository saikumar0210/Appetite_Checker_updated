package com.example.naicsdemo.dto;

import com.example.naicsdemo.entity.Company;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppendedData {

    @JsonProperty("Company Name")
    private String companyName;

    @JsonProperty("Secondary Business Name")
    private String secondaryBusinessName;

    @JsonProperty("Street Address")
    private String streetAddress;

    @JsonProperty("City")
    private String city;

    @JsonProperty("State/Province")
    private String stateProvince;

    @JsonProperty("ZIP Code")
    private String zipCode;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("URL")
    private String url;

    @JsonProperty("CEO Title")
    private String ceoTitle;

    @JsonProperty("CEO First Name")
    private String ceoFirstName;

    @JsonProperty("CEO Last Name")
    private String ceoLastName;

    @JsonProperty("CEO Name")
    private String ceoName;

    @JsonProperty("Line of Business")
    private String lineOfBusiness;

    @JsonProperty("Location Type")
    private String locationType;

    @JsonProperty("Year Started")
    private String yearStarted;

    @JsonProperty("Employees on Site")
    private String employeesOnSite;

    @JsonProperty("Employees Total")
    private String employeesTotal;

    @JsonProperty("Sales Volume in US$")
    private String salesVolumeUS;

    @JsonProperty("4 Digit SIC 1")
    private String sic4Digit1;

    @JsonProperty("4 Digit SIC 1 Description")
    private String sic4Digit1Description;

    @JsonProperty("4 Digit SIC 2")
    private String sic4Digit2;

    @JsonProperty("4 Digit SIC 2 Description")
    private String sic4Digit2Description;

    @JsonProperty("8 Digit SIC 1")
    private String sic8Digit1;

    @JsonProperty("8 Digit SIC 1 Description")
    private String sic8Digit1Description;

    @JsonProperty("8 Digit SIC 2")
    private String sic8Digit2;

    @JsonProperty("8 Digit SIC 2 Description")
    private String sic8Digit2Description;

    @JsonProperty("NAICS 1 Code")
    private String naics1Code;

    @JsonProperty("NAICS 1 Description")
    private String naics1Description;

    @JsonProperty("NAICS 2 Code")
    private String naics2Code;

    @JsonProperty("NAICS 2 Description")
    private String naics2Description;

    // Constructor to map from Company entity
    public AppendedData(Company company) {
        if (company != null) {
            this.companyName = company.getCompanyName();
            this.secondaryBusinessName = company.getSecondaryBusinessName();
            this.streetAddress = company.getStreetAddress();
            this.city = company.getCity();
            this.stateProvince = company.getStateProvince();
            this.zipCode = company.getZipCode();
            this.country = company.getCountry();
            this.phone = company.getPhone();
            this.url = company.getUrl();
            this.ceoTitle = company.getCeoTitle();
            this.ceoFirstName = company.getCeoFirstName();
            this.ceoLastName = company.getCeoLastName();
            this.ceoName = company.getCeoName();
            this.lineOfBusiness = company.getLineOfBusiness();
            this.locationType = company.getLocationType();
            this.yearStarted = company.getYearStarted();
            this.employeesOnSite = company.getEmployeesOnSite();
            this.employeesTotal = company.getEmployeesTotal();
            this.salesVolumeUS = company.getSalesVolumeUS();
            this.sic4Digit1 = company.getSic4Digit1();
            this.sic4Digit1Description = company.getSic4Digit1Description();
            this.sic4Digit2 = company.getSic4Digit2();
            this.sic4Digit2Description = company.getSic4Digit2Description();
            this.sic8Digit1 = company.getSic8Digit1();
            this.sic8Digit1Description = company.getSic8Digit1Description();
            this.sic8Digit2 = company.getSic8Digit2();
            this.sic8Digit2Description = company.getSic8Digit2Description();
            this.naics1Code = company.getNaics1Code();
            this.naics1Description = company.getNaics1Description();
            this.naics2Code = company.getNaics2Code();
            this.naics2Description = company.getNaics2Description();
        }
    }

    public AppendedData() {
    }
}

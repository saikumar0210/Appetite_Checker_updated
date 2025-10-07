package com.example.naicsdemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MatchingData fields
    private String duns;
    private String layout;
    private String matchMethod;
    private String matchGrade;
    private String confidenceCode;
    private Integer matchesRemaining;
    private String bemfab;

    // AppendedData fields
    private String companyName;
    private String secondaryBusinessName;
    private String streetAddress;
    private String city;
    private String stateProvince;
    private String zipCode;
    private String country;
    private String phone;
    private String url;
    private String ceoTitle;
    private String ceoFirstName;
    private String ceoLastName;
    private String ceoName;
    private String lineOfBusiness;
    private String locationType;
    private String yearStarted;
    private String employeesOnSite;
    private String employeesTotal;
    private String salesVolumeUS;
    private String sic4Digit1;
    private String sic4Digit1Description;
    private String sic4Digit2;
    private String sic4Digit2Description;
    private String sic8Digit1;
    private String sic8Digit1Description;
    private String sic8Digit2;
    private String sic8Digit2Description;
    private String naics1Code;
    private String naics1Description;
    private String naics2Code;
    private String naics2Description;
}

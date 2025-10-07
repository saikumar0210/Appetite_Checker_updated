package com.example.naicsdemo.repository;

import com.example.naicsdemo.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company findByDuns(String duns);
}

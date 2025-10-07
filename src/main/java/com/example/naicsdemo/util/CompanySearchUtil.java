package com.example.naicsdemo.util;

import com.example.naicsdemo.entity.Company;
import java.util.ArrayList;
import java.util.List;

public class CompanySearchUtil {

    public static List<Company> searchCompanies(List<Company> companies, String keyword) {
        List<Company> filtered = new ArrayList<>();
        String k = keyword.toLowerCase();

        for (Company c : companies) {
            if ((c.getCompanyName() != null && c.getCompanyName().toLowerCase().contains(k)) ||
                    (c.getNaics1Code() != null && c.getNaics1Code().contains(k)) ||
                    (c.getNaics1Description() != null && c.getNaics1Description().toLowerCase().contains(k)) ||
                    (c.getNaics2Code() != null && c.getNaics2Code().contains(k)) ||
                    (c.getNaics2Description() != null && c.getNaics2Description().toLowerCase().contains(k)) ||
                    (c.getDuns() != null && c.getDuns().contains(k))
            ) {
                filtered.add(c);
            }
        }
        return filtered;
    }
}

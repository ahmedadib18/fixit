package com.fixit.fixit.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "helper_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"helper_id", "category_id"}))
public class HelperCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "helper_id", nullable = false)
    private Helper helper;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "fixed_rate", precision = 10, scale = 2)
    private BigDecimal fixedRate;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    // =====================
    // Getters and Setters
    // =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Helper getHelper() { return helper; }
    public void setHelper(Helper helper) { this.helper = helper; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public BigDecimal getFixedRate() { return fixedRate; }
    public void setFixedRate(BigDecimal fixedRate) { this.fixedRate = fixedRate; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }
}
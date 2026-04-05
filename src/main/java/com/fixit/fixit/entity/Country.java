package com.fixit.fixit.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "countries")
public class Country {

    @Id
    @Column(name = "id")
    private Long id;


    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "iso2", nullable = false, unique = true, length = 2)
    private String iso2;

    @Column(name = "iso3", length = 3)
    private String iso3;

    @Column(name = "phone_code", length = 10)
    private String phoneCode;

    // =====================
    // Getters and Setters
    // =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIso2() { return iso2; }
    public void setIso2(String iso2) { this.iso2 = iso2; }

    public String getIso3() { return iso3; }
    public void setIso3(String iso3) { this.iso3 = iso3; }

    public String getPhoneCode() { return phoneCode; }
    public void setPhoneCode(String phoneCode) { this.phoneCode = phoneCode; }
}
package com.fixit.fixit.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "cities")
public class City {

    @Id
    @Column(name = "id")
    private Long id;


    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "state_name", length = 100)
    private String stateName;

    // =====================
    // Getters and Setters
    // =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }

    public String getStateName() { return stateName; }
    public void setStateName(String stateName) { this.stateName = stateName; }
}
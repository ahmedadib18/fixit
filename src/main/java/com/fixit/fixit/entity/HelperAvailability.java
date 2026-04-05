package com.fixit.fixit.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "helper_availabilities")
public class HelperAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "helper_id", nullable = false)
    private Helper helper;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "timezone", length = 50)
    private String timezone = "UTC";

    @Column(name = "is_active")
    private Boolean isActive = true;

    // =====================
    // Getters and Setters
    // =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Helper getHelper() { return helper; }
    public void setHelper(Helper helper) { this.helper = helper; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
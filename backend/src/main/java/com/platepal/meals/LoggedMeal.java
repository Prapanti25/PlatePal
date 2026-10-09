package com.platepal.meals;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.platepal.profile.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "logged_meals")
public class LoggedMeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(name = "dish_name", nullable = false, length = 240)
    private String dishName;

    @Column(name = "estimated_calories")
    private Integer estimatedCalories;

    @Column(name = "protein_grams", precision = 8, scale = 2)
    private BigDecimal proteinGrams;

    @Column(name = "carbs_grams", precision = 8, scale = 2)
    private BigDecimal carbsGrams;

    @Column(name = "fats_grams", precision = 8, scale = 2)
    private BigDecimal fatsGrams;

    @CreationTimestamp
    @Column(name = "logged_at", nullable = false, updatable = false)
    private Instant loggedAt;

    protected LoggedMeal() {
    }

    public LoggedMeal(AppUser user, String imageUrl, VisionAnalysis analysis) {
        this.user = user;
        this.imageUrl = imageUrl;
        this.dishName = analysis.dishName();
        this.estimatedCalories = analysis.estimatedCalories();
        this.proteinGrams = analysis.proteinGrams();
        this.carbsGrams = analysis.carbsGrams();
        this.fatsGrams = analysis.fatsGrams();
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDishName() {
        return dishName;
    }

    public Integer getEstimatedCalories() {
        return estimatedCalories;
    }

    public BigDecimal getProteinGrams() {
        return proteinGrams;
    }

    public BigDecimal getCarbsGrams() {
        return carbsGrams;
    }

    public BigDecimal getFatsGrams() {
        return fatsGrams;
    }

    public Instant getLoggedAt() {
        return loggedAt;
    }
}
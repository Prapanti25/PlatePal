package com.platepal.planner;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.platepal.profile.AppUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal_plans")
public class MealPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "ends_on", nullable = false)
    private LocalDate endsOn;

    @Column(nullable = false, length = 24)
    private String status = "ACTIVE";

    @Column(name = "is_fallback", nullable = false)
    private boolean fallback;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeEntity> recipes = new ArrayList<>();

    protected MealPlan() {
    }

    public MealPlan(AppUser user, LocalDate startsOn, LocalDate endsOn, boolean fallback) {
        this.user = user;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
        this.fallback = fallback;
    }

    public void addRecipe(RecipeEntity recipe) {
        recipes.add(recipe);
        recipe.setMealPlan(this);
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public String getStatus() {
        return status;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void archive() {
        status = "ARCHIVED";
    }

    public List<RecipeEntity> getRecipes() {
        return recipes;
    }
}
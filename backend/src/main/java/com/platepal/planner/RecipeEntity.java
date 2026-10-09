package com.platepal.planner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "recipes")
public class RecipeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @Column(name = "day_of_week", nullable = false)
    private short dayOfWeek;

    @Column(name = "meal_type", nullable = false, length = 24)
    private String mealType;

    @Column(nullable = false, length = 240)
    private String name;

    @Column(name = "prep_minutes")
    private Integer prepMinutes;

    @Column(name = "estimated_cost_bdt", precision = 12, scale = 2)
    private BigDecimal estimatedCostBdt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<Ingredient> ingredients = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> instructions = new ArrayList<>();

    protected RecipeEntity() {
    }

    public RecipeEntity(short dayOfWeek, RecipeDraft draft) {
        this.dayOfWeek = dayOfWeek;
        this.mealType = draft.mealType();
        this.name = draft.name();
        this.prepMinutes = draft.prepMinutes();
        this.estimatedCostBdt = draft.estimatedCostBdt();
        this.ingredients = draft.ingredients();
        this.instructions = draft.instructions();
    }

    void setMealPlan(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }

    public Long getId() {
        return id;
    }

    public short getDayOfWeek() {
        return dayOfWeek;
    }

    public String getMealType() {
        return mealType;
    }

    public String getName() {
        return name;
    }

    public Integer getPrepMinutes() {
        return prepMinutes;
    }

    public BigDecimal getEstimatedCostBdt() {
        return estimatedCostBdt;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }
}
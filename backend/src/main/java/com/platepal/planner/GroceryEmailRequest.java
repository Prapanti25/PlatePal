package com.platepal.planner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GroceryEmailRequest(@NotBlank @Email String email) {
}
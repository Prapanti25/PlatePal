package com.platepal.meals;

import jakarta.validation.constraints.NotBlank;

public record LogImageRequest(@NotBlank String imageUrl) {
}
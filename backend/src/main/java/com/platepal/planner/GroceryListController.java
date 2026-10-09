package com.platepal.planner;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/grocery-list")
public class GroceryListController {
    private final GroceryListService groceryListService;
    private final BrevoEmailService emailService;

    public GroceryListController(GroceryListService groceryListService, BrevoEmailService emailService) {
        this.groceryListService = groceryListService;
        this.emailService = emailService;
    }

    @GetMapping
    public List<GroceryItemView> getList(Principal principal) {
        return groceryListService.list(principal.getName());
    }

    @PatchMapping("/{id}/pantry")
    public GroceryItemView updatePantry(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody PantryUpdateRequest request) {
        return groceryListService.updatePantry(principal.getName(), id, request.inPantry());
    }

    @PostMapping("/email")
    public ResponseEntity<Void> sendEmail(Principal principal, @Valid @RequestBody GroceryEmailRequest request) {
        List<GroceryItemView> items = groceryListService.list(principal.getName());
        emailService.sendShoppingList(request.email(), items);
        return ResponseEntity.noContent().build();
    }
}
package com.cts.connectease.config;

import com.cts.connectease.dto.CategoryFilterSchema;
import com.cts.connectease.dto.FilterField;
import com.cts.connectease.dto.PriceRange;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class CategoryFilterConfig {
    
    private final Map<String, CategoryFilterSchema> filterSchemas;
    
    public CategoryFilterConfig() {
        this.filterSchemas = initializeFilterSchemas();
    }
    
    private Map<String, CategoryFilterSchema> initializeFilterSchemas() {
        Map<String, CategoryFilterSchema> schemas = new HashMap<>();
        
        // PG/Hostels
        schemas.put("pg", CategoryFilterSchema.builder()
            .categoryId("pg")
            .priceRange(new PriceRange(new BigDecimal("5000"), new BigDecimal("15000")))
            .filters(Arrays.asList(
                FilterField.builder()
                    .key("genderType")
                    .label("Gender Type")
                    .type("dropdown")
                    .options(Arrays.asList("Male", "Female", "Unisex"))
                    .build(),
                FilterField.builder()
                    .key("foodType")
                    .label("Food Type")
                    .type("checkbox")
                    .options(Arrays.asList("Veg", "Non-Veg", "Both"))
                    .build(),
                FilterField.builder()
                    .key("occupancy")
                    .label("Occupancy")
                    .type("dropdown")
                    .options(Arrays.asList("Single", "Double", "Triple", "Four"))
                    .build(),
                FilterField.builder()
                    .key("amenities")
                    .label("Amenities")
                    .type("checkbox")
                    .options(Arrays.asList("WiFi", "AC", "Power Backup", "Laundry", "Parking"))
                    .build()
            ))
            .build());
        
        // Food Services
        schemas.put("food", CategoryFilterSchema.builder()
            .categoryId("food")
            .priceRange(new PriceRange(new BigDecimal("50"), new BigDecimal("500")))
            .filters(Arrays.asList(
                FilterField.builder()
                    .key("cuisineType")
                    .label("Cuisine Type")
                    .type("checkbox")
                    .options(Arrays.asList("North Indian", "South Indian", "Chinese", "Continental", "Fast Food"))
                    .build(),
                FilterField.builder()
                    .key("mealType")
                    .label("Meal Type")
                    .type("dropdown")
                    .options(Arrays.asList("Breakfast", "Lunch", "Dinner", "Snacks"))
                    .build(),
                FilterField.builder()
                    .key("dietType")
                    .label("Diet Type")
                    .type("checkbox")
                    .options(Arrays.asList("Veg", "Non-Veg", "Vegan", "Jain"))
                    .build(),
                FilterField.builder()
                    .key("delivery")
                    .label("Delivery Available")
                    .type("checkbox")
                    .options(Arrays.asList("Yes", "No"))
                    .build()
            ))
            .build());
        
        // Electricians
        schemas.put("electrician", CategoryFilterSchema.builder()
            .categoryId("electrician")
            .priceRange(new PriceRange(new BigDecimal("200"), new BigDecimal("2000")))
            .filters(Arrays.asList(
                FilterField.builder()
                    .key("serviceType")
                    .label("Service Type")
                    .type("checkbox")
                    .options(Arrays.asList("Repair", "Installation", "Maintenance", "Wiring"))
                    .build(),
                FilterField.builder()
                    .key("urgency")
                    .label("Urgency")
                    .type("dropdown")
                    .options(Arrays.asList("Emergency", "Same Day", "Scheduled"))
                    .build(),
                FilterField.builder()
                    .key("experience")
                    .label("Minimum Experience")
                    .type("dropdown")
                    .options(Arrays.asList("1+ years", "3+ years", "5+ years", "10+ years"))
                    .build()
            ))
            .build());
        
        // Plumbers
        schemas.put("plumber", CategoryFilterSchema.builder()
            .categoryId("plumber")
            .priceRange(new PriceRange(new BigDecimal("200"), new BigDecimal("2500")))
            .filters(Arrays.asList(
                FilterField.builder()
                    .key("serviceType")
                    .label("Service Type")
                    .type("checkbox")
                    .options(Arrays.asList("Leak Repair", "Pipe Installation", "Drain Cleaning", "Fixture Installation"))
                    .build(),
                FilterField.builder()
                    .key("urgency")
                    .label("Urgency")
                    .type("dropdown")
                    .options(Arrays.asList("Emergency", "Same Day", "Scheduled"))
                    .build(),
                FilterField.builder()
                    .key("experience")
                    .label("Minimum Experience")
                    .type("dropdown")
                    .options(Arrays.asList("1+ years", "3+ years", "5+ years", "10+ years"))
                    .build()
            ))
            .build());
        
        // Cleaners
        schemas.put("cleaner", CategoryFilterSchema.builder()
            .categoryId("cleaner")
            .priceRange(new PriceRange(new BigDecimal("500"), new BigDecimal("5000")))
            .filters(Arrays.asList(
                FilterField.builder()
                    .key("cleaningType")
                    .label("Cleaning Type")
                    .type("checkbox")
                    .options(Arrays.asList("Home Cleaning", "Office Cleaning", "Deep Cleaning", "Carpet Cleaning"))
                    .build(),
                FilterField.builder()
                    .key("frequency")
                    .label("Frequency")
                    .type("dropdown")
                    .options(Arrays.asList("One-time", "Weekly", "Bi-weekly", "Monthly"))
                    .build(),
                FilterField.builder()
                    .key("ecoFriendly")
                    .label("Eco-Friendly Products")
                    .type("checkbox")
                    .options(Arrays.asList("Yes", "No"))
                    .build()
            ))
            .build());
        
        // Laundry
        schemas.put("laundry", CategoryFilterSchema.builder()
            .categoryId("laundry")
            .priceRange(new PriceRange(new BigDecimal("10"), new BigDecimal("100")))
            .filters(Arrays.asList(
                FilterField.builder()
                    .key("washType")
                    .label("Wash Type")
                    .type("dropdown")
                    .options(Arrays.asList("Per Kg", "Per Piece", "Monthly Package"))
                    .build(),
                FilterField.builder()
                    .key("serviceType")
                    .label("Service Type")
                    .type("checkbox")
                    .options(Arrays.asList("Wash & Fold", "Dry Cleaning", "Ironing", "Steam Press"))
                    .build(),
                FilterField.builder()
                    .key("pickupService")
                    .label("Pickup & Delivery")
                    .type("checkbox")
                    .options(Arrays.asList("Available", "Not Available"))
                    .build(),
                FilterField.builder()
                    .key("turnaround")
                    .label("Turnaround Time")
                    .type("dropdown")
                    .options(Arrays.asList("Same Day", "24 Hours", "48 Hours", "Standard"))
                    .build()
            ))
            .build());
        
        return schemas;
    }
    
    public Map<String, CategoryFilterSchema> getAllSchemas() {
        return filterSchemas;
    }
    
    public CategoryFilterSchema getSchemaByCategoryId(String categoryId) {
        return filterSchemas.get(categoryId);
    }
}

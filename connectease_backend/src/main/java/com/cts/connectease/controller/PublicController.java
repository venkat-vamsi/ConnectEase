package com.cts.connectease.controller;

import com.cts.connectease.model.Category;
import com.cts.connectease.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private VendorService vendorService;

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return vendorService.getAllCategories();
    }
}
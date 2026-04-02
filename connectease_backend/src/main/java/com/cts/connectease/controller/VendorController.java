package com.cts.connectease.controller;

import com.cts.connectease.dto.ServiceDetailsDTO;
import com.cts.connectease.dto.VendorDashboardDTO;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @GetMapping("/dashboard")
    public VendorDashboardDTO getDashboard(Authentication authentication) {
        String currentUserId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;
        return vendorService.getVendorDashboardStats(currentUserId);
    }

    @PostMapping("/service/add")
    public ServiceDetailsDTO addService(Authentication authentication, @RequestBody ServiceEntity service) {
        String currentUserId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;

        return vendorService.createNewService(currentUserId, service);
    }
}
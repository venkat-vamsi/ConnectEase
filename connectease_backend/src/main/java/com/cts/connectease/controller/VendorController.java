package com.cts.connectease.controller;

import com.cts.connectease.dto.ListingCardDTO;
import com.cts.connectease.dto.ServiceDetailsDTO;
import com.cts.connectease.dto.VendorDashboardDTO;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/services")
    public ResponseEntity<List<ListingCardDTO>> getMyServices(Authentication authentication) {
        String currentUserId = authentication.getCredentials().toString();
        return ResponseEntity.ok(vendorService.getVendorServices(currentUserId));
    }

    @PutMapping("/service/{sid}")
    public ResponseEntity<ServiceDetailsDTO> updateService(
            @PathVariable String sid,
            @RequestBody ServiceEntity service,
            Authentication authentication) {
        String currentUserId = authentication.getCredentials().toString();
        return ResponseEntity.ok(vendorService.updateService(currentUserId, sid, service));
    }

    @DeleteMapping("/service/{sid}")
    public ResponseEntity<Map<String, String>> deleteService(
            @PathVariable String sid,
            Authentication authentication) {
        String currentUserId = authentication.getCredentials().toString();
        vendorService.deleteService(currentUserId, sid);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Service deleted successfully"));
    }

    @PatchMapping("/service/{sid}/status")
    public ResponseEntity<ServiceDetailsDTO> toggleServiceStatus(
            @PathVariable String sid,
            Authentication authentication) {
        String currentUserId = authentication.getCredentials().toString();
        return ResponseEntity.ok(vendorService.toggleServiceStatus(currentUserId, sid));
    }
}

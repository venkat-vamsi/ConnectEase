package com.cts.connectease.controller;

import com.cts.connectease.dto.ServiceDetailsDTO;
import com.cts.connectease.dto.VendorDashboardDTO;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @GetMapping("/dashboard/{uid}")
    public VendorDashboardDTO getDashboard(@PathVariable String uid) {
        return vendorService.getVendorDashboardStats(uid);
    }

    @PostMapping("/{uid}/service/add")
    public ServiceDetailsDTO addService(@PathVariable String uid, @RequestBody ServiceEntity service) {
        // Correctly returning the DTO now
        return vendorService.createNewService(uid, service);
    }
}
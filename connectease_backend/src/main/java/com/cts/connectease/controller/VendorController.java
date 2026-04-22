package com.cts.connectease.controller;

import com.cts.connectease.dto.ServiceDetailsDTO;
import com.cts.connectease.dto.VendorDashboardDTO;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

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
        System.out.println("Dashboard request for userId: " + currentUserId);
        return vendorService.getVendorDashboardStats(currentUserId);
    }

    @PostMapping("/service/add")
    public ServiceDetailsDTO addService(Authentication authentication,
                                       @RequestParam("name") String name,
                                       @RequestParam("description") String description,
                                       @RequestParam("price") String price,
                                       @RequestParam("categoryId") String categoryId,
                                       @RequestParam("city") String city,
                                       @RequestParam("area") String area,
                                       @RequestParam(value = "imageUrls", required = false) String[] imageUrls,
                                       @RequestParam(value = "features", required = false) String[] features) throws IOException {
        String currentUserId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;

        ServiceEntity service = new ServiceEntity();
        service.setName(name);
        service.setDescription(description);
        service.setPrice(java.math.BigDecimal.valueOf(Double.parseDouble(price)));

        // Set category
        com.cts.connectease.model.Category category = new com.cts.connectease.model.Category();
        category.setCid(categoryId);
        service.setCategory(category);

        // Set location
        com.cts.connectease.model.Location location = new com.cts.connectease.model.Location();
        location.setCity(city);
        location.setArea(area);
        service.setLocation(location);

        // Handle images
        List<com.cts.connectease.model.ServiceImages> serviceImages = new ArrayList<>();
        if (imageUrls != null && imageUrls.length > 0) {
            for (String url : imageUrls) {
                if (url != null && !url.trim().isEmpty()) {
                    com.cts.connectease.model.ServiceImages img = new com.cts.connectease.model.ServiceImages();
                    img.setUrl(url.trim());
                    img.setIsPrimary(serviceImages.isEmpty()); // First image is primary
                    serviceImages.add(img);
                }
            }
        }

        // If no images uploaded, get one from category
        if (serviceImages.isEmpty()) {
            String defaultImage = vendorService.getDefaultImageForCategory(categoryId);
            if (defaultImage != null) {
                com.cts.connectease.model.ServiceImages img = new com.cts.connectease.model.ServiceImages();
                img.setUrl(defaultImage);
                img.setIsPrimary(true);
                serviceImages.add(img);
            }
        }

        service.setImages(serviceImages);

        // Handle features
        if (features != null && features.length > 0) {
            List<com.cts.connectease.model.Feature> featureList = new ArrayList<>();
            for (String featureName : features) {
                if (featureName != null && !featureName.trim().isEmpty()) {
                    com.cts.connectease.model.Feature feature = new com.cts.connectease.model.Feature();
                    feature.setName(featureName.trim());
                    featureList.add(feature);
                }
            }
            service.setFeatures(featureList);
        }

        return vendorService.createNewService(currentUserId, service);
    }
}
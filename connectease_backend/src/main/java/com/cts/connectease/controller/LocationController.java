package com.cts.connectease.controller;

import com.cts.connectease.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        List<String> cities = locationRepository.findAllDistinctCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/areas")
    public ResponseEntity<List<String>> getAreas(@RequestParam String city) {
        List<String> areas = locationRepository.findAreasByCity(city);
        return ResponseEntity.ok(areas);
    }
}

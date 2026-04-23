package com.cts.connectease.controller;

import com.cts.connectease.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(locationRepository.findDistinctCities());
    }

    @GetMapping("/cities/{city}/areas")
    public ResponseEntity<List<String>> getAreasByCity(@PathVariable String city) {
        return ResponseEntity.ok(locationRepository.findDistinctAreasByCity(city));
    }
}

package com.cts.connectease.controller;

import com.cts.connectease.dto.ChangePasswordRequest;
import com.cts.connectease.dto.UpdateProfileRequest;
import com.cts.connectease.dto.UserProfileDTO;
import com.cts.connectease.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{uid}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String uid) {
        return ResponseEntity.ok(userService.getUserProfile(uid));
    }

    @PutMapping("/{uid}")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @PathVariable String uid,
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String currentUserId = authentication.getCredentials().toString();
        return ResponseEntity.ok(userService.updateProfile(uid, request, currentUserId));
    }

    @PutMapping("/{uid}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable String uid,
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String currentUserId = authentication.getCredentials().toString();
        userService.changePassword(uid, request, currentUserId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Password updated successfully"));
    }

    @DeleteMapping("/{uid}")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @PathVariable String uid,
            Authentication authentication) {
        String currentUserId = authentication.getCredentials().toString();
        userService.deleteAccount(uid, currentUserId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Account deleted successfully"));
    }
}

package com.cts.connectease.service;

import com.cts.connectease.dto.ChangePasswordRequest;
import com.cts.connectease.dto.UpdateProfileRequest;
import com.cts.connectease.dto.UserProfileDTO;
import com.cts.connectease.model.User;
import com.cts.connectease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String uid) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(user);
    }

    @Transactional
    public UserProfileDTO updateProfile(String uid, UpdateProfileRequest request, String currentUserId) {
        if (!uid.equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: cannot update another user's profile");
        }
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNo() != null) user.setPhoneNo(request.getPhoneNo());
        if (request.getImage() != null) user.setImage(request.getImage());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String uid, ChangePasswordRequest request, String currentUserId) {
        if (!uid.equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: cannot change another user's password");
        }
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String uid, String currentUserId) {
        if (!uid.equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: cannot delete another user's account");
        }
        userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.deleteById(uid);
    }

    private UserProfileDTO toDto(User user) {
        return UserProfileDTO.builder()
                .uid(user.getUid())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNo(user.getPhoneNo())
                .image(user.getImage())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}

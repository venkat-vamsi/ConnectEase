package com.cts.connectease.controller;

import com.cts.connectease.model.User;
import com.cts.connectease.service.AuthService;
import com.cts.connectease.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/signup")
	public ResponseEntity<?> register(@RequestBody User user) {
		String result = authService.registerUser(user);
		if (result.startsWith("Error")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", "error", "message", result));
		}
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("status", "success", "message", "User registered successfully"));
	}

	@PostMapping("/signin")
	public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletResponse response) {
		String email = body.get("email");
		String password = body.get("password");

		Optional<User> userOpt = authService.authenticate(email, password);

		if (userOpt.isPresent()) {
			User user = userOpt.get();
			String token = jwtUtil.generateToken(user);

			// Store JWT in a secure HttpOnly cookie
			Cookie cookie = new Cookie("jwt", token);
			cookie.setHttpOnly(true);
			cookie.setSecure(false); // Set to true ONLY if using HTTPS in production
			cookie.setPath("/");
			cookie.setMaxAge(86400); // 1 day
			response.addCookie(cookie);

			return ResponseEntity.ok(Map.of(
					"status", "success",
					"message", "login successful",
					"role", user.getRole(),
					"uid", user.getUid(),
					"fullName", user.getFullName() != null ? user.getFullName() : ""
			));
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("status", "error", "message", "Invalid credentials"));
	}
    
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletResponse response) {
		Cookie cookie = new Cookie("jwt", null);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(0); // Instantly expires the cookie
		response.addCookie(cookie);
		return ResponseEntity.ok(Map.of("status", "success", "message", "Logged out successfully"));
	}
}


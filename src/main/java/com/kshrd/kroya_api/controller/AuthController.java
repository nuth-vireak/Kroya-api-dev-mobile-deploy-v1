package com.kshrd.kroya_api.controller;

import com.kshrd.kroya_api.payload.Auth.LoginRequest;
import com.kshrd.kroya_api.payload.Auth.PasswordRequest;
import com.kshrd.kroya_api.payload.Auth.UserInfoRequest;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.service.Auth.AuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    // Refresh token
    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    // Step 1: Check if Email Exist (for the first screen in your UI)
    @GetMapping("/check-email-exist")
    public BaseResponse checkEmailExist(@RequestParam String email) {
        return authenticationService.checkEmailExist(email);
    }

    // Step 2: Authenticate with Email and Password (for the second screen in your UI)
    @PostMapping("/login")
    public BaseResponse loginByEmailAndPassword(@RequestBody LoginRequest loginRequest) {
        return authenticationService.loginByEmailAndPassword(loginRequest);
    }

    // Step 1: Send OTP for Email Verification
    @PostMapping("/send-otp")
    public BaseResponse sendOtp(@RequestParam String email) throws MessagingException {
        return authenticationService.generateOtp(email);
    }

    // Step 2: Validate OTP for Email Verification
    @PostMapping("/validate-otp")
    public BaseResponse validateOtp(@RequestParam String email, @RequestParam String otp) {
        return authenticationService.validateOtp(email, otp);
    }

    // Step 1: Create Password
    @PostMapping("/register")
    public BaseResponse createPassword(@RequestBody PasswordRequest passwordRequest) {
        return authenticationService.createPassword(passwordRequest);
    }

    // Step 2: Save Additional Information
    @PostMapping("/save-user-info")
    public BaseResponse saveUserInfo(@RequestBody UserInfoRequest userInfoRequest) {
        return authenticationService.saveUserInfo(userInfoRequest);
    }

    // Forget Password: (Reset the Password after OTP verification)
    @PostMapping("/reset-password")
    public BaseResponse resetPassword(@RequestBody PasswordRequest passwordRequest) {
        return authenticationService.resetPassword(passwordRequest);
    }
}


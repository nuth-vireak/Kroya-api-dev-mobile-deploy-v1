package com.kshrd.kroya_api.service.Auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.kroya_api.config.JwtService;
import com.kshrd.kroya_api.entity.CodeEntity;
import com.kshrd.kroya_api.entity.UserEntity;
import com.kshrd.kroya_api.entity.token.Token;
import com.kshrd.kroya_api.entity.token.TokenRepository;
import com.kshrd.kroya_api.enums.ResponseMessage;
import com.kshrd.kroya_api.enums.TokenType;
import com.kshrd.kroya_api.exception.CustomExceptionSecurity;
import com.kshrd.kroya_api.exception.NotFoundExceptionHandler;
import com.kshrd.kroya_api.exception.exceptionValidateInput.Validation;
import com.kshrd.kroya_api.payload.Auth.*;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.repository.Code.CodeRepository;
import com.kshrd.kroya_api.repository.User.UserRepository;
import com.kshrd.kroya_api.service.Code.EmailService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final Validation validation;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final CodeRepository codeRepository;
    private final EmailService emailService;

    // Step 1: Validate Email (for the first screen)
    public BaseResponse<?> checkEmailExist(String email) {

        log.debug("Checking email for validation: {}", email);
        validation.ValidationEmail(email);

        var userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            log.warn("User not found for email: {}", email);
            throw new NotFoundExceptionHandler("User not found");
        }

        log.info("Email found for: {}", email);
        return BaseResponse.builder()
                .message("Email found, proceed to password input")
                .statusCode("200")
                .build();
    }

    // Step 2: Authenticate Email and Password (for the second screen)
    public BaseResponse<?> loginByEmailAndPassword(LoginRequest loginRequest) {

        log.debug("Validating login request for email: {}", loginRequest.getEmail());
        validation.ValidationEmail(loginRequest.getEmail());

        var userEntity = userRepository.findByEmail(loginRequest.getEmail());

        if (userEntity == null) {
            log.warn("User not found for email: {}", loginRequest.getEmail());
            throw new NotFoundExceptionHandler("User not found");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
            log.warn("Incorrect password for email: {}", loginRequest.getEmail());
            throw new CustomExceptionSecurity(ResponseMessage.INCORRECT_PASSWORD);
        }

        log.info("User authenticated successfully for email: {}", loginRequest.getEmail());
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail(loginRequest.getEmail());
        authenticationRequest.setPassword(loginRequest.getPassword());

        return authenticate(authenticationRequest);
    }

    // Helper method to authenticate and generate tokens
    private BaseResponse<?> authenticate(AuthenticationRequest request) {

        log.debug("Authenticating user: {}", request.getEmail());
        var user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            log.warn("User not found during authentication for email: {}", request.getEmail());
            throw new CustomExceptionSecurity(ResponseMessage.INCORRECT_USERNAME);
        }

        // Authenticate using Spring Security's authentication manager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        log.info("Authentication successful for user: {}", request.getEmail());

        // Generate JWT tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        log.info("Generated new JWT tokens for user: {}", request.getEmail());

        return BaseResponse.builder()
                .payload(AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .build())
                .build();
    }

    // Helper method to save the user's token to the database for later use
    private void saveUserToken(UserEntity user, String jwtToken) {
        log.debug("Saving token for user: {}", user.getEmail());
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .tokenExpired(false)
                .tokenRevoked(false)
                .build();
        tokenRepository.save(token);
    }

    // Helper method to revoke all tokens for a user (used when a user logs in)
    private void revokeAllUserTokens(UserEntity user) {
        log.debug("Revoking all valid tokens for user: {}", user.getEmail());
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            log.debug("No valid tokens found for user: {}", user.getEmail());
            return;
        }
        validUserTokens.forEach(token -> {
            token.setTokenExpired(true);
            token.setTokenRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
        log.info("All tokens revoked for user: {}", user.getEmail());
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        // Check if the Authorization header is missing or does not start with Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        refreshToken = authHeader.substring(7);
        log.debug("Refresh token: {}", refreshToken); // Debug level to show token

        // Extract the user email from the refresh token
        userEmail = jwtService.extractUsername(refreshToken);
        log.debug("Extracted user email: {}", userEmail);

        if (userEmail != null) {
            // Find the user by email
            var user = this.userRepository.findByEmail(userEmail);

            if (user != null && jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                log.info("Successfully generated new access token for user: {}", userEmail);

                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);

                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

                // Return the new tokens in the response
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            } else {
                log.warn("Invalid token or user not found for email: {}", userEmail);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token or user not found");
            }
        } else {
            log.error("Failed to extract user information from token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token: No user information");
        }
    }

    // Generate OTP and save to CodeEntity
    public BaseResponse<?> generateOtp(String email) throws MessagingException {

        var user = userRepository.findByEmail(email);

        // Check if OTP already exists for this email
        CodeEntity codeEntity = codeRepository.findByEmail(email);

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Set expiry to 5 minutes from now
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusMinutes(5);

        // If OTP already exists, update it; otherwise, create a new CodeEntity
        if (codeEntity != null) {
            log.info("Updating existing OTP for email: {}", email);
            codeEntity.setPinCode(otp);
            codeEntity.setExpireDate(expiryDate);
            codeEntity.setCreateDate(now);
        } else {
            log.info("Creating new OTP for email: {}", email);
            codeEntity = new CodeEntity();
            codeEntity.setEmail(email);
            codeEntity.setPinCode(otp);
            codeEntity.setCreateDate(now);
            codeEntity.setExpireDate(expiryDate);
            codeEntity.setUser(user);
        }

        // Save the OTP code to the database
        codeRepository.save(codeEntity);

        log.info("Generated OTP for email: {}, OTP: {}", email, otp);

        // You can send the OTP to the user via email, SMS, etc. here.
        emailService.sendEmail(email, otp);

        return BaseResponse.builder()
                .payload(email)
                .message(otp)
                .statusCode("200")
                .build();
    }

    public BaseResponse<?> validateOtp(String email, String otp) {
        // Find the OTP code by email
        var codeEntity = codeRepository.findByEmail(email);

        if (codeEntity == null) {
            log.warn("OTP not found for email: {}", email);
            return BaseResponse.builder().message("OTP not found").statusCode("404").build();
        }

        // Check if the OTP has expired
        if (LocalDateTime.now().isAfter(codeEntity.getExpireDate())) {
            log.warn("OTP expired for email: {}", email);
            return BaseResponse.builder().message("OTP expired").statusCode("400").build();
        }

        // Check if the OTP matches
        if (!codeEntity.getPinCode().equals(otp)) {
            log.warn("Invalid OTP for email: {}", email);
            return BaseResponse.builder().message("Invalid OTP").statusCode("400").build();
        }

        // OTP is valid, proceed to create the user
        UserEntity userEntity = userRepository.findByEmail(email);

        if (userEntity == null) {
            // If user does not exist, create a new user
            log.info("Creating a new user for email: {}", email);
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode("default_password"));
            newUser.setFullName("");
            newUser.setPhoneNumber("");
            newUser.setLocation("");
            newUser.setEmailVerified(true);
            newUser.setEmailVerifiedAt(LocalDateTime.now());
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setRole("ROLE_USER");

            userRepository.save(newUser);

            log.info("New user created and email verified for email: {}", email);
        } else {
            // If user already exists, just update the email verified status
            userEntity.setEmailVerified(true);
            userEntity.setEmailVerifiedAt(LocalDateTime.now());
            userRepository.save(userEntity);

            log.info("Existing user email verified for email: {}", email);
        }

        log.info("OTP validated and email verified for email: {}", email);
        return BaseResponse.builder().message("OTP validated and email verified").statusCode("200").build();
    }

    public BaseResponse<?> createPassword(PasswordRequest passwordRequest) {
        log.debug("Create password for email: {}", passwordRequest.getEmail());

        // Find user by email
        var user = userRepository.findByEmail(passwordRequest.getEmail());
        if (user == null) {
            throw new NotFoundExceptionHandler("User not found");
        }

        // Update the user's password
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);

        log.info("Password created successfully for email: {}", passwordRequest.getEmail());

        // Generate JWT tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);  // Optional: revoke any existing tokens for the user
        saveUserToken(user, jwtToken);

        log.info("Generated JWT token for email: {}", passwordRequest.getEmail());

        return BaseResponse.builder()
                .message("Password created successfully")
                .statusCode("200")
                .payload(AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .build())
                .build();
    }

    public BaseResponse<?> saveUserInfo(UserInfoRequest userInfoRequest) {
        log.debug("Saving additional info for email: {}", userInfoRequest.getEmail());

        // Find user by email
        var user = userRepository.findByEmail(userInfoRequest.getEmail());
        if (user == null) {
            throw new NotFoundExceptionHandler("User not found");
        }

        // Update the user's additional information
        user.setFullName(userInfoRequest.getUserName());
        user.setPhoneNumber(userInfoRequest.getPhoneNumber());
        user.setLocation(userInfoRequest.getAddress());

        userRepository.save(user);

        return BaseResponse.builder()
                .message("User information saved successfully")
                .statusCode("200")
                .build();
    }

    public BaseResponse<?> resetPassword(PasswordRequest passwordRequest) {

        log.info("Processing reset password for email: {}", passwordRequest.getEmail());

        // Find the user by email
        var user = userRepository.findByEmail(passwordRequest.getEmail());
        if (user == null) {
            throw new NotFoundExceptionHandler("User not found");
        }

        // Update the password for the user
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset successfully for email: {}", passwordRequest.getEmail());

        return BaseResponse.builder()
                .message("Password reset successfully")
                .statusCode("200")
                .build();
    }

//    public BaseResponse<?> loginAsGuest() {
//
//        log.info("Processing login as guest");
//
//        // Generate random email, full name, and password for the guest user
//        String randomEmail = generateRandomEmail();
//        String randomFullName = "Guest_" + UUID.randomUUID().toString().substring(0, 8);
//        String randomPassword = generateRandomPassword(8);  // Generate an 8-character password
//
//        // Create a new guest user
//        log.info("Creating new guest user with email: {}", randomEmail);
//
//        UserEntity guestUser = new UserEntity();
//        guestUser.setEmail(randomEmail);
//        guestUser.setFullName(randomFullName);
//        guestUser.setPassword(passwordEncoder.encode(randomPassword));  // Encode the generated password
//        guestUser.setRole("ROLE_GUEST");  // Assign the guest role
//        guestUser.setCreatedAt(LocalDateTime.now());
//
//        // Save the new guest user to the database
//        userRepository.save(guestUser);
//
//        log.info("Guest user created successfully with email: {}", randomEmail);
//
//        // Step 3: Authenticate the guest user using the generated credentials
//        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
//        authenticationRequest.setEmail(randomEmail);
//        authenticationRequest.setPassword(randomPassword);  // Use the generated password
//
//        // Step 4: Call the authenticate method to generate the tokens
//        return authenticate(authenticationRequest);
//    }
//
//    // Helper method to generate a random email
//    private String generateRandomEmail() {
//        String uuid = UUID.randomUUID().toString().substring(0, 8);  // Generate random UUID and shorten it
//        return "guest_" + uuid + "@kroya.com";
//    }
//
//    // Helper method to generate a random password with specified length
//    private String generateRandomPassword(int length) {
//        SecureRandom random = new SecureRandom();
//        StringBuilder password = new StringBuilder(length);
//        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
//
//        for (int i = 0; i < length; i++) {
//            password.append(characters.charAt(random.nextInt(characters.length())));
//        }
//        return password.toString();
//    }
}

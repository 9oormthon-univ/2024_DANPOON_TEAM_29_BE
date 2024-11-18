package com.globalnest.be.user.controller;

import com.globalnest.be.global.application.AWSStorageService;
import com.globalnest.be.global.dto.ResponseTemplate;
import com.globalnest.be.oauth.dto.CustomOAuth2User;
import com.globalnest.be.user.dto.request.FirstLoginRequest;
import com.globalnest.be.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "User API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AWSStorageService awsStorageService;

    @PostMapping("/first-login")
    @Operation(summary = "첫 로그인 정보 기입")
    public ResponseEntity<?> registerFirstLoginUser(@ModelAttribute FirstLoginRequest request,
                                                    @RequestParam MultipartFile file,
                                                    @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        String file_url = awsStorageService.uploadFile(file,"user");
        userService.registerUser(request, file_url , customOAuth2User.getUserId());
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ResponseTemplate.EMPTY_RESPONSE);
    }
}
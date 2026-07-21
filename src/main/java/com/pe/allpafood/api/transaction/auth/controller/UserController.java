package com.pe.allpafood.api.transaction.auth.controller;

import com.pe.allpafood.api.transaction.user.bussiness.impl.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {


    private final ProfileService profileService;

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestAttribute String userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }
}

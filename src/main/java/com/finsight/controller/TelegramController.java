package com.finsight.controller;

import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import com.finsight.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/telegram")
public class TelegramController {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final Random random = new Random();

    public TelegramController(UserRepository userRepository, SecurityUtil securityUtil) {
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/generate-link-code")
    public ResponseEntity<Map<String, String>> generateLinkCode() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow();

        String code = String.format("%06d", random.nextInt(1000000));
        user.setTelegramLinkingCode(code);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("code", code, "botUsername", "FinSightBot")); // Or fetch from props
    }
}

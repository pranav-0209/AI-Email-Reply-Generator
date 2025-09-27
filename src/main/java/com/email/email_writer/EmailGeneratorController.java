package com.email.email_writer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailGeneratorController {

    private final EmailGeneratorService emailGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<String> getEmail(@RequestBody EmailRequestDto emailRequestDto) {
        String response = emailGeneratorService.generateEmailReply(emailRequestDto);
        return ResponseEntity.ok(response);
    }
}

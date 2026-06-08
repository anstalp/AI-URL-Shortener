package com.anastasis.minilink.controller;

import com.anastasis.minilink.UrlRequestDto;
import com.anastasis.minilink.entity.UrlMapping;
import com.anastasis.minilink.service.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/urls")
    public ResponseEntity<?> createShortUrl(@Valid @RequestBody UrlRequestDto request){
        String originalUrl = request.getOriginalUrl();
        String customAlias = request.getCustomAlias();

        try {
            UrlMapping savedUrl = urlService.shortenUrl(originalUrl, customAlias);
            return ResponseEntity.ok(savedUrl);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{shortCode}")
    public void redirectToOriginal( @PathVariable String shortCode, HttpServletResponse response) throws IOException {

        UrlMapping mapping = urlService.getLink(shortCode);
        String originalUrl = mapping.getOriginalUrl();

        response.sendRedirect(originalUrl);
    }

    @GetMapping("/api/urls")
    public List<UrlMapping> getAllUrls() {
        return urlService.getAllLinks();
    }


}

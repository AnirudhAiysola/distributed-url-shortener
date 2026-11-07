package com.kyo.url_shortener.url;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/urls")
public class ShortUrlController {

    private final ShortUrlService service;
    private final String baseUrl;

    public ShortUrlController(ShortUrlService service,
                              @Value("${shortener.base-url}") String baseUrl) {
        this.service = service;
        this.baseUrl = baseUrl;
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request) {
        ShortUrl url = service.create(request.toCommand());
        ShortUrlResponse  response =  ShortUrlResponse.from(url, baseUrl);

        return ResponseEntity.created(URI.create("/api/v1/urls/" + url.getCode())).body(response);
    }
}
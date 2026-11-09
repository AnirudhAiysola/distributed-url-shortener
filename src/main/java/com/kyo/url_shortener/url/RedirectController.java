package com.kyo.url_shortener.url;

import com.kyo.url_shortener.analytics.ClickEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final ShortUrlService service;
    private final ClickEventPublisher clickEventPublisher;

    @GetMapping("/{code:[A-Za-z0-9_-]{3,32}}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String longUrl = service.resolve(code);

        clickEventPublisher.publish(code);
        return ResponseEntity.status(HttpStatus.FOUND).location((URI.create(longUrl))).build();
    }
}
package com.kyo.url_shortener.url;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortUrlController.class)
class ShortUrlControllerTest {

    private static final Instant CREATED = Instant.parse("2026-11-07T10:00:00Z");
    private static final String LONG_URL = "https://example.com/some/long/path";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ShortUrlService service;

    // Example: the happy path, fully written
    @Test
    void validRequestReturns201WithLocationAndBody() throws Exception {
        when(service.create(any(CreateShortUrlCommand.class)))
                .thenReturn(new ShortUrl("abc1234", LONG_URL, null, CREATED));

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"longUrl": "https://example.com/some/long/path"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/urls/abc1234"))
                .andExpect(jsonPath("$.code").value("abc1234"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc1234"));
    }

    @Test
    void invalidUrlReturns400() throws Exception {
       mockMvc.perform(post("/api/v1/urls").contentType(MediaType.APPLICATION_JSON)
               .content("""
                      {"longUrl": "not-a-url"}
                       """)).andExpect(status().isBadRequest());

       verify(service, never()).create(any());
    }

    @Test
    void missingLongUrlReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/urls").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {}
                        """)).andExpect(status().isBadRequest());
        verify(service, never()).create(any());
    }

    @Test
    void aliasWithInvalidCharactersReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/urls").contentType(MediaType.APPLICATION_JSON).content("""
                {"longUrl":  "https://example.com/some/long/path", "alias" : "bad alias!"}
                """)).andExpect(status().isBadRequest());
        verify(service, never()).create(any());
    }
}
package com.kyo.url_shortener.url;

import com.kyo.url_shortener.analytics.ClickEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RedirectController.class)
class RedirectControllerTest {

    private static final Instant CREATED = Instant.parse("2026-11-07T10:00:00Z");
    private static final String LONG_URL = "https://example.com/some/long/path";


    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ShortUrlService service;

    @MockitoBean
    ClickEventPublisher clickEventPublisher;

    @Test
    void existingCodeRedirectsWith302() throws Exception {
        when(service.resolve("abc1234")).thenReturn(LONG_URL);

        mockMvc.perform(get("/abc1234"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", LONG_URL));
    }

    @Test
    void missingCodeReturns404() throws Exception {
        when(service.resolve("nope123"))
                .thenThrow(new ShortUrlNotFoundException("nope123"));
        mockMvc.perform(get("/nope123")).andExpect(status().isNotFound());
    }

    @Test
    void expiredCodeReturns410() throws Exception {
        when(service.resolve("old1234"))
                .thenThrow(new ShortUrlExpiredException("old1234"));

        mockMvc.perform(get("/old1234")).andExpect(status().isGone());
    }
}
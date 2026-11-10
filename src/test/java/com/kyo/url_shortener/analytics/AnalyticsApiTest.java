package com.kyo.url_shortener.analytics;

import com.jayway.jsonpath.JsonPath;
import com.kyo.url_shortener.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AnalyticsApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void paginatesDailyResultsWithCursor() throws Exception {
        seedLink("anl0001");
        seedClicks("anl0001", "2026-11-01", 5);
        seedClicks("anl0001", "2026-11-02", 3);
        seedClicks("anl0001", "2026-11-03", 7);

        String firstPage = mockMvc.perform(get("/api/v1/urls/anl0001/analytics")
                        .param("from", "2026-11-01").param("to", "2026-11-03").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicks").value(15))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].period").value("2026-11-01"))
                .andExpect(jsonPath("$.nextCursor").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String cursor = JsonPath.read(firstPage, "$.nextCursor");

        mockMvc.perform(get("/api/v1/urls/anl0001/analytics")
                        .param("from", "2026-11-01").param("to", "2026-11-03").param("limit", "2")
                        .param("cursor", cursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].period").value("2026-11-03"))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
    }

    @Test
    void groupsByMonth() throws Exception {
        seedLink("anl0002");
        seedClicks("anl0002", "2026-10-31", 4);
        seedClicks("anl0002", "2026-11-01", 6);
        seedClicks("anl0002", "2026-11-15", 1);

        mockMvc.perform(get("/api/v1/urls/anl0002/analytics")
                        .param("from", "2026-10-01").param("to", "2026-11-30").param("granularity", "month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].period").value("2026-10-01"))
                .andExpect(jsonPath("$.data[0].clicks").value(4))
                .andExpect(jsonPath("$.data[1].period").value("2026-11-01"))
                .andExpect(jsonPath("$.data[1].clicks").value(7));
    }

    @Test
    void unknownCodeReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/urls/nosuch1/analytics"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidGranularityReturns400() throws Exception {
        seedLink("anl0003");
        mockMvc.perform(get("/api/v1/urls/anl0003/analytics").param("granularity", "year"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tamperedCursorReturns400() throws Exception {
        seedLink("anl0004");
        mockMvc.perform(get("/api/v1/urls/anl0004/analytics").param("cursor", "not-a-cursor!"))
                .andExpect(status().isBadRequest());
    }

    private void seedLink(String code) {
        jdbc.update("INSERT INTO urls (code, long_url) VALUES (?, ?)", code, "https://example.com");
    }

    private void seedClicks(String code, String day, long clicks) {
        jdbc.update("INSERT INTO daily_clicks (code, day, clicks) VALUES (?, ?, ?)",
                code, LocalDate.parse(day), clicks);
    }
}
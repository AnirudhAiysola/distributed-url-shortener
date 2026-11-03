package com.kyo.url_shortener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class IdSequenceTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void idSequenceCheck(){
        Long first = jdbcTemplate.queryForObject("SELECT nextval('url_id_seq')", Long.class);
        Long second = jdbcTemplate.queryForObject("SELECT nextval('url_id_seq')", Long.class);

        assertThat(second).isEqualTo(first + 1000);
    }
}

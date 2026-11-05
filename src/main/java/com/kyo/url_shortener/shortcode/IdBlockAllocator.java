package com.kyo.url_shortener.shortcode;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class IdBlockAllocator {

    static final int BLOCK_SIZE = 1000; // must match INCREMENT BY in V1 migration

    private final JdbcTemplate jdbcTemplate;

    private long next;      // next ID to hand out
    private long blockEnd;  // first ID NOT in the current block

    public IdBlockAllocator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized long nextId() {
        if(next >= blockEnd){
            long newStart = jdbcTemplate.queryForObject("SELECT nextval('url_id_seq')", Long.class);
            next = newStart;
            blockEnd = newStart + BLOCK_SIZE;
        }
        long id = next;
        next++;
        return id;
    }
}
package com.kyo.url_shortener.shortcode;

import com.kyo.url_shortener.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class IdBlockAllocatorTest {

    @Autowired
    IdBlockAllocator allocator;

    @Test
    void handsOutSequentialIdsWithinABlock() {
        // TODO: call nextId() twice
        // TODO: assert the second is exactly first + 1
        long first = allocator.nextId();
        long second = allocator.nextId();

        assertThat(second).isEqualTo(first+1);
    }

    @Test
    void fetchesANewBlockWhenTheCurrentOneRunsOut() {
        // TODO: call nextId() BLOCK_SIZE + 1 times, store all results in a Set<Long>
        // TODO: assert the set size is BLOCK_SIZE + 1 (no duplicates across the block boundary)
        Set<Long> seen = new HashSet<>();
        for(int i=1;i<=IdBlockAllocator.BLOCK_SIZE+1;i++){
            seen.add(allocator.nextId());
        }
        assertThat(seen.size()).isEqualTo(IdBlockAllocator.BLOCK_SIZE+1);
    }

    @Test
    void concurrentCallsNeverReturnDuplicates() throws Exception {
        Set<Long> ids = ConcurrentHashMap.newKeySet(); // thread-safe Set
        int calls = 10_000;

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < calls; i++) {
                // TODO: submit a task that calls allocator.nextId() and adds it to ids
                executor.submit(() -> ids.add(allocator.nextId()));
            }
        } // closing the executor waits for all tasks to finish

        // TODO: assert ids.size() is calls
        assertThat(ids.size()).isEqualTo(calls);
    }
}
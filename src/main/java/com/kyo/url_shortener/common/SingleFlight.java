package com.kyo.url_shortener.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class SingleFlight<T> {

    private final ConcurrentHashMap<String, CompletableFuture<T>> inFlight = new ConcurrentHashMap<>();

    public T execute(String key, Supplier<T> loader) {
        CompletableFuture<T> mine = new CompletableFuture<>();

        CompletableFuture<T> existing = inFlight.putIfAbsent(key, mine);

        if(existing != null){
            return await(existing);
        }

        // LEADER: we're first, so do the real work
        try {
            T result= loader.get();
            mine.complete(result);
            return result;
        } catch (RuntimeException ex) {
            mine.completeExceptionally(ex);
            throw ex;
        } finally {
            inFlight.remove(key,mine);
        }
    }

    private T await(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException ex) {
            // Unwrap so followers see the leader's original exception (e.g. ShortUrlNotFoundException)
            if (ex.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw ex;
        }
    }
}
package org.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A data structure for a task whose result can be listened using a {@link CompletableFuture}.
 *
 * @param name
 * @param start
 * @param duration
 * @param listeners
 */
public record Task(int id, String name, LocalDateTime start, int duration, List<CompletableFuture<String>> listeners) {

    public Task(int id, String name, LocalDateTime now, int duration) {
        this(id, name, now, duration, new ArrayList<>());
    }

    public CompletableFuture<String> subscribe() {
        if (start.plusSeconds(duration).isBefore(LocalDateTime.now())) {
            return CompletableFuture.completedFuture("Task was alrelady completed :-)");
        }
        CompletableFuture<String> future = new CompletableFuture<>();
        listeners.add(future);
        return future;
    }

    public void unSubscribe(CompletableFuture<String> future) {
        listeners.remove(future);
    }

    public void complete(String result) {
        listeners.forEach(listener -> listener.complete(result));
    }

}

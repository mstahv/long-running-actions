package org.example;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppWideTasks {

    private static int nextId = 0;

    private List<Task> activeTasks = new ArrayList<>();

    public Task startTask(String name, int duration) {
        Task task = new Task(nextId++, name, LocalDateTime.now(), duration);
        activeTasks.add(task);
        return task;
    }

    public List<Task> getActiveTasks() {
        return new ArrayList<>(activeTasks);
    }

    @Scheduled(fixedRate = 100)
    void maintainTasks() {
        List<Task> finished = activeTasks.stream().filter(task -> task.start().plusSeconds(task.duration()).isBefore(LocalDateTime.now()))
                .toList();
        activeTasks.removeAll(finished);
        finished.forEach(task -> task.complete("Task " + task.name() + " completed at " + LocalTime.now()));

        if(activeTasks.isEmpty()) {
            // For the demo, make sure there is always at least one task running
            startTask("System Initiated #" + (nextId), 15);
        }
    }

}

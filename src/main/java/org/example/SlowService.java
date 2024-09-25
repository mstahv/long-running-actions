package org.example;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
public class SlowService {


    public String slowBlockingMethod(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return "Done in " + millis + "ms";
    }

    public String slowBlockingMethodWithNotifier(Consumer<Double> progressListener) {
        progressListener.accept(0.0);
        LocalTime start = LocalTime.now();
        for(int i = 0; i < 20; i++) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressListener.accept(i / 20.0);
        }
        return "Done! " + start + " -> " + LocalTime.now();
    }


    public CompletableFuture<String> generateStringAsync(int millis) {
        return CompletableFuture.supplyAsync(() -> slowBlockingMethod(millis));
    }

}

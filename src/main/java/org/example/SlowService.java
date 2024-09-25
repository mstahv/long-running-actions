package org.example;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * An example service that simulates slow some operations that are typical in real life projects.
 * This example is Vaadin agnostic and could exist.
 */
@Service
public class SlowService {

    /**
     * Simulates a slow blocking method that takes a certain amount of time to complete.
     *
     * @param millis a parameter that defines how long the method will take to complete
     * @return a "random" string representing the computation of the operation
     */
    public String slowBlockingMethod(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return "Done in " + millis + "ms";
    }

    /**
     * Simulates a slow blocking method that takes a certain amount of time to complete. This method also notifies a
     * listener about the progress of the operation.
     *
     * @param progressListener a listener that will be notified about the progress of the operation
     * @return a "random" string representing the computation of the operation
     */
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


    /**
     * Generates a string asynchronously. The method immediately returns a {@link CompletableFuture}, a helper in
     * JDKs that is nowadays very popular in asynchronous Java APIS. Users can attach listeners to the future that
     * will be called when the result is ready.
     *
     * @param millis a parameter that defines how long the method will take to complete
     * @return a future that will be completed with the result of the operation
     */
    public CompletableFuture<String> generateStringAsync(int millis) {
        return CompletableFuture.supplyAsync(() -> slowBlockingMethod(millis));
    }

}

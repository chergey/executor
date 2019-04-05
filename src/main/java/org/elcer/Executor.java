package org.elcer;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class Executor {

    private AtomicBoolean stopped = new AtomicBoolean();
    private DelayQueue<Task<?>> tasks = new DelayQueue<>();


    public Executor() {
        Thread dispenser = new Thread(() -> {
            while (!stopped.get()) {
                Task task;
                while ((task = tasks.poll()) != null) {
                    task.run();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    stopped.set(true);
                }
            }


        });
        dispenser.start();
    }


    public <V> Task<V> execute(LocalDateTime dateTime, Callable<V> callable) {
        Task<V> task = new Task<>(callable, dateTime);
        tasks.add(task);
        return task;

    }


    public void stop() {
        if (!stopped.compareAndSet(false, true))
            throw new IllegalStateException("Executor was already stopped!");
    }


}

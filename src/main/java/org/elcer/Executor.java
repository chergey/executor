package org.elcer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class Executor {

    private AtomicBoolean stopped = new AtomicBoolean();
    private DelayQueue<Task<?>> tasks = new DelayQueue<>();
    private static final TimeUnit DEFAULT_UNIT = TimeUnit.NANOSECONDS;

    private static Map<TimeUnit, ChronoUnit> UNITS = new HashMap<>() {
        {
            put(TimeUnit.DAYS, ChronoUnit.DAYS);
            put(TimeUnit.HOURS, ChronoUnit.HOURS);
            put(TimeUnit.MICROSECONDS, ChronoUnit.MICROS);
            put(TimeUnit.MILLISECONDS, ChronoUnit.MILLIS);
            put(TimeUnit.MINUTES, ChronoUnit.MINUTES);
            put(TimeUnit.NANOSECONDS, ChronoUnit.NANOS);
            put(TimeUnit.SECONDS, ChronoUnit.SECONDS);
        }
    };


    public Executor() {
        Thread dispenser = new Thread(() -> {
            while (!stopped.get()) {
                Task task;
                while ((task = tasks.poll()) != null) {
                    task.run();
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


    private class Task<V> implements Runnable, Delayed {
        private final Callable<V> action;
        private final LocalDateTime executionTime, submissionTime = LocalDateTime.now();

        private volatile V result;

        Task(Callable<V> action, LocalDateTime executionTime) {
            this.action = action;
            this.executionTime = executionTime;
        }

        @Override
        public void run() {
            try {
                result = action.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        public V getResult() {
            return result;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return LocalDateTime.now().until(executionTime, UNITS.get(unit));
        }

        @Override
        public int compareTo(Delayed o) {
            int res = Long.compare(this.getDelay(DEFAULT_UNIT), o.getDelay(DEFAULT_UNIT));
            if (res == 0 && o instanceof Task) {
                Task o1 = (Task) o;
                return submissionTime.compareTo((o1).submissionTime);
            }
            return res;
        }


    }


}

package org.elcer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Task<V> implements Runnable, Delayed {

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

    private static final TimeUnit DEFAULT_UNIT = TimeUnit.NANOSECONDS;


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
            return submissionTime.compareTo(o1.submissionTime);
        }
        return res;
    }


}

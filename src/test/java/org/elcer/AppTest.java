package org.elcer;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class AppTest {


    @Test
    public void testExecutor() throws InterruptedException {

        Executor executor = new Executor();

        List<Integer> executionOrder = new CopyOnWriteArrayList<>();

        executor.execute(LocalDateTime.now(), () -> executionOrder.add(1));
        executor.execute(LocalDateTime.now().plusSeconds(3), () -> executionOrder.add(2));
        executor.execute(LocalDateTime.now().plusSeconds(3), () -> executionOrder.add(3));
        executor.execute(LocalDateTime.now().plusSeconds(6), () -> executionOrder.add(4));
        executor.execute(LocalDateTime.now().plusSeconds(6), () -> executionOrder.add(5));
        executor.execute(LocalDateTime.now().plusSeconds(1), () -> executionOrder.add(6));

        executor.execute(LocalDateTime.now().plusNanos(1), () -> executionOrder.add(7));
        executor.execute(LocalDateTime.now().plusNanos(2), () -> executionOrder.add(8));
        executor.execute(LocalDateTime.now().plusNanos(3), () -> executionOrder.add(9));

        executor.execute(LocalDateTime.now().plusNanos(3), () -> executionOrder.add(10));
        executor.execute(LocalDateTime.now().plusNanos(3), () -> executionOrder.add(11));
        executor.execute(LocalDateTime.now().plusNanos(3), () -> executionOrder.add(12));

        Thread.sleep(10000);

        executor.stop();

        int[] orderNumbers = executionOrder.stream().mapToInt(o -> o).toArray();

        Assert.assertArrayEquals(new int[]{1, 7, 8, 9, 10, 11, 12, 6, 2, 3, 4, 5}, orderNumbers);

    }
}

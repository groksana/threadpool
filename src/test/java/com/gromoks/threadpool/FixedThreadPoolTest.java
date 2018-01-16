package com.gromoks.threadpool;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FixedThreadPoolTest {

    @Test
    public void testShutdownNow() {
        FixedThreadPool executor = new FixedThreadPool(3);

        Runnable run = () -> System.out.println(Thread.currentThread());

        for (int i = 0; i < 20; i++) {
            executor.execute(run);
        }

        List<Runnable> unfinishedTasks;
        unfinishedTasks = executor.shutdownNow();

        assertTrue(unfinishedTasks.size() > 0);
    }

    @Test
    public void testShutdown() throws InterruptedException {
        FixedThreadPool executor = new FixedThreadPool(3);

        AtomicInteger taskCount = new AtomicInteger(0);

        Runnable run = taskCount::getAndIncrement;

        for (int i = 0; i < 20; i++) {
            executor.execute(run);
            if (i == 9) {
                executor.shutdown();
            }
        }

        Thread.sleep(1000);
        assertEquals(taskCount.get(), 10);
    }

}

package com.gromoks.threadpool;

import java.util.List;

public class Main {
    static FixedThreadPool executor = new FixedThreadPool(10);

    public static void main(String[] args) throws InterruptedException {
        Runnable run = () -> System.out.println(Thread.currentThread());
        for (int i = 0; i < 5; i++) {
            executor.execute(run);
        }

        Thread.sleep(3);

        executor.printThreadState();
        //executor.shutdown();
        List<Runnable> unfinishedTasks = executor.shutdownNow();
        unfinishedTasks.forEach(System.out::println);

        Thread.sleep(3000);

        executor.printThreadState();
    }
}

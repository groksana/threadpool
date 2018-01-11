package com.gromoks.threadpool;

import java.util.List;

public class Main {
    static FixedThreadPool executor = new FixedThreadPool(3);

    public static void main(String[] args) {
        Runnable run = () -> System.out.println(Thread.currentThread());
        for (int i = 0; i < 5; i++) {
            executor.execute(run);
        }

        List<Runnable> unfinishedTasks;
        unfinishedTasks = executor.shutdownNow();
        unfinishedTasks.forEach(System.out::println);
    }
}

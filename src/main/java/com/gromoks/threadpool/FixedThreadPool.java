package com.gromoks.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class FixedThreadPool implements Executor {

    private final Thread[] threadPool;
    private final Queue<Runnable> runnableTaskQueue = new LinkedBlockingQueue<>();
    private volatile boolean activeQueue;

    public FixedThreadPool(int threadCount) {
        threadPool = new Thread[threadCount];
        activeQueue = true;
        Runnable taskRunner = this::taskRunner;

        for (int i = 0; i < threadPool.length; i++) {
            Thread thread = new Thread(taskRunner);
            threadPool[i] = thread;
            thread.start();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (activeQueue) {
            runnableTaskQueue.offer(command);
        }
    }

    public void shutdown() {
        activeQueue = false;
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> runnableList = new ArrayList<>();

        shutdown();

        for (int i = 0; i < threadPool.length; i++) {
            threadPool[i].interrupt();
        }

        synchronized (runnableTaskQueue) {
            runnableList.addAll(runnableTaskQueue);
            runnableTaskQueue.clear();
        }

        return runnableList;
    }

    private void taskRunner() {
        while (!isTerminated()) {
            Runnable task = runnableTaskQueue.poll();
            if (task != null) {
                String name = Thread.currentThread().getName();
                System.out.println("Task Started by Thread :" + name);
                task.run();
                System.out.println("Task Finished by Thread :" + name);
            }
        }
    }

    private boolean isTerminated() {
        return runnableTaskQueue.isEmpty() && !activeQueue;
    }
}

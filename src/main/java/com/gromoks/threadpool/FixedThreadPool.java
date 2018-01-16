package com.gromoks.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class FixedThreadPool implements Executor {

    private final Thread[] threadPool;
    private final BlockingQueue<Runnable> runnableTaskQueue = new LinkedBlockingQueue<>();
    private volatile boolean activeQueue;

    public FixedThreadPool(int threadCount) {
        threadPool = new Thread[threadCount];
        activeQueue = true;
        Runnable taskRunner = this::taskRunner;

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(taskRunner);
            threadPool[i] = thread;
            thread.start();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (activeQueue) {
            synchronized (runnableTaskQueue) {
                runnableTaskQueue.offer(command);
                runnableTaskQueue.notify();
            }
        }
    }

    public void shutdown() {
        activeQueue = false;
        synchronized (runnableTaskQueue) {
            runnableTaskQueue.notifyAll();
        }
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

    public void printThreadState() {
        for (Thread thread : threadPool) {
            System.out.println("Status of " + thread.getName() + " - " + thread.getState());
        }
    }

    private void taskRunner() {
        while (!isTerminated()) {
            Runnable task;

            synchronized (runnableTaskQueue) {
                while (runnableTaskQueue.isEmpty()) {
                    try {
                        runnableTaskQueue.wait();
                        if (!activeQueue) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        System.out.println("An error occurred for " + Thread.currentThread().getName() + " while queue is waiting: " + e.getMessage());
                        break;
                    }
                }
                task = runnableTaskQueue.poll();
            }
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

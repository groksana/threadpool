package com.gromoks.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class FixedThreadPool implements Executor {

    private final BlockingQueue<Runnable> runnableTaskQueue = new LinkedBlockingQueue<>();
    private volatile boolean activeQueue;

    private final List<Thread> threadPool = new ArrayList<>();
    private final int capacity;
    private int activeThreadCount = 0;

    public FixedThreadPool(int threadCount) {
        activeQueue = true;
        capacity = threadCount;
    }

    @Override
    public void execute(Runnable command) {
        if (activeQueue) {
            if (activeThreadCount < capacity) {
                threadInit();
            }
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
        threadPool.forEach(Thread::interrupt);

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

    private void threadInit() {
        activeThreadCount++;
        Runnable taskRunner = this::taskRunner;
        Thread thread = new Thread(taskRunner);
        threadPool.add(thread);
        thread.start();
    }

    private void taskRunner() {
        while (!isTerminated()) {
            Runnable task = null;

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
                if (!Thread.currentThread().isInterrupted()) {
                    task = runnableTaskQueue.poll();
                } else {break;}
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

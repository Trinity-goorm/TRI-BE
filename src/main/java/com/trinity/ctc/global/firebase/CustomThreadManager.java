package com.trinity.ctc.global.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.ThreadManager;

import java.util.concurrent.*;

public class CustomThreadManager extends ThreadManager {

    @Override
    protected ExecutorService getExecutor(FirebaseApp firebaseApp) {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("firebase-msg-" + thread.getId());
            thread.setDaemon(false);
            thread.setPriority(Thread.MAX_PRIORITY);
            return thread;
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                100,
                100,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory
        );

        executor.prestartAllCoreThreads();

        return executor;
    }

    @Override
    protected void releaseExecutor(FirebaseApp firebaseApp, ExecutorService executorService) {
        executorService.shutdownNow();  // 스레드 풀 강제 종료
    }

    @Override
    protected ThreadFactory getThreadFactory() {
        return Executors.defaultThreadFactory();
    }
}
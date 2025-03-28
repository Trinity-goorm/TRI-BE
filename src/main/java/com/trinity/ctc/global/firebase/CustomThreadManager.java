package com.trinity.ctc.global.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.ThreadManager;

import java.util.concurrent.*;

public class CustomThreadManager extends ThreadManager {

    // FireBase SDK에 할당할 스레드 풀 설정 -> 발송 스레드
    @Override
    protected ExecutorService getExecutor(FirebaseApp firebaseApp) {

        // 스레드 이름, 우선순위 설정
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("firebase-msg-" + thread.getId());
            thread.setDaemon(false);
            thread.setPriority(9);
            return thread;
        };

        // 커스텀 스레드풀 초기화
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                100,
                100,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory
        );

        // application init 시, 스레드풀 내 스레드 바로 할당 
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

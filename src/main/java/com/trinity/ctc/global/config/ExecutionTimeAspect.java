package com.trinity.ctc.global.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class ExecutionTimeAspect {

    private static final ThreadLocal<AtomicInteger> indentation = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    @Around("execution(* com.trinity.ctc.domain..*(..))") //search api 호출 시간 측정
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        int depth = indentation.get().getAndIncrement();  // 들여쓰기 깊이 증가
        long startTime = System.nanoTime();
        Object result = joinPoint.proceed();
        long endTime = System.nanoTime();
        indentation.get().decrementAndGet(); // 깊이 감소

        String methodName = joinPoint.getSignature().toShortString();
        String indent = "  ".repeat(depth);  // 들여쓰기 적용
        System.out.println(indent + methodName + " 실행 시간: " + (endTime - startTime) / 1_000_000 + " ms");

        return result;
    }
}


package com.xladmt.makify.common.aop;

import com.xladmt.makify.common.util.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Service/Facade 레이어의 모든 메서드를 AOP로 로깅
 * 메서드 실행시간, 파라미터, 반환값, 예외 정보를 기록
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ServiceLoggingAspect {

    private final SensitiveDataMasker sensitiveDataMasker;

    /**
     * Service 및 Facade 클래스의 public 메서드 로깅
     */
    @Around("execution(* com.xladmt.makify..service..*(..))" +
            " || execution(* com.xladmt.makify..facade..*(..))" +
            " || execution(* com.xladmt.makify..application..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1️⃣ 메서드 정보 수집
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 2️⃣ 메서드 시작 로그
        log.info("───────────────────────────────────────────────────────────────");
        log.info("[SERVICE START] {}.{}", className, methodName);
        
        if (args.length > 0) {
            String maskedArgs = maskMethodArguments(args);
            log.info("  Parameters: [{}]", maskedArgs);
        }

        // 3️⃣ 메서드 실행
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();

            // 4️⃣ 메서드 성공 로그
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[SERVICE END] {}.{}", className, methodName);
            
            if (result != null) {
                String maskedResult = sensitiveDataMasker.maskObject(result);
                log.info("  Return Value: {}", maskedResult);
            } else {
                log.info("  Return Value: null");
            }
            
            log.info("  Execution Time: {}ms", executionTime);
            log.info("───────────────────────────────────────────────────────────────");

            return result;

        } catch (Throwable e) {
            // 5️⃣ 메서드 실패 로그
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("───────────────────────────────────────────────────────────────");
            log.error("[SERVICE ERROR] {}.{}", className, methodName);
            log.error("  Exception Type: {}", e.getClass().getSimpleName());
            log.error("  Exception Message: {}", e.getMessage());
            log.error("  Execution Time: {}ms", executionTime);
            log.error("  Stack Trace: ", e);
            log.error("───────────────────────────────────────────────────────────────");

            throw e;
        }
    }

    /**
     * 메서드 파라미터 마스킹
     */
    private String maskMethodArguments(Object[] args) {
        return Arrays.stream(args)
            .map(arg -> {
                if (arg == null) {
                    return "null";
                }
                // 기본 타입은 그대로, 복잡한 객체는 마스킹
                if (isPrimitiveType(arg)) {
                    return arg.toString();
                }
                return sensitiveDataMasker.maskObject(arg);
            })
            .collect(Collectors.joining(", "));
    }

    /**
     * 기본 타입 확인
     */
    private boolean isPrimitiveType(Object obj) {
        return obj instanceof String ||
               obj instanceof Integer ||
               obj instanceof Long ||
               obj instanceof Double ||
               obj instanceof Float ||
               obj instanceof Boolean ||
               obj instanceof Byte ||
               obj instanceof Short ||
               obj instanceof Character;
    }
}

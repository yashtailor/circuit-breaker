package com.yash.experiments.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class KafkaListenerCircuitBreakerAspect {
    @Autowired CustomCircuitBreakerRegistry circuitBreakerRegistry;
    @Around("@annotation(CustomCircuitBreaker)")
    public void interceptKafkaListener(ProceedingJoinPoint joinPoint) throws Throwable {
        String name = ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(CustomCircuitBreaker.class).name();
//        log.info(String.valueOf(joinPoint));
        log.info("ck:"+name);
        circuitBreakerRegistry.circuitBreakerRegistry().circuitBreaker(name).decorateRunnable(()->{
            try {
                joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }).run();
    }
}

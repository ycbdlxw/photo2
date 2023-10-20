package com.ycbd.photoservice.interceptors;

import java.util.Arrays;

import javax.annotation.Resource;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ycbd.photoservice.services.LogDaoService;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

// @Aspect
// @Component
// @Order(1)
 @Slf4j
public class LoggingAspect {
    @Resource
     protected LogDaoService logDaoService;//com.electrum.datacapture.mapper.logDataMapper logDataMapper;
    ThreadLocal<Long> startTime = new ThreadLocal<>();
    /**
     * 环绕通知：目标方法执行前后分别执行一些代码，发生异常的时候执行另外一些代码
     *
     * @return
     */
    @Around(value = "execution(* com.ycbd.photoservice.services..*.*(..))")
    public Object aroundMethod(ProceedingJoinPoint jp) {
        Object result = null;
        Long endTime = Long.valueOf(0);
        startTime.set(System.currentTimeMillis());
        String methodName = jp.getSignature().getName();
        try {

            log.error("【环绕通知中的--->前置通知】：the method 【" + methodName + "】 begins with "
                    + Arrays.asList(jp.getArgs()));
            // 执行目标方法
            result = jp.proceed();
            endTime = System.currentTimeMillis() - startTime.get();
        } catch (Throwable e) {
            System.out.println("【环绕通知中的--->异常通知】：the method 【" + methodName + "】 occurs exception " + e);
        }
       
        return result;
    }
}

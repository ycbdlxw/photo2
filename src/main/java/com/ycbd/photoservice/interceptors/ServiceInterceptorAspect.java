package com.ycbd.photoservice.interceptors;

import java.lang.annotation.Repeatable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ycbd.photoservice.services.LogDaoService;
import com.ycbd.photoservice.tools.Tools;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class ServiceInterceptorAspect {
    @Resource
     protected LogDaoService logDaoService;
     
     @Value("${system.developflag:true}")
    boolean developflag;

    @Around("execution(* com.ycbd.photoservice.services..*.*(..))")
    public Object interceptServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取用户IP地址
        String ipAddress = "";
        // 获取浏览器名称
        String browserName = getBrowserName();
        // 获取token
        String token = getToken();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        ipAddress= Tools.getIpAddr(request);
        // 保存到数据库表log中
        saveToLogTable(ipAddress, browserName, token);
         String methodName = joinPoint.getSignature().getName();
        log.info(ipAddress+","+browserName+","+token+",【环绕通知中的--->前置通知】:the method 【" + methodName + "】 begins with "
                    + Arrays.asList(joinPoint.getArgs()));
        // 执行目标方法
        Object result=null;
        JSONObject jsonResult=new JSONObject();       
        switch (methodName){
            case "login":
                    result = joinPoint.proceed();
                    jsonResult = JSONUtil.parseObj(result);
                    if(jsonResult.size()>0){
                    token=generateToken(jsonResult);
                    jsonResult.putOnce("token",token);
                    }
                    log.info(methodName+" "+jsonResult.toString());
                    return jsonResult;
            default:
                    
                    if (JWTUtil.verify(token, "ycbd".getBytes()) || developflag) {
                    // 进行后续的服务调用
                    result = joinPoint.proceed();
                
                    } else {
                        String message="token 验证不通过  : "+token;
                        log.info(message);
                        Map<String,Object> resultMap=new HashMap<>();
                        resultMap.put("error",message);
                        log.info(message);
                        result=resultMap;
                        // 返回验证不通过的结果
                    }
            return result;
        }
      
    }
    private String generateToken(JSONObject result){
        String token =JWTUtil.createToken(result, "ycbd".getBytes());
        return token;
    }

    private String getBrowserName() {
        // 获取浏览器名称的逻辑
        // ...
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String browserName = request.getHeader("User-Agent");
    if (StrUtil.isEmpty(browserName)) {
        browserName = "Unknown";
    }
    return browserName;
    }

    private String getToken() {
        // 获取token的逻辑
        // ...
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String token = request.getHeader("authorization");
    if (StrUtil.isEmpty(token)) {
        token = "";
    }
    return token;
    }

    private void saveToLogTable(String ipAddress, String browserName, String token) {
        // 将信息保存到数据库表log中的逻辑
        // ...
    }
}
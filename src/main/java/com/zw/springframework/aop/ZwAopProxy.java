package com.zw.springframework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

//默认使用JDK代理
public class ZwAopProxy implements InvocationHandler {

    //目标类
    private Object target;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        return null;
    }
}

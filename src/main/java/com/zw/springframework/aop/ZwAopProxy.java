package com.zw.springframework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

//默认使用JDK代理
public class ZwAopProxy implements InvocationHandler {

    //目标类
    private Object target;

    private ZwAopConfig zwAopConfig;

    public Object getProxy(Object instance){
        this.target = instance;
        Class<?> clazz = instance.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //获得原始方法
        Method m = this.target.getClass().getMethod(method.getName(), method.getParameterTypes());

        //在原始方法调用以前要执行增强的代码
        if(zwAopConfig.contains(m)){
            ZwAopConfig.Aspect aspect = zwAopConfig.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }
        //反射调用原始方法
        Object obj = method.invoke(this.target, args);
        //在原始方法调用以后要执行增强的代码
        if(zwAopConfig.contains(m)){
            ZwAopConfig.Aspect aspect = zwAopConfig.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }
        //将最原始的返回值返回出去
        return obj;
    }

    public void setConfig(ZwAopConfig zwAopConfig) {
        this.zwAopConfig = zwAopConfig;
    }
}

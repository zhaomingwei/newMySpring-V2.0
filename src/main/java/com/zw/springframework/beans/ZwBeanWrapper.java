package com.zw.springframework.beans;

import com.zw.springframework.aop.ZwAopConfig;
import com.zw.springframework.aop.ZwAopProxy;

public class ZwBeanWrapper {

    private ZwAopProxy zwAopProxy = new ZwAopProxy();

    //还会用到  观察者  模式
    //1、支持事件响应，会有一个监听
    private ZwBeanPostProcessor zwBeanPostProcessor;

    //包装原始对象后的对象
    private Object wrapperInstance;

    //保存原始对象
    private Object originalInstance;

    public ZwBeanWrapper(Object instance) {
        //进行包装
        this.wrapperInstance = zwAopProxy.getProxy(instance);
        this.originalInstance = instance;
    }

    public ZwBeanPostProcessor getZwBeanPostProcessor() {
        return zwBeanPostProcessor;
    }

    public void setZwBeanPostProcessor(ZwBeanPostProcessor zwBeanPostProcessor) {
        this.zwBeanPostProcessor = zwBeanPostProcessor;
    }


    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public void setWrapperInstance(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }

    public Object getOriginalInstance() {
        return originalInstance;
    }

    public void setOriginalInstance(Object originalInstance) {
        this.originalInstance = originalInstance;
    }

    public void setAopConfig(ZwAopConfig config) {
        zwAopProxy.setConfig(config);
    }

}

package com.zw.springframework.support;

public class ZwBeanWrapper {

    //包装原始对象后的对象
    private Object wrapperInstance;

    //保存原始对象
    private Object originalInstance;

    public ZwBeanWrapper(Object instance) {
        //进行包装
        this.wrapperInstance = instance;
        this.originalInstance = instance;
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
}

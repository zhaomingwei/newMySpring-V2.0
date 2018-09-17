package com.zw.springframework.beans;

//用作事件监听
public class ZwBeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName){
        System.out.println(beanName + ":开始包装");
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName){
        System.out.println(beanName + ":包装完成");
        return bean;
    }


}

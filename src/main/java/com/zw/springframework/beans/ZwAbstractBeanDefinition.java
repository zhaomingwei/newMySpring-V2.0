package com.zw.springframework.beans;

import com.zw.springframework.config.ZwBeanDefinition;

public class ZwAbstractBeanDefinition extends ZwBeanDefinition {
    private String beanClassName;

    private String factoryBeanName;

    private boolean lazyInit = false;

    //beanClassName
    public void setBeanClassName(String beanClassName){
        this.beanClassName = beanClassName;
    };

    //获取BeanClassname
    public String getBeanClassName(){
        return beanClassName;
    };

    //设置懒加载标识，true-是，false-否
    public void setLazyInit(boolean lazyInit){
        this.lazyInit = lazyInit;
    };

    //是否是懒加载
    public boolean isLazyInit(){
        return lazyInit;
    };

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
}

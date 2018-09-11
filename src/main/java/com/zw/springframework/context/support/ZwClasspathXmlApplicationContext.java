package com.zw.springframework.context.support;

import com.zw.springframework.annotation.Autowired;
import com.zw.springframework.annotation.Controller;
import com.zw.springframework.annotation.Service;
import com.zw.springframework.config.ZwBeanDefinition;
import com.zw.springframework.core.util.ZwAssert;
import com.zw.springframework.support.ZwBeanWrapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZwClasspathXmlApplicationContext {

    //配置文件位置，暂用String数组代替
    private String[] configResources;

    //存储所有beanClass
    private ZwBeanDefinitionReader reader;

    //Map 存储BeanDefinition
    private Map<String, ZwBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, ZwBeanDefinition>();

    //用来保证注册式单例的容器,可以认为是IOC的缓存，每次注册先来此容器检查有没有重复的bean，没有添加，有则直接取用
    private Map<String, Object> beanCacheMap = new HashMap<String, Object>();

    //用来存储所有被代理过的对象
    private Map<String, ZwBeanWrapper> beanWrapperMap = new ConcurrentHashMap<String, ZwBeanWrapper>();

    //单个文件构造函数，把单个文件放在数组里在调用参数为一个数组的构造函数
    public ZwClasspathXmlApplicationContext(String configResources) {
        this(new String[]{configResources});
    }

    public ZwClasspathXmlApplicationContext(String ... configResources) {
        //设置bean资源文件路径
        setConfigLocations(configResources);
        refresh();
    }

    private void refresh() {
        //定位
        this.reader = new ZwBeanDefinitionReader(configResources);
        //加载
        List<String> beanDefinitionList = this.reader.loadBeanDefinition();
        //注册
        doRegistry(beanDefinitionList);
        //依赖注入
        doAutowired();
    }

    private void doAutowired() {
        for(Map.Entry<String, ZwBeanDefinition> entry : this.beanDefinitionMap.entrySet()){
            //是否延迟加载
            if(!entry.getValue().isLazyInit()){
                getBean(entry.getValue());
            }
        }

        for(Map.Entry<String, ZwBeanWrapper> zwBeanWrapperEntry : this.beanWrapperMap.entrySet()){
            populateBean(zwBeanWrapperEntry.getKey(), zwBeanWrapperEntry.getValue().getOriginalInstance());
        }

    }

    //开始真正的注入
    private void populateBean(String key, Object originalInstance) {
        //获取传入对象的类型
        Class<?> clazz = originalInstance.getClass();
        //如果这个class没有Controller 和 service注解则返回
        if(!clazz.isAnnotationPresent(Controller.class) && !clazz.isAnnotationPresent(Service.class)){
            return;
        }
        //获取这个类所有的字段(不管访问权限)
        Field[] fields = clazz.getDeclaredFields();
        if(fields.length == 0){
            return;
        }
        //遍历所有字段
        for(Field filed:fields){
            //不是Autowired的注解则进行下一个
            if(!filed.isAnnotationPresent(Autowired.class)){
                continue;
            }
            //获取Autowired注解信息
            Autowired autowired = filed.getAnnotation(Autowired.class);
            //获取注解想要注入的类型的名称
            String autowiredName = autowired.value().trim();
            if("".startsWith(autowiredName)){
                //如果注解想要注入的类型名字为空，则获取定义该字段的类型名称
                autowiredName = filed.getType().getName();
            }
            //强制设置该字段可访问(包括private等)
            filed.setAccessible(true);
            try{
                //注入开始
                //从beanWrapperMap中获取key为autowiredName的wrapperInstance的包装类赋值给该字段
                filed.set(originalInstance, this.beanWrapperMap.get(autowiredName).getWrapperInstance());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private Object getBean(ZwBeanDefinition zwBeanDefinition) {
        Object instance = instanceBean(zwBeanDefinition);
        if(null==instance){
            return null;
        }
        ZwBeanWrapper zwBeanWrapper = new ZwBeanWrapper(instance);
        beanWrapperMap.put(zwBeanDefinition.getBeanClassName(), zwBeanWrapper);
        //返回的这个WrapperInstance是我们通过动态代理后的对象
        return beanWrapperMap.get(zwBeanDefinition.getBeanClassName()).getWrapperInstance();
    }

    private Object instanceBean(ZwBeanDefinition zwBeanDefinition) {
        Object instance = null;
        String className = zwBeanDefinition.getBeanClassName();
        try {
            if(this.beanCacheMap.containsKey(className)){
                instance = beanCacheMap.get(className);
            }else{
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();//构造函数实例化一个变量
                beanCacheMap.put(className, zwBeanDefinition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private void doRegistry(List<String> beanDefinitionList) {
        //beanName有三种情况：
        //1、默认类名首字符小写
        //2、自定义名字
        //3、接口注入
        for(String className:beanDefinitionList){
            Class<?> clazz = null;
            try{
                clazz = Class.forName(className);
            }catch (Exception e){
                e.printStackTrace();
            }

            //如果是接口则不实例化，使用实现类注入
            if(clazz == null || clazz.isInterface()){
                continue;
            }

            //注册bean
            ZwBeanDefinition beanDefinition = this.reader.registerBean(className);

            if(null!=beanDefinition){
                this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            }
            Class<?>[] interfaces = clazz.getInterfaces();
            if(null!=interfaces && interfaces.length>0){
                for(Class<?> cls:interfaces){
                    //如果是多个实现类则会覆盖，需要自定义名称
                    this.beanDefinitionMap.put(cls.getName(), beanDefinition);
                }
            }
            //容器初始化完毕
        }
    }

    /**
     * 解析Bean定义资源文件的路径，处理多个资源文件字符串数组
     * @param configResources
     */
    private void setConfigLocations(String ... configResources){
        if(null != configResources){
            ZwAssert.noNullElements(configResources, "Config locations must not be null");
            this.configResources = new String[configResources.length];
            for(int i = 0; i < configResources.length; i++){
                this.configResources[i] = configResources[i].trim();
            }

        }else{
            this.configResources = null;
        }
    }


}

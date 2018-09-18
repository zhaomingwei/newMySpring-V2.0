package com.zw.springframework.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class ZwAopProxyUtils {

     public static Object getTargetObject(Object object) throws Exception {
         //不是代理类直接返回
         if(!isAopProxy(object)){
             return object;
         }
         //是代理类
         return getProxyTargetObject(object);
     }

     //判断是否是代理类
     private static boolean isAopProxy(Object object){
         return Proxy.isProxyClass(object.getClass());
     }

     private static Object getProxyTargetObject(Object object) throws Exception {
         //获取代理对象中指定字段（这里是h）的值
         Field h = object.getClass().getSuperclass().getDeclaredField("h");
         h.setAccessible(true);

         ZwAopProxy zwAopProxy = (ZwAopProxy) h.get(object);
         //获取Proxy对象中的此字段的值
         Field target = zwAopProxy.getClass().getDeclaredField("target");
         target.setAccessible(true);
         return target.get(zwAopProxy);
     }

}

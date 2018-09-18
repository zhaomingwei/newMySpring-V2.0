package com.zw.springframework.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

//对配置文件中的aop表达式进行读取封装
public class ZwAopConfig {

    //以目标对象需要增强的方法为key，增强内容为value
    private Map<Method, Aspect> points = new HashMap<Method, Aspect>();

    public void put(Method method, Object aspect, Method[] points){
        this.points.put(method, new Aspect(aspect, points));
    }

    public Aspect get(Method method){
        return this.points.get(method);
    }

    public boolean contains(Method method){
        return this.points.containsKey(method);
    }

    class Aspect{
        private Object aspect;//切面对象
        private Method[] points;//切面的所有方法

        public Object getAspect() {
            return aspect;
        }

        public Method[] getPoints() {
            return points;
        }

        public Aspect(Object aspect, Method[] points) {

            this.aspect = aspect;
            this.points = points;
        }
    }

}

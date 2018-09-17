package com.zw.springframework.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class ZwHandlerAdapter {

    private Map<String, Integer> paramMapping;

    public ZwHandlerAdapter(Map paramMapping) {
        this.paramMapping = paramMapping;
    }

    //将请求参数与方法参数进行动态匹配
    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, ZwHandlerMapping handler) throws InvocationTargetException, IllegalAccessException {
        //1、准备方法形参列表
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();

        //2、拿到自定义命名参数所在位置
        //用户通过URL传过来的参数列表
        Map<String, String[]> reqParamMap = req.getParameterMap();

        //3、构造实参列表
        Object[] paramValues = new Object[paramTypes.length];
        for(Map.Entry<String, String[]> entry:reqParamMap.entrySet()){
           String value = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");
           if(!this.paramMapping.containsKey(entry.getKey())){
               continue;
           }
           int index = this.paramMapping.get(entry.getKey());
           //页面上传过来的全是String类型，实际参数类型多样，需要转换
           paramValues[index] = castStringValue(value, paramTypes[index]);
        }

        if(this.paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if(this.paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }

        //4、从handler中取出controller、method，然后利用反射机制进行调用
        Object result = handler.getMethod().invoke(handler.getController(), paramValues);
        if(null==result){
            return null;
        }
        boolean isModelAndView = handler.getMethod().getReturnType() == ModelAndView.class;
        if(isModelAndView){
            return (ModelAndView) result;
        }else{
            return null;
        }
    }

    private Object castStringValue(String value, Class<?> paramType) {
        if(paramType == String.class){
            return value;
        }else if(paramType == Integer.class){
            return Integer.valueOf(value);
        }else if(paramType == int.class){
            return Integer.valueOf(value).intValue();
        }
        return null;
    }
}

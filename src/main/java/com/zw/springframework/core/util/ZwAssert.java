package com.zw.springframework.core.util;

public abstract class ZwAssert {

    /**
     * 判断传入的数组中每个元素是否为空，为空则抛出异常
     * @param array
     * @param message
     */
    public static void noNullElements(Object[] array, String message) {
        if(null!=array){
            for(Object element : array){
                if(null == element){
                    throw new IllegalArgumentException(message);
                }
            }
        }
    }

}

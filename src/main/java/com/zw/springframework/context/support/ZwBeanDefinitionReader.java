package com.zw.springframework.context.support;

import com.zw.springframework.support.ZwAbstractBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ZwBeanDefinitionReader {

    //配置
    private Properties config = new Properties();

    //配置文件中获取自动扫描的包名的key
    private String SCAN_PACKAGE = "scanPackage";

    //保存所有的class
    private List<String> registerBeanClasses = new ArrayList();

    public ZwBeanDefinitionReader(String ... configResources) {
        for(String configResource : configResources){
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(configResource.replace("classpath:", ""));
            try {
                config.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(null!=is){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    //递归扫描所有相关class保存在一个List中
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for(File file : classDir.listFiles()){
            if(file.isDirectory()){
                doScanner(packageName + "." + file.getName());
            }else{
                registerBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }


    }

    public List<String> loadBeanDefinition() {
        return this.registerBeanClasses;
    }

    //每注册一个bean则返回一个BeanDefinition对象
    public ZwAbstractBeanDefinition registerBean(String className) {
        if(registerBeanClasses.contains(className)){
            ZwAbstractBeanDefinition zwBeanDefinition = new ZwAbstractBeanDefinition();
            zwBeanDefinition.setBeanClassName(className);
            zwBeanDefinition.setFactoryBeanName(lowFirstCase(className.substring(className.lastIndexOf(".") + 1)));
            return zwBeanDefinition;
        }
        return null;
    }


    /**
     * 首字母小写转换
     * @param str
     * @return
     */
    private String lowFirstCase(String str){
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig(){
        return this.config;
    }
}

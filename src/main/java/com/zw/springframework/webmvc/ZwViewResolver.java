package com.zw.springframework.webmvc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZwViewResolver {

    private String templateName;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }

    private File templateFile;

    public ZwViewResolver(String templateName, File templateFile) {
        this.templateName = templateName;
        this.templateFile = templateFile;
    }

    //1、先以utf-8编码读取文件
    //2、然后用正则匹配￥{}
    //3、如果能匹配到，则开始遍历匹配的总个数开始替换
    public String viewResolver(ModelAndView mv) throws Exception {
        StringBuffer sb = new StringBuffer();
        //创建从中读取和向其中写入（可选）的随机访问文件流, "r" 以只读方式打开
        RandomAccessFile ra = new RandomAccessFile(this.templateFile, "r");

        String line;
        //把文件读出存入StringBuffer，StringBuffer线程安全，StringBuilder线程不安全
        while (null!=(line = ra.readLine())){
            line = new String(line.getBytes("ISO-8859-1"), "utf-8");
            Matcher matcher = matcher(line);
            while(matcher.find()){
                //捕获组是从 1 开始从左到右的索引
                for(int i=1;i<=matcher.groupCount();i++){
                    //取出参数名
                    String paramName = matcher.group(i);
                    //取出参数值
                    Object paramValue = mv.getModel().get(paramName);
                    if (null == paramValue) {
                        continue;
                    }
                    line = line.replaceAll("￥\\{" + paramName + "\\}", paramValue.toString());
                }
            }
            line = new String(line.getBytes("utf-8"), "ISO-8859-1");
            sb.append(line);
        }
        return sb.toString();

    }

    private Matcher matcher(String line) {
        Pattern pattern = Pattern.compile("￥\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(line);
    }
}

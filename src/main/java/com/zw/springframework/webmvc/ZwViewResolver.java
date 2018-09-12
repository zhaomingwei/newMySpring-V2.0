package com.zw.springframework.webmvc;

import java.io.File;

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

    public String viewResolver(ModelAndView mv) {

        return null;

    }
}

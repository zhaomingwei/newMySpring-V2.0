package com.zw.springframework.webmvc.servlet;

import com.zw.springframework.context.support.ZwClasspathXmlApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DispatchServlet extends HttpServlet {

    private final String DEFAULT_CONFIG_LOCATION = "contextConfigLocation";

    @Override
    public void init(ServletConfig config) throws ServletException {
        //相当于把IOC容器初始化了
        ZwClasspathXmlApplicationContext ctx = new ZwClasspathXmlApplicationContext(config.getInitParameter(DEFAULT_CONFIG_LOCATION));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}

package com.zw.springframework.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ZwHandlerAdapter {

    private Map paramMapping;

    public ZwHandlerAdapter(Map paramMapping) {
        this.paramMapping = paramMapping;
    }

    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, ZwHandlerMapping handler) {
        return null;
    }
}

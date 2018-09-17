package com.zw.springframework.webmvc.servlet;

import com.zw.springframework.annotation.Controller;
import com.zw.springframework.annotation.RequestMapping;
import com.zw.springframework.annotation.RequestParam;
import com.zw.springframework.context.support.ZwClasspathXmlApplicationContext;
import com.zw.springframework.webmvc.ModelAndView;
import com.zw.springframework.webmvc.ZwHandlerAdapter;
import com.zw.springframework.webmvc.ZwHandlerMapping;
import com.zw.springframework.webmvc.ZwViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchServlet extends HttpServlet {

    private final String DEFAULT_CONFIG_LOCATION = "contextConfigLocation";

    private final String DEFAULT_VIEW_TEMPLATE = "templateRoot";

    //保存请求地址与方法映射的实体类
    private List<ZwHandlerMapping> handlerMappings = new ArrayList<ZwHandlerMapping>();

    //保存每一个handler对应的参数列表
    private Map<ZwHandlerMapping, ZwHandlerAdapter> handlerAdapterMap = new HashMap<ZwHandlerMapping, ZwHandlerAdapter>();

    //记录页面模板文件
    private List<ZwViewResolver> viewResolvers = new ArrayList<ZwViewResolver>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //相当于把IOC容器初始化了
        ZwClasspathXmlApplicationContext ctx = new ZwClasspathXmlApplicationContext(config.getInitParameter(DEFAULT_CONFIG_LOCATION));
        initStrategies(ctx);
    }

    //九种策略
    //对于每一个请求，每种策略能对其进行处理，但是最终会返回ModelAndView
    private void initStrategies(ZwClasspathXmlApplicationContext ctx) {
        //文件上传解析，如果请求类型是multipart将通过MultipartResolver进行文件上传解析
        initMultipartResolver(ctx);
        //本地化解析
        initLocaleResolver(ctx);
        //主题解析
        initThemeResolver(ctx);


        //用来保存Controller中配置的RequestMapping和Method的一个对应关系(自己实现核心思想)
        //通过HandlerMapping，将请求映射到处理器(自己实现核心思想)
        initHandlerMappings(ctx);
        //HandlerAdapters 用来动态匹配Method参数，包括类转换，动态赋值
        //通过HandlerAdapter进行多类型的参数动态匹配(自己实现核心思想)
        initHandlerAdapters(ctx);
        //通过ViewResolvers实现动态模板的解析
        //自己解析一套模板语言
        //通过viewResolver解析逻辑视图到具体视图实现
        initViewResolvers(ctx);


        initHandlerExceptionResolvers(ctx);
        initRequestToViewNameTranslator(ctx);
        initFlashMapManager(ctx);
    }

    //将Controller中配置的RequestMapping和Method进行一一对应
    private void initHandlerMappings(ZwClasspathXmlApplicationContext ctx) {
        //弄一个map维护map.put(url,Method)
        //从容器中获取所有实例
        String[] beanNames = ctx.getBeanDefinitionNames();
        //遍历所有bean实例
        for(String beanName : beanNames){
            Object controller = ctx.getBean(beanName);
            Class<?> clazz = controller.getClass();
            //如果该实例没有Controller注解，继续下一个
            if(!clazz.isAnnotationPresent(Controller.class)){
                continue;
            }

            String baseUrl = "";
            //如果有Controller并且有RequestMapping注解则把RequestMapping带有的值拿出来作为基准请求地址
            if(clazz.isAnnotationPresent(RequestMapping.class)){
                baseUrl = clazz.getAnnotation(RequestMapping.class).value();
            }

            //获取该Controller所有public方法，遍历带有RequestMapping注解的方法
            Method[] methods = clazz.getMethods();
            for(Method method : methods){
                //如果该public方法没有RequestMapping注解则跳过
                if(!method.isAnnotationPresent(RequestMapping.class)){
                    continue;
                }
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                ZwHandlerMapping zwHandlerMapping = new ZwHandlerMapping(controller, method, pattern);
                this.handlerMappings.add(zwHandlerMapping);
                System.out.println("Mapping: " + regex + " , " + method);
            }
        }
    }

    //HandlerAdapters 用来动态匹配Method参数，包括类转换，动态赋值
    private void initHandlerAdapters(ZwClasspathXmlApplicationContext ctx) {

        for(ZwHandlerMapping handlerMapping : this.handlerMappings){
            Map paramMap = new HashMap();
            //处理命名参数，就是前端传入的参数，也就是自定义名字的
            //该循环处理的是将参数位置与其下标对应
            Annotation[][] annotations = handlerMapping.getMethod().getParameterAnnotations();
            for(int i=0;i<annotations.length;i++){
                for(Annotation a:annotations[i]){
                    if(a instanceof RequestParam){
                        String paramName = ((RequestParam) a).value();
                        paramMap.put(paramName, i);
                    }
                }
            }

            //处理非命名参数，比如HttpServletRequest、HttpResponseServlet等
            Class<?>[] clazzes = handlerMapping.getMethod().getParameterTypes();
            for(int i=0;i<clazzes.length;i++){
                Class<?> clazz = clazzes[i];
                if(clazz == HttpServletRequest.class || clazz == HttpServletResponse.class){
                    paramMap.put(clazz.getName(), i);
                }
            }
            ZwHandlerAdapter handlerAdapter = new ZwHandlerAdapter(paramMap);
            this.handlerAdapterMap.put(handlerMapping, handlerAdapter);
        }
    }

    //解决请求页面名称与模板名称关联问题
    private void initViewResolvers(ZwClasspathXmlApplicationContext ctx) {
        String templateRoot = ctx.getConfig().getProperty(DEFAULT_VIEW_TEMPLATE);
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);
        for(File templateFile : templateRootDir.listFiles()){
            this.viewResolvers.add(new ZwViewResolver(templateFile.getName(), templateFile));
        }
    }

    private void initFlashMapManager(ZwClasspathXmlApplicationContext ctx) {}
    private void initRequestToViewNameTranslator(ZwClasspathXmlApplicationContext ctx) {}
    private void initHandlerExceptionResolvers(ZwClasspathXmlApplicationContext ctx) {}
    private void initThemeResolver(ZwClasspathXmlApplicationContext ctx) {}
    private void initLocaleResolver(ZwClasspathXmlApplicationContext ctx) {}
    private void initMultipartResolver(ZwClasspathXmlApplicationContext ctx) {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("进入doPost方法");
        try{
            doDispatch(req, resp);
        }catch (Exception e){
            resp.getWriter().write("<font size='25' color='blue'>500 Exception</font><br/>Details:<br/>" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s","\r\n") +  "<font color='green'><i>Copyright@GupaoEDU</i></font>");
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //取得处理当前请求的controller,这里也称为handler处理器,
        ZwHandlerMapping handler = getHandler(req);
        // 如果handler为空,则返回404
        if (handler == null) {
            resp.getWriter().write("<font size='25' color='red'>404 Not Found</font><br/><font color='green'><i>Copyright@GupaoEDU</i></font>");
            return;
        }

        //获取request的处理器适配器handler adapter
        ZwHandlerAdapter ha = getHandlerAdapter(handler);

        //实际的处理器处理请求,返回结果视图对象
        ModelAndView mv = ha.handle(req, resp, handler);

        processDispatchResult(resp, mv);

    }

    //找到页面模板然后解析输出
    private void processDispatchResult(HttpServletResponse resp, ModelAndView mv) throws Exception {
        if(mv == null){
            return;
        }
        if(this.viewResolvers.isEmpty()){
            return;
        }
        for(ZwViewResolver viewResolver:this.viewResolvers){
            if(mv.getViewName().equals(viewResolver.getTemplateName())){
                String out = viewResolver.viewResolver(mv);
                if(out!=null){
                    resp.getWriter().write(out);
                    break;
                }
            }
        }
    }

    private ZwHandlerAdapter getHandlerAdapter(ZwHandlerMapping handler) {
        if(this.handlerAdapterMap.isEmpty()){
            return null;
        }
        return this.handlerAdapterMap.get(handler);
    }

    //根据请求的url找到对应的HandlerMapping
    private ZwHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        //遍历找到对应的请求url
        for(ZwHandlerMapping handlerMapping:this.handlerMappings){
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if(!matcher.matches()){
                continue;
            }
            return handlerMapping;
        }
        return null;
    }
}

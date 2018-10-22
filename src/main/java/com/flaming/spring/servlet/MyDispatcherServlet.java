package com.flaming.spring.servlet;

import com.flaming.spring.annotation.MyAutowired;
import com.flaming.spring.annotation.MyController;
import com.flaming.spring.annotation.MyRequestMapping;
import com.flaming.spring.annotation.MyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Author Flaming
 * @date 2018/10/22 12:15
 */
public class MyDispatcherServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(MyDispatcherServlet.class);

    private Map<String, Object> iocContainer = new HashMap<>();
    private Map<String, Method> handlerMapping = new HashMap<>();
    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        uri = uri.replace(contextPath, "").replaceAll("/+", "/");

        if(!handlerMapping.containsKey(uri)){
            resp.getWriter().write("404 Not Found");
            return;
        }

        Map<String, String[]> params = req.getParameterMap();
        Method method = handlerMapping.get(uri);
        String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());
        Object[] invokeParam = new Object[]{req, resp, params.get("name")[0]};
        method.invoke(iocContainer.get(beanName), invokeParam);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        // 1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 2.解析配置文件，并且读取信息，完成扫描
        doScanner(contextConfig.getProperty("scanPackage"));

        // 3.初始化刚刚扫描所有的类，放入IOC容器中
        doInstance();

        // 4.完成自动化注入
        doAutowired();

        // 5.初始化HandlerMapping，将URL和Method匹配
        initHandlerMapping();

        log.info("My spring framework init completed");

    }

    private void initHandlerMapping() {
        if(iocContainer.isEmpty()){
            return;
        }

        for(Map.Entry<String, Object> entry : iocContainer.entrySet()){
            Class<?> clazz = entry.getValue().getClass();

            if(!clazz.isAnnotationPresent(MyRequestMapping.class)){
                return;
            }

            String baseUrl = clazz.getAnnotation(MyRequestMapping.class).value();

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if(!method.isAnnotationPresent(MyRequestMapping.class)){
                    continue;
                }

                String methodUrl = method.getAnnotation(MyRequestMapping.class).value();
                String url = (baseUrl + "/" + methodUrl).replace("//", "/");

                handlerMapping.put(url, method);
                log.info("Mapped url : {}", url);
            }
        }

    }

    private void doAutowired() {

        if(iocContainer.isEmpty()){
            return;
        }

        for(Map.Entry<String, Object> entry : iocContainer.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                if(!field.isAnnotationPresent(MyAutowired.class)){
                    return;
                }

                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String beanName = autowired.value().trim();
                if(beanName.isEmpty()){
                    beanName = field.getType().getName();
                }

                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), iocContainer.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doInstance() {
        if(classNames.isEmpty()){
            return;
        }

        try{
            for(String className : classNames){
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(MyController.class)){
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    iocContainer.put(beanName, clazz.newInstance());
                }else if(clazz.isAnnotationPresent(MyService.class)){
                    MyService myService = clazz.getAnnotation(MyService.class);
                    String beanName = myService.value();
                    if(beanName.trim().isEmpty()){
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    if(iocContainer.containsKey(beanName)){
                        log.error("Bean {} has been defined", beanName);
                    }
                    iocContainer.put(beanName, instance);

                   Class<?>[] interfaces = clazz.getInterfaces();
                   for(Class<?> inter : interfaces){
                       iocContainer.put(inter.getName(), instance);
                   }
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private String lowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        if(null == scanPackage || scanPackage.isEmpty()){
            log.error("Can not find property 'scanPackage' ");
            return;
        }

        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        assert url != null;
        File classDir = new File(url.getFile());
        for(File file : Objects.requireNonNull(classDir.listFiles())){
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else {
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

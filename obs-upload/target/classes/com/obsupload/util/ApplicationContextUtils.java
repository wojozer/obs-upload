package com.obsupload.util;

import org.springframework.context.ApplicationContext;

/**
 * @Author: zhengwj
 * @Description:
 * @Date: 2020/4/20 22:05
 * @Version: 1.0
 */
public class ApplicationContextUtils {

    private static ApplicationContext context;

    private ApplicationContextUtils() {
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public static <T> T getBean(Class<T> t) {
        return context.getBean(t);
    }
}

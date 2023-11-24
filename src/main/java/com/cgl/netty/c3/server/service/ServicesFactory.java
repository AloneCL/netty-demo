package com.cgl.netty.c3.server.service;

import com.cgl.netty.c3.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenguanglei
 * @date 2023/4/18
 */
public class ServicesFactory {

    static Properties properties;

    static Map<Class<?>, Object> map = new ConcurrentHashMap<>();

    static {
        try (InputStream inputStream = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(inputStream);
            Set<String> names = properties.stringPropertyNames();
            for (String name : names) {
                if (name.endsWith("Service")) {
                    Class<?> interfaceClass = Class.forName(name);
                    Class<?> instanceClass = Class.forName(properties.getProperty(name));
                    map.put(interfaceClass, instanceClass.newInstance());
                }
            }
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new ExceptionInInitializerError(e);
        }

    }

    public static <T> T getInstance(Class<T> clazz) {
        return (T) map.get(clazz);
    }
}

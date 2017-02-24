package com.sai.slog.app.config;

import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 16/01/17.
 */
public class ConfigInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        String configServiceUri = System.getProperty("config.service.uri");
        String profile = System.getProperty("admin.app.profile");
        System.out.println("\n\n\n\n");
        System.out.println(" SLOG ADMIN APP ");
        System.out.println("Config URI: " + configServiceUri);
        System.out.println("App Profile: " + profile);
        System.out.println("\n\n\n\n");
        if (configServiceUri == null || profile == null) {
            throw new IllegalArgumentException("'config.service.uri' and 'admin.app.profile' must be set as a system property using -D option.");
        }
        RestTemplate rt = new RestTemplate();
        Map configs = rt.getForObject(configServiceUri.trim() + "/slog-ui/" + profile.trim(), Map.class);
        List<Map> propertySources = (List<Map>) configs.get("propertySources");
        for (Map propSource : propertySources) {
            Map<String, Object> source = (Map) propSource.get("source");
            source.entrySet().forEach(entry -> System.setProperty(entry.getKey().trim(), entry.getValue().toString().trim()));
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}

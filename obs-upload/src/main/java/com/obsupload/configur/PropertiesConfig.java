package com.obsupload.configur;

import javax.annotation.PostConstruct;

import com.obsupload.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesConfig {
    @Value("${spring.profiles.active}")
    private String active;
    private static final Logger log = LoggerFactory.getLogger(PropertiesConfig.class);

    static {
        log.info("自定义加载Properties数据...");
    }

    public PropertiesConfig() {
    }

    @PostConstruct
    public void setProperty() {
        new PropertiesUtil(String.format("application-%s.properties", this.active));
    }
}

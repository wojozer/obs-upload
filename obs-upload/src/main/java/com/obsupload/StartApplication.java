package com.obsupload;


import com.obsupload.service.HuaweiyunOBS;
import com.obsupload.util.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * 主启动类
 * 与WebApplication相同
 * @SpringBootApplication 注解的程序入口类已经包含@Configuration，不含@ServletComponentScan
 * @EnableScheduling 用于启动定时服务，必需得加
 * @author admin
 *
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class StartApplication {
	private static final Logger log = LoggerFactory.getLogger(StartApplication.class);


	public static void main(String[] args) {
		log.info("启动服务中...");
		ConfigurableApplicationContext context = SpringApplication.run(StartApplication.class, args);
		log.info("服务已启动...");

		ApplicationContextUtils.setApplicationContext(context);
		openExcute();
	}





	public static  void openExcute(){
		HuaweiyunOBS buaweiyunOBS = ApplicationContextUtils.getBean(HuaweiyunOBS.class);
		buaweiyunOBS.monitoring();
	}

}

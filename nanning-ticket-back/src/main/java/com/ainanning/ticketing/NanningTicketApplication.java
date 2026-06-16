package com.ainanning.ticketing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类
 *
 * <p>AI 南宁票务管理系统 - 后端服务入口。</p>
 *
 * @author nanning-ticket
 */
@SpringBootApplication
@MapperScan("com.ainanning.ticketing.mapper")
public class NanningTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(NanningTicketApplication.class, args);
        System.out.println("\n" +
                "========================================\n" +
                "  AI 南宁票务系统启动成功\n" +
                "  Swagger UI: http://localhost:8090/swagger-ui.html\n" +
                "========================================\n");
    }
}

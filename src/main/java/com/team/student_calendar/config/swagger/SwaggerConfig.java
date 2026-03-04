package com.team.student_calendar.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        // 로컬 서버 설정
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8081");
        localServer.setDescription("로컬 환경");

        // 운영 서버 설정
        Server prodServer = new Server();
        prodServer.setUrl("http://run-domain.com");
        prodServer.setDescription("운영 환경");

        Info info = new Info()
                .title("Swagger API")
                .version("v1.0.0")
                .description("스웨거 API");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, prodServer));
    }
}

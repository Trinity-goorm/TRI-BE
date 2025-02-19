package com.trinity.ctc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class SwaggerConfig {

    // 서버 URL 주입
    @Value("${swagger.server-url.local}")
    private String localServerUrl;

    @Value("${swagger.server-url.production}")
    private String productionServerUrl;

    @Value("${swagger.group.auth.paths}")
    private String[] authPaths;

    @Value("${swagger.group.seat.paths}")
    private String[] seatPaths;

    @Value("${swagger.group.fcm.paths}")
    private String[] fcmPaths;

    @Value("/api/reservations/**")
    private String[] reservationPaths;

    @Value("/api/notifications/**")
    private String[] notificationPaths;

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url(localServerUrl).description("로컬 서버"));
        servers.add(new Server().url(productionServerUrl).description("프로덕션 서버"));

        return new OpenAPI()
                .servers(servers)
                .info(new Info()
                        .title("캐치핑 API")
                        .description("캐치핑 프로젝트의 API 명세서")
                        .version("v1"));
    }
    /**
     * Auth API 그룹
     */
    @Bean
    public GroupedOpenApi authOpenApi() {
        return GroupedOpenApi.builder()
                .group("Auth API")
                .pathsToMatch(authPaths)
                .build();
    }

    /**
     * Seat API 그룹
     */
    @Bean
    public GroupedOpenApi seatOpenApi() {
        return GroupedOpenApi.builder()
                .group("Seat API")
                .pathsToMatch(seatPaths)
                .build();
    }

    /**
     *  Fcm API 그룹
     */
    @Bean
    public GroupedOpenApi FcmOpenApi() {
        return GroupedOpenApi.builder()
                .group("Fcm API")
                .pathsToMatch(fcmPaths)
                .build();
    }

    /**
     * Seat API 그룹
     */
    @Bean
    public GroupedOpenApi reservationOpenApi() {
        return GroupedOpenApi.builder()
                .group("Reservation API")
                .pathsToMatch(reservationPaths)
                .build();
    }

    /**
     * Notification API 그룹
     */
    @Bean
    public GroupedOpenApi notificationOpenApi() {
        return GroupedOpenApi.builder()
                .group("Notification API")
                .pathsToMatch(notificationPaths)
                .build();
    }
}


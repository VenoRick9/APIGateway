package by.baraznov.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .and()
                        .predicate(p -> !p.getRequest().getPath().equals("/auth/registration"))
                        .uri("http://localhost:8079"))

                .route("user-service",
                        r -> r.path("/users/**", "/cards/**")
                        .uri("http://localhost:8080"))

                .route("order-service", r -> r.path("/orders/**", "/items/**")
                        .uri("http://localhost:8078"))

                .build();
    }
}

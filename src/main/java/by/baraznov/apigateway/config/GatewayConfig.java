package by.baraznov.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Value("${user.service.url}")
    private String userUrl;
    @Value("${auth.service.url}")
    private String authUrl;
    @Value("${order.service.url}")
    private String orderUrl;
    @Value("${payment.service.url}")
    private String paymentUrl;


    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .and()
                        .predicate(p -> !p.getRequest().getPath().equals("/auth/registration"))
                        .uri(authUrl))

                .route("user-service",
                        r -> r.path("/users/**", "/cards/**")
                        .uri(userUrl))

                .route("order-service", r -> r.path("/orders/**", "/items/**")
                        .uri(orderUrl))
                .route("payment-service", r -> r.path("/payments/**")
                        .uri(paymentUrl))
                .build();
    }
}

package by.baraznov.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${user.service.url}")
    private String userUrl;
    @Value("${auth.service.url}")
    private String authUrl;

    @Bean
    public WebClient userClient() {
        return WebClient.builder()
                .baseUrl(userUrl)
                .build();
    }

    @Bean
    public WebClient authClient() {
        return WebClient.builder()
                .baseUrl(authUrl)
                .build();
    }
}

package groupId;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Special handling for SockJS and WebSocket traffic
            .route("scip-websocket", r -> r
                .path("/scip-websocket/**")
                .uri("ws://localhost:4000")) // Replace with your actual backend service URL
            .build();
    }
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
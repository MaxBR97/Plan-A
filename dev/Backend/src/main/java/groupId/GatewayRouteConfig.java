package groupId;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;

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
    public RouterFunction<ServerResponse> staticResourceRouter() {
        return RouterFunctions
            // Serve config.json from static/
            .route(RequestPredicates.GET("/config.json"), request ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ClassPathResource("static/config.json"))
            )

            // Serve exactly example.zpl from static/
            .andRoute(RequestPredicates.GET("/example.zpl"), request ->
                ServerResponse.ok()
                    .contentType(MediaType.TEXT_PLAIN) // or MediaType.APPLICATION_OCTET_STREAM if download expected
                    .bodyValue(new ClassPathResource("static/example.zpl"))
            )

            // Serve static assets like JS, CSS, etc.
            .andRoute(RequestPredicates.GET("/static/{*path}"), request ->
                ServerResponse.ok().bodyValue(new ClassPathResource("static/" + request.path().substring(1)))
            )

            // Fallback to index.html for frontend routing
            .andRoute(RequestPredicates.GET("/{path:^(?!api|auth|scip-websocket|static|config\\.json|example\\.zpl).*}"), request ->
                ServerResponse.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .bodyValue(new ClassPathResource("static/index.html"))
            );
    }


    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
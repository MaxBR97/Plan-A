package groupId;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import java.util.*;

@Component
public class JwtAuthHeaderFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal().flatMap(principal -> {
            if (principal instanceof JwtAuthenticationToken jwtAuthToken) {
                Jwt jwt = jwtAuthToken.getToken();

                String username = jwt.getClaimAsString("preferred_username");
                List<String> roles = jwt.getClaimAsStringList("realm_access.roles");

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User", username)
                    .header("X-Roles", String.join(",", roles))
                    .build();

                ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

                return chain.filter(mutatedExchange);
            }

            return chain.filter(exchange);
        });
    }
}

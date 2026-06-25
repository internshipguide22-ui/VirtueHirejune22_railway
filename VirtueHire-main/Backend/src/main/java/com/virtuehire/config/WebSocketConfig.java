package com.virtuehire.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origin-patterns:https://admin.virtuehire.in,https://*.up.railway.app,http://localhost:*}")
    private String[] allowedOriginPatterns;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws-assessment")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .withSockJS();

        registry.addEndpoint("/api/ws-assessment")
                .setAllowedOriginPatterns(allowedOriginPatterns);

        registry.addEndpoint("/ws-assessment")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .withSockJS();
        
        registry.addEndpoint("/ws-assessment")
                .setAllowedOriginPatterns(allowedOriginPatterns);
    }
}

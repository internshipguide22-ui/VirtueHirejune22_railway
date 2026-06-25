package com.virtuehire.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origin-patterns:https://admin.virtuehire.in,https://*.up.railway.app,http://localhost:*}")
    private String[] allowedOriginPatterns;

    @Value("${app.websocket.scheduler.pool-size:2}")
    private int schedulerPoolSize;

    @Value("${app.websocket.channel.pool-size:2}")
    private int channelPoolSize;

    @Bean
    public TaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, schedulerPoolSize));
        scheduler.setThreadNamePrefix("ws-scheduler-");
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[] { 30000, 30000 })
                .setTaskScheduler(webSocketTaskScheduler());
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(Math.max(1, channelPoolSize))
                .maxPoolSize(Math.max(1, channelPoolSize))
                .queueCapacity(100);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(Math.max(1, channelPoolSize))
                .maxPoolSize(Math.max(1, channelPoolSize))
                .queueCapacity(100);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws-assessment")
                .setAllowedOriginPatterns(CorsOriginPatterns.mergeWithRequiredPatterns(allowedOriginPatterns).toArray(String[]::new))
                .withSockJS()
                .setHeartbeatTime(30000)
                .setTaskScheduler(webSocketTaskScheduler());

        registry.addEndpoint("/api/ws-assessment")
                .setAllowedOriginPatterns(CorsOriginPatterns.mergeWithRequiredPatterns(allowedOriginPatterns).toArray(String[]::new));

        registry.addEndpoint("/ws-assessment")
                .setAllowedOriginPatterns(CorsOriginPatterns.mergeWithRequiredPatterns(allowedOriginPatterns).toArray(String[]::new))
                .withSockJS()
                .setHeartbeatTime(30000)
                .setTaskScheduler(webSocketTaskScheduler());
        
        registry.addEndpoint("/ws-assessment")
                .setAllowedOriginPatterns(CorsOriginPatterns.mergeWithRequiredPatterns(allowedOriginPatterns).toArray(String[]::new));
    }
}

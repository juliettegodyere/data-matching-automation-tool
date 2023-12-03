package net.queencoder.springboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ApiResponseConfig {
    @Value("${formelo.base.url}")
    private String formeloBaseUrl;
    
 
    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl(formeloBaseUrl).build();
    }
}

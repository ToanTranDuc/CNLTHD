package com.bt4.order_service.config;

import com.bt4.order_service.client.InventoryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.client.support.RestClientAdapter;

@Configuration
public class InventoryClientConfig {

    @Bean
    InventoryClient inventoryClient(
            @Value("${inventory.url}") String inventoryBaseUrl
    ) {
        RestClient restClient = RestClient.builder()
                .baseUrl(inventoryBaseUrl)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(InventoryClient.class);
    }
}


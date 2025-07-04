package com.jzo2o.foundations.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfiguration {
    @Bean
    public RestHighLevelClient client(){
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.101.68",9200,"http"))
        );
    }
}
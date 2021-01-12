package com.bookstore.configurations;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

@Configuration
public class ElasticSearchConfig {

	   @Value("${elastic.search.connector.maxConnPerRoute:5}")
	    private int maxConnPerRoute;

	    @Value("${elastic.search.connector.maxTotalConn:20}")
	    private int maxTotalConn;

	    @Value("${elastic.search.config.endpoint}")
	    private String esEndPoint;

	    @Value("${elastic.search.config.password}")
	    private String password;

	    @Value("${elastic.search.config.username}")
	    private String username;

	    @Bean
	    public JestClient getEsConnector() {

	        JestClientFactory factory = new JestClientFactory() {

	            @Override
	            protected HttpClientBuilder configureHttpClient(HttpClientBuilder builder) {
	                builder = super.configureHttpClient(builder);

	                boolean requestSentRetryEnabled = true;

	                builder.setRetryHandler(new DefaultHttpRequestRetryHandler(
	                        5,
	                        requestSentRetryEnabled));

	                return builder;
	            }
	        };

	        factory.setHttpClientConfig(new HttpClientConfig
	                .Builder(esEndPoint).defaultCredentials(username, password)
	                .multiThreaded(true)
	                .defaultMaxTotalConnectionPerRoute(maxConnPerRoute)
	                .maxTotalConnection(maxTotalConn)
	                .build());

	        JestClient client = factory.getObject();

	        return client;
	    }
	
}

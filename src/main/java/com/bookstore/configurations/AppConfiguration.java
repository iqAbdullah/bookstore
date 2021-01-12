package com.bookstore.configurations;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableScheduling
public class AppConfiguration {

    private final static Integer POOL_MAX_TOTAL = 20;
    private final static Integer CONNECTION_TIMEOUT = 2000;
    private final static Integer READ_TIMEOUT = 2000;
    private final static Integer SOCKET_TIMEOUT = 2000;

	@Bean
	public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
		PoolingHttpClientConnectionManager result = new PoolingHttpClientConnectionManager();
		result.setMaxTotal(POOL_MAX_TOTAL);
		return result;
	}

	@Bean
	public RequestConfig requestConfig() {
		RequestConfig result = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_TIMEOUT).setConnectTimeout(READ_TIMEOUT)
				.setSocketTimeout(SOCKET_TIMEOUT).build();
		return result;
	}

	@Bean
	public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager,
			RequestConfig requestConfig) {
		CloseableHttpClient result = HttpClientBuilder.create().setConnectionManager(poolingHttpClientConnectionManager)
				.setDefaultRequestConfig(requestConfig).build();
		return result;
	}

	@Bean(name = "restTemplate")
	public RestTemplate restTemplate(HttpClient httpClient) {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		return new RestTemplate(requestFactory);
	}



}

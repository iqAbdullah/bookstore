package com.bookstore.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Configuration
public class MongoConfig {

	@Value("${mongodb.uri}")
	private String mongoUri;

	@Value("${mongodb.database}")
	private String database;

	@Bean
	public MongoClient mongo() {
		if (mongoUri.contains("mongodb+srv:")) {
			MongoClientURI uri = new MongoClientURI(mongoUri);
			return new MongoClient(uri);
		}
		return new MongoClient(mongoUri);
	}

	@SuppressWarnings("deprecation")
	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongo(), database);
	}

}

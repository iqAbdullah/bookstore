package com.bookstore.entities;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Document(collection = "transactions")
@Getter
@Builder
public class Transaction {
	
	@Id
	private String id;
	@NonNull
	private String from;
	@NonNull
	private String to;
	@NonNull
	@Field(value = "transaction_type")
	private String transactionType;
	@NonNull
	@Setter
	private Map<String,Integer> stock;
	@NonNull
	@Builder.Default
	private Long time = System.currentTimeMillis();
	@NonNull
	@Field(value = "pament_mode")
	private String paymentMode;
	@NonNull
	@Builder.Default
	private Integer amount = 0;
	
}

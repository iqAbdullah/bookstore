
package com.bookstore.book.entity;

import java.util.Collections;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.bookstore.transaction.enums.TransactionType;

import lombok.*;

@Document(collection = "books")
@EqualsAndHashCode
@Builder
@Getter
public class BookInventory {

	@Id
	private String id;
	@Indexed(unique = true)
	@NonNull
	private String isbn;
	@NonNull
	private String title;
	@NonNull
	private String author;
	@NonNull
	@Builder.Default
	private Integer price = 0;

	@Field("count_present")
	@Builder.Default
	private Integer countPresent = 0;
	@Field("count_sold")
	@Builder.Default
	private Integer countSold = 0;
	@NonNull
	@Field("transaction_history")
	private List<String> transactionHistroy;
	@Field("last_transaction")
	private String lastTransaction;
	@Field("updated_at")
	@Builder.Default
	@NonNull
	private Long updatedAt = System.currentTimeMillis();;

	public static class BookInventoryBuilder {
		public BookInventoryBuilder lastTransaction(final String transactionId) {
			this.lastTransaction = transactionId;
			this.transactionHistroy = Collections.singletonList(transactionId);
			return this;
		}
	}

	public void addTransaction(final Integer difference, final String transactionId, final String transactionType) {
		if (TransactionType.PURCHASE.name().equals(transactionType)) {
			this.countPresent += difference;
		} else {
			this.countSold += difference;
			this.countPresent -= difference;
		}
		this.lastTransaction = transactionId;
		this.transactionHistroy.add(0,transactionId);
		this.updatedAt = System.currentTimeMillis();
	}
}

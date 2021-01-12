package com.bookstore.book.model;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Book {
	
	@NotNull
	private String ISBN;
	@NotNull
	private String title;
	@NotNull
	private String author;
	@NotNull
	private Integer price;
	
}

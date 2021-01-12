package com.bookstore.book.utils;

import com.bookstore.book.entity.BookInventory;
import com.bookstore.book.model.Book;

public class ConversionUtil {

	public static Book convert(final BookInventory bookInventory) {
		return Book.builder().author(bookInventory.getAuthor()).title(bookInventory.getTitle())
				.ISBN(bookInventory.getIsbn()).price(bookInventory.getPrice()).build();
	}

}

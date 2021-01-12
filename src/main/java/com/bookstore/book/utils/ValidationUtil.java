package com.bookstore.book.utils;

import org.springframework.util.StringUtils;

import com.bookstore.book.model.Book;
import com.bookstore.constants.Messages;
import com.google.common.base.Preconditions;

public class ValidationUtil {

	public static boolean validateBook(final Book book) {
		Preconditions.checkNotNull(book.getISBN(), Messages.ISBN_NULL);
		Preconditions.checkNotNull(book.getAuthor(), Messages.AUTHOR_NULL);
		Preconditions.checkNotNull(book.getPrice(), Messages.PRICE_NULL);
		Preconditions.checkNotNull(book.getTitle(), Messages.TITLE_NULL);
		return true;
	}

	public static boolean validateSearchParams(final String isbn, final String title, final String author) {
		if (StringUtils.isEmpty(isbn) && StringUtils.isEmpty(title) && StringUtils.isEmpty(author))
			return false;
		return true;
	}

}

package com.bookstore.book.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.bookstore.book.entity.BookInventory;
import com.bookstore.book.exception.BookDoesNotExist;
import com.bookstore.book.exception.InsufficentData;
import com.bookstore.book.model.Book;
import com.bookstore.entities.Transaction;
import com.bookstore.media.model.MediaPost;

import lombok.NonNull;

@Component("bookService")
public interface BookService {

	public Boolean addBook(@NonNull final String ISBN, @NonNull final String title, @NonNull final String author,
			@NonNull final Integer price);

	public List<Book> findBookByParams(final String isbn, final String title, final String author, final int size,
			final int limit) throws InsufficentData;

	public List<MediaPost> findMedia(final String isbn) throws BookDoesNotExist;

	public Boolean updateTransaction(@NonNull final Transaction transaction);

	public BookInventory findByISBN(@NonNull String key);

	Boolean bookExists(@NonNull String isbn);

}

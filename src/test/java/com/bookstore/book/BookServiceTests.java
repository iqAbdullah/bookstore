package com.bookstore.book;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bookstore.book.Repo.BookInventoryRepo;
import com.bookstore.book.entity.BookInventory;
import com.bookstore.book.exception.InsufficentData;
import com.bookstore.book.model.Book;
import com.bookstore.book.services.BookService;

@SpringBootTest
public class BookServiceTests {

	@Autowired
	BookService bookService;
	@Autowired
	BookInventoryRepo bookInventoryRepo;

	private final String author = "firstname lastname";
	private final String title = "title1";
	private final Integer price = 100;
	private final String ISBN = "isbn1";
	
	@Test
	public void addBookSuccess() {
		bookService.addBook(ISBN, title, author, price);
		Optional<BookInventory> bookInventoryEntity = bookInventoryRepo.findByIsbn(ISBN);

		assertTrue(bookInventoryEntity.isPresent());

		BookInventory bookInventory = bookInventoryEntity.get();

		assertAll(() -> assertEquals(bookInventory.getIsbn(), ISBN),
				() -> assertEquals(bookInventory.getAuthor(), author),
				() -> assertEquals(bookInventory.getTitle(), title), () -> assertEquals(bookInventory.getPrice(), price),
				() -> assertEquals(bookInventory.getCountPresent(), 0),
				() -> assertEquals(bookInventory.getCountSold(), 0), () -> assertNotNull(bookInventory.getUpdatedAt()),
				() -> assertNull(bookInventory.getLastTransaction()),
				() -> assertTrue(bookInventory.getTransactionHistroy().isEmpty()));

		bookInventoryRepo.delete(bookInventory);

	}

	@Test
	public void addBookFailure() {

		Throwable ISBNEx = assertThrows(NullPointerException.class,
				() -> bookService.addBook(null, title, author, price));
		Throwable authorEX = assertThrows(NullPointerException.class,
				() -> bookService.addBook(ISBN, title, null, price));
		Throwable titleEX = assertThrows(NullPointerException.class,
				() -> bookService.addBook(ISBN, null, author, price));
		Throwable priceEX = assertThrows(NullPointerException.class,
				() -> bookService.addBook(ISBN, title, author, null));

		assertAll(() -> assertEquals("ISBN is marked non-null but is null", ISBNEx.getMessage()),
				() -> assertEquals("author is marked non-null but is null", authorEX.getMessage()),
				() -> assertEquals("title is marked non-null but is null", titleEX.getMessage()),
				() -> assertEquals("price is marked non-null but is null", priceEX.getMessage()));

	}

	@Test
	public void searchBookISBNSuccess() throws InsufficentData {
		bookService.addBook(ISBN, title, author, price);

		List<Book> bookList = bookService.findBookByParams(ISBN, null, null, 10, 0);

		Book book = Book.builder().author(author).ISBN(ISBN).title(title).price(price).build();
		
		assertAll(() -> assertEquals(bookList.size(), 1), () -> assertEquals(bookList.get(0),book));
		
		bookInventoryRepo.deleteByIsbn(ISBN);
	}

	@Test
	public void searchBookTitleSuccess() throws InsufficentData {
		bookService.addBook(ISBN, title, author, price);

		List<Book> bookList = bookService.findBookByParams(null, title, null, 10, 0);

		Book book = Book.builder().author(author).ISBN(ISBN).title(title).price(price).build();

		assertAll(() -> assertEquals(bookList.size(), 1), () -> assertEquals(bookList.get(0), book));

		bookInventoryRepo.deleteByIsbn(ISBN);
	}

	@Test
	public void searchBookAuthorSuccess() throws InsufficentData {
		bookService.addBook(ISBN, title, author, price);

		List<Book> bookList = bookService.findBookByParams(null, null, author, 10, 0);

		Book book = Book.builder().author(author).ISBN(ISBN).title(title).price(price).build();

		assertAll(() -> assertEquals(bookList.size(), 1), () -> assertEquals(bookList.get(0), book));

		bookInventoryRepo.deleteByIsbn(ISBN);
	}

	@Test
	public void searchBookNullFailure() throws InsufficentData {
		bookService.addBook(ISBN, title, author, price);

		assertThrows(InsufficentData.class,
				() -> bookService.findBookByParams(null, null, null, 10, 0));

		bookInventoryRepo.deleteByIsbn(ISBN);
	}

}

package com.bookstore.transaction;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bookstore.book.Repo.BookInventoryRepo;
import com.bookstore.book.entity.BookInventory;
import com.bookstore.book.exception.BookDoesNotExist;
import com.bookstore.book.exception.OutOfStock;
import com.bookstore.book.exception.TransactionFailure;
import com.bookstore.book.services.BookService;
import com.bookstore.entities.Transaction;
import com.bookstore.transaction.enums.TransactionType;
import com.bookstore.transaction.repo.TransactionRepo;
import com.bookstore.transaction.service.TransactionService;

@SpringBootTest
public class TransactionsTest {
	
	private final String supplyer = "Supplyer";
	private final String bookstore = "Bookstore";
	private final String consumer = "Consumer";
	
	private final String author = "firstname lastname";
	private final String title = "title1";
	private final Integer price1 = 100;
	private final Integer price2 = 50;
	private final String ISBN1 = "isbn1";
	private final String ISBN2 = "isbn2";
	private final Integer b1quantity_p = 2;
	private final Integer b2quantity_p = 2;
	
	private final Integer b1quantity_s = 1;
	private final Integer b2quantity_s = 2;
	
	private final String paymentMode = "credit";
	
	@Autowired TransactionService transactionService;
	@Autowired TransactionRepo transactionRepo;
	@Autowired BookService bookService;
	@Autowired BookInventoryRepo bookInventoryRepo;
	
	
	/**
	 *	Onboarding 2 books 
	 */
	private void prepare() {
		bookService.addBook(ISBN1, title, author, price1);
		bookService.addBook(ISBN2, title, author, price2);
	}
	
	/**
	 *	preparing order for adding to stock 
	 */
	private Map<String,Integer> stockOrder(){
		Map<String,Integer> stock = new HashMap<String,Integer>();
		stock.put(ISBN1, b1quantity_p);
		stock.put(ISBN2, b2quantity_p);
		return stock;
	}
	
	/**
	 * ISBN3 is not onboarded, So it should throw BookDoesNotExist exception
	 */
	private Map<String,Integer> stockOrderFailure(){
		Map<String,Integer> stock = new HashMap<String,Integer>();
		stock.put(ISBN1, b1quantity_p);
		stock.put("ISBN3", b2quantity_p);
		return stock;
	}
	
	/**
	 * order for selling on of each 2 books
	 */
	private Map<String,Integer> sellOrder(){
		Map<String,Integer> stock = new HashMap<String,Integer>();
		stock.put(ISBN1, b1quantity_s);
		stock.put(ISBN2, b2quantity_s);
		return stock;
	}
	
	/**
	 * order where book2 is out of stock after selling once
	 */
	private Map<String,Integer> outOfStock(){
		Map<String,Integer> stock = new HashMap<String,Integer>();
		stock.put(ISBN1, b1quantity_s);
		stock.put(ISBN2, b2quantity_s);
		return stock;
	}
	
	/**
	 * deleting Transaction entity by id
	 */
	private void clearT(final String transactionId) {
		transactionRepo.deleteById(transactionId);
	}
	
	/**
	 * clearing books records created for testing purpose
	 */
	private void clearB() {
		bookInventoryRepo.deleteByIsbn(ISBN1);
		bookInventoryRepo.deleteByIsbn(ISBN2);
	}
	
	/**
	 * adding books to stock and then asserting expected changes in the books and transaction records
	 */
	@Test
	public void perchasingBookSuccess() throws BookDoesNotExist, TransactionFailure {
		prepare();
		String transactionId = transactionService.purchase(stockOrder(), supplyer, paymentMode);
		Optional<Transaction> transactionOptional = transactionRepo.findById(transactionId);
		
		assertTrue(transactionOptional.isPresent());
		
		Transaction transaction = transactionOptional.get();
		
		// checking transaction record
		assertAll(
				()->assertEquals(transactionId, transaction.getId()),
				()->assertEquals(supplyer, transaction.getFrom()),
				()->assertEquals(bookstore, transaction.getTo()),
				()->assertEquals(paymentMode, transaction.getPaymentMode()),
				()->assertEquals(price1*b1quantity_p+price2*b2quantity_p, transaction.getAmount()),
				()->assertEquals(TransactionType.PURCHASE.name(), transaction.getTransactionType()),
				()->assertEquals(b1quantity_p, transaction.getStock().get(ISBN1)),
				()->assertEquals(b2quantity_p, transaction.getStock().get(ISBN2)),
				()->assertNotNull(transaction.getTime())
				);

		// checking book records
		BookInventory book1 = bookInventoryRepo.findByIsbn(ISBN1).get();
		BookInventory book2 = bookInventoryRepo.findByIsbn(ISBN2).get();
		
		//checking changes in Transaction history and quantities
		assertAll(
				()->assertEquals(transactionId, book1.getLastTransaction()),
				()->assertEquals(transactionId, book2.getLastTransaction()),
				()->assertEquals(transactionId, book1.getTransactionHistroy().get(0)),
				()->assertEquals(transactionId, book2.getTransactionHistroy().get(0)),
				()->assertEquals(b1quantity_p, book1.getCountPresent()),
				()->assertEquals(b2quantity_p, book2.getCountPresent()),
				()->assertEquals(0, book1.getCountSold()),
				()->assertEquals(0, book2.getCountSold())
				);
		
		// clearing data
		
		clearT(transactionId);
		clearB();
	}
	
	/**
	 * checking null checks
	 * 
	 * @param
	 * @return 
	 */
	@Test
	public void perchaseNullFailure() {

		Throwable stockEx = assertThrows(NullPointerException.class,
				() -> transactionService.purchase(null, supplyer, paymentMode));
		Throwable fromEX = assertThrows(NullPointerException.class,
				() -> transactionService.purchase(stockOrder(), null, paymentMode));
		Throwable paymentModeEX = assertThrows(NullPointerException.class,
				() -> transactionService.purchase(stockOrder(), supplyer, null));

		assertAll(() -> assertEquals("stock is marked non-null but is null", stockEx.getMessage()),
				() -> assertEquals("from is marked non-null but is null", fromEX.getMessage()),
				() -> assertEquals("paymentMode is marked non-null but is null", paymentModeEX.getMessage()));
	}
	
	/**
	 * checking for BookDoesNotExist exception
	 */
	@Test
	public void perchaseBookDoesNotexist() {
		assertThrows(BookDoesNotExist.class, ()->transactionService.purchase(stockOrder(), supplyer, paymentMode));
	}
	
	/**
	 * Selling books form the stock	
	 * 
	 * @param
	 * @return 
	 */
	@Test
	public void sellingBookSuccess() throws BookDoesNotExist, TransactionFailure, OutOfStock {
		prepare();
		String perchaseId = transactionService.purchase(stockOrder(), supplyer, paymentMode);
		String sellId = transactionService.sell(sellOrder(), consumer, paymentMode);
		
		// searching transaction
		Optional<Transaction> transactionOptional = transactionRepo.findById(sellId);

		assertTrue(transactionOptional.isPresent());
		
		Transaction transaction = transactionOptional.get();
		
		//checking sell transaction
		assertAll(
				()->assertEquals(sellId, transaction.getId()),
				()->assertEquals(bookstore, transaction.getFrom()),
				()->assertEquals(consumer, transaction.getTo()),
				()->assertEquals(paymentMode, transaction.getPaymentMode()),
				()->assertEquals(price1*b1quantity_s+price2*b2quantity_s, transaction.getAmount()),
				()->assertEquals(TransactionType.SELLING.name(), transaction.getTransactionType()),
				()->assertEquals(b1quantity_s, transaction.getStock().get(ISBN1)),
				()->assertEquals(b2quantity_s, transaction.getStock().get(ISBN2)),
				()->assertNotNull(transaction.getTime())
				);
		//getting book records
		BookInventory book1 = bookInventoryRepo.findByIsbn(ISBN1).get();
		BookInventory book2 = bookInventoryRepo.findByIsbn(ISBN2).get();
		
		//accessing changes in the transaction history and quantity after sale
		assertAll(
				()->assertEquals(sellId, book1.getLastTransaction()),
				()->assertEquals(sellId, book2.getLastTransaction()),
				()->assertEquals(sellId, book1.getTransactionHistroy().get(0)),
				()->assertEquals(sellId, book2.getTransactionHistroy().get(0)),
				()->assertEquals(b1quantity_p-b1quantity_s, book1.getCountPresent()),
				()->assertEquals(b2quantity_p-b2quantity_s, book2.getCountPresent()),
				()->assertEquals(b1quantity_s, book1.getCountSold()),
				()->assertEquals(b2quantity_s, book2.getCountSold())
				);				
		
		clearT(perchaseId);
		clearT(sellId);
		clearB();
	}
	
	/**
	 * null params check
	 * 
	 * @param
	 * @return 
	 */
	@Test
	public void sellNullFailure() {

		Throwable stockEx = assertThrows(NullPointerException.class,
				() -> transactionService.sell(null, consumer, paymentMode));
		Throwable toEX = assertThrows(NullPointerException.class,
				() -> transactionService.sell(stockOrder(), null, paymentMode));
		Throwable paymentModeEX = assertThrows(NullPointerException.class,
				() -> transactionService.sell(stockOrder(), consumer, null));

		assertAll(() -> assertEquals("stock is marked non-null but is null", stockEx.getMessage()),
				() -> assertEquals("to is marked non-null but is null", toEX.getMessage()),
				() -> assertEquals("paymentMode is marked non-null but is null", paymentModeEX.getMessage()));
	}
	
	/**
	 * checking OutOfStock Exception
	 * 
	 * @param
	 * @return 
	 */
	@Test
	public void sellOutofstock() throws BookDoesNotExist, TransactionFailure, OutOfStock {
		prepare();
		String perchaseId = transactionService.purchase(stockOrder(), supplyer, paymentMode);
		String sellId = transactionService.sell(sellOrder(), consumer, paymentMode);
		
		assertThrows(OutOfStock.class, ()->transactionService.sell(outOfStock(), consumer, paymentMode));
		clearT(perchaseId);
		clearT(sellId);
		clearB();
	}
	
	/**
	 * checking bookDoesNotExist
	 * 
	 * @param
	 * @return 
	 */
	@Test
	public void sellBookDoesNotExists() throws BookDoesNotExist, TransactionFailure, OutOfStock {
		prepare();
		String perchaseId = transactionService.purchase(stockOrder(), supplyer, paymentMode);
		
		 assertThrows(BookDoesNotExist.class, ()->transactionService.sell(stockOrderFailure(), consumer, paymentMode));
		clearT(perchaseId);
		clearB();
	}
}

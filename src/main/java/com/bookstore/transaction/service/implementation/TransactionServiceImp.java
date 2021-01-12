package com.bookstore.transaction.service.implementation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bookstore.book.entity.BookInventory;
import com.bookstore.book.exception.BookDoesNotExist;
import com.bookstore.book.exception.OutOfStock;
import com.bookstore.book.exception.TransactionFailure;
import com.bookstore.book.services.BookService;
import com.bookstore.constants.Constants;
import com.bookstore.entities.Transaction;
import com.bookstore.transaction.enums.Errors;
import com.bookstore.transaction.enums.TransactionType;
import com.bookstore.transaction.repo.TransactionRepo;
import com.bookstore.transaction.service.TransactionService;

import lombok.NonNull;

@Component
public class TransactionServiceImp implements TransactionService{

	private static final String STORE_ID = "Bookstore";

	@Autowired
	private TransactionRepo transactionRepo;
	@Autowired
	private BookService bookService;

	private Integer entryCost(Entry<String, Integer> entry) {
		final BookInventory bookInventory = bookService.findByISBN(entry.getKey());
		final Integer price = (bookInventory != null) ? bookInventory.getPrice() : 0;
		return Math.multiplyExact(price, entry.getValue());
	}

	private Integer tallyAmount(final Map<String, Integer> stock) {
		return stock.entrySet().stream().map(this::entryCost).reduce(0, Math::addExact);
	}

	private Boolean bookNotExists(Entry<String, Integer> entry) {
		return !bookService.bookExists(entry.getKey());
	}

	private Boolean checkTheQuantity(Entry<String, Integer> entry) {
		BookInventory bookInventory = bookService.findByISBN(entry.getKey());
		return !(bookInventory != null && bookInventory.getCountPresent() >= entry.getValue());
	}

	private Entry<String, Integer> provideActualAvailable(Entry<String, Integer> entry) {
		BookInventory bookInventory = bookService.findByISBN(entry.getKey());
		if (bookInventory != null) {
			entry.setValue((bookInventory.getCountPresent() >= entry.getValue()) ? entry.getValue()
					: bookInventory.getCountPresent());
		}
		return entry;
	}

	@Override
	public String purchase(@NonNull final Map<String, Integer> stock,@NonNull final String from,@NonNull final String paymentMode)
			throws BookDoesNotExist, TransactionFailure {

		final Map<String, Integer> booksdoesNotExist = stock.entrySet().stream().filter(this::bookNotExists)
				.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

		if (!booksdoesNotExist.isEmpty()) {
			throw new BookDoesNotExist(Errors.BOOK_NOT_PRESENT.getCode(),
					Errors.BOOK_NOT_PRESENT.getMessage().replace(Constants.REPLACE_TAG, booksdoesNotExist.keySet().toString()));
		}

		Transaction transaction = Transaction.builder().from(from).to(STORE_ID).stock(stock).amount(tallyAmount(stock))
				.transactionType(TransactionType.PURCHASE.name()).paymentMode(paymentMode).build();
		transaction = transactionRepo.save(transaction);
		if (!bookService.updateTransaction(transaction)) {
			transactionRepo.delete(transaction);
			throw new TransactionFailure();
		}
		return transaction.getId();
	}

	@Override
	public String sell(@NonNull final Map<String, Integer> stock,@NonNull final String to,@NonNull final String paymentMode)
			throws BookDoesNotExist, OutOfStock, TransactionFailure {
		final Map<String, Integer> booksdoesNotExist = stock.entrySet().stream().filter(this::bookNotExists)
				.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

		if (!booksdoesNotExist.isEmpty()) {
			throw new BookDoesNotExist(Errors.BOOK_NOT_PRESENT.getCode(), Errors.BOOK_NOT_PRESENT.getMessage()
					.replace(Constants.REPLACE_TAG, booksdoesNotExist.keySet().toString()));
		}

		final Map<String, Integer> booksNotAvailable = stock.entrySet().stream().filter(this::checkTheQuantity)
				.map(this::provideActualAvailable)
				.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
		if (!booksNotAvailable.isEmpty()) {
			throw new OutOfStock(Errors.BOOK_OUT_OF_STOCK.getCode(),
					Errors.BOOK_OUT_OF_STOCK.getMessage().replace(Constants.REPLACE_TAG, booksNotAvailable.toString()));
		}
		Transaction transaction = Transaction.builder().from(STORE_ID).to(to).stock(stock).amount(tallyAmount(stock))
				.transactionType(TransactionType.SELLING.name()).paymentMode(paymentMode).build();
		transaction = transactionRepo.save(transaction);
		if (!bookService.updateTransaction(transaction)) {
			transactionRepo.delete(transaction);
			throw new TransactionFailure();
		}
		return transaction.getId();
	}
}

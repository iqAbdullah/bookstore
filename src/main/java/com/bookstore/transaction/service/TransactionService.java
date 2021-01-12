package com.bookstore.transaction.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.bookstore.book.exception.BookDoesNotExist;
import com.bookstore.book.exception.OutOfStock;
import com.bookstore.book.exception.TransactionFailure;

import lombok.NonNull;

@Component("transactionService")
public interface TransactionService {

	public String purchase(@NonNull final Map<String, Integer> stock,@NonNull final String from,@NonNull final String paymentMode)
			throws BookDoesNotExist, TransactionFailure;

	public String sell(@NonNull final Map<String, Integer> stock,@NonNull final String to,@NonNull final String paymentMode)
			throws BookDoesNotExist, OutOfStock, TransactionFailure;

}

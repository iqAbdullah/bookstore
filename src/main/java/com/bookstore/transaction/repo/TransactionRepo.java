package com.bookstore.transaction.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bookstore.entities.Transaction;

public interface TransactionRepo extends MongoRepository<Transaction, String>{

}

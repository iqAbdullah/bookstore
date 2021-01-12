package com.bookstore.book.Repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import com.bookstore.book.entity.BookInventory;

@Component
public interface BookInventoryRepo extends MongoRepository<BookInventory, String>{
	
	public Optional<BookInventory> findByIsbn(final String isbn); 
	
	public void deleteByIsbn(final String isbn);

}

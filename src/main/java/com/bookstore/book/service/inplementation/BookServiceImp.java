package com.bookstore.book.service.inplementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.bookstore.book.Repo.BookInventoryRepo;
import com.bookstore.book.entity.BookInventory;
import com.bookstore.book.exception.BookDoesNotExist;
import com.bookstore.book.exception.InsufficentData;
import com.bookstore.book.model.Book;
import com.bookstore.book.services.BookService;
import com.bookstore.book.utils.ConversionUtil;
import com.bookstore.book.utils.ValidationUtil;
import com.bookstore.entities.Transaction;
import com.bookstore.es.client.ElasticSearchClient;
import com.bookstore.media.model.MediaPost;
import com.google.gson.Gson;

import lombok.NonNull;

@Component
public class BookServiceImp implements BookService {

	private static final Logger logger = Logger.getLogger(BookServiceImp.class.getName());

	@Autowired
	private BookInventoryRepo bookInventoryRepo;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private ElasticSearchClient elasticSearchClient;

	private Gson gson = new Gson();

	@Override
	public Boolean updateTransaction(@NonNull final Transaction transaction) {
		return transaction.getStock().entrySet().stream().map((entry) -> {
			return updateStockEnrty(entry.getKey(), transaction);
		}).reduce(true, Boolean::logicalAnd);
	}

	private Boolean updateStockEnrty(@NonNull final String isbn,@NonNull final Transaction transaction) {
		final BookInventory bookInventory = bookInventoryRepo.findByIsbn(isbn).orElseGet(null);
		if (bookInventory == null) {
			return false;
		}
		bookInventory.addTransaction(transaction.getStock().get(isbn), transaction.getId(),
				transaction.getTransactionType());
		bookInventoryRepo.save(bookInventory);
		return true;
	}

	@Override
	public Boolean bookExists(@NonNull final String isbn) {
		return bookInventoryRepo.findByIsbn(isbn).isPresent();
	}

	@Override
	public BookInventory findByISBN(@NonNull final String isbn) {
		return bookInventoryRepo.findByIsbn(isbn).orElseGet(null);
	}

	@Override
	public Boolean addBook(@NonNull final String ISBN,@NonNull final String title,@NonNull final String author,@NonNull final Integer price) {
		BookInventory book = BookInventory.builder().isbn(ISBN).author(author).title(title).price(price)
				.transactionHistroy(new ArrayList<String>()).build();
		bookInventoryRepo.save(book);
		return true;
	}

	@Override
	public List<Book> findBookByParams(final String isbn, final String title, final String author, final int size,
			final int limit) throws InsufficentData {
		if(!ValidationUtil.validateSearchParams(isbn, title, author)) {throw new InsufficentData();}
		Query query = new Query();
		Pageable pageable = PageRequest.of(limit, size);
		Criteria criteria = new Criteria();
		List<Criteria> clist = new ArrayList<Criteria>();
		if (!StringUtils.isEmpty(isbn))
			clist.add(Criteria.where("isbn").is(isbn));
		if (!StringUtils.isEmpty(author))
			clist.add(Criteria.where("author").regex(author, "i"));
		if (!StringUtils.isEmpty(title))
			clist.add(Criteria.where("title").regex(title, "i"));
		criteria.orOperator(clist.toArray(new Criteria[(clist.size())]));
		query.addCriteria(criteria).with(pageable);
		List<BookInventory> result = mongoTemplate.find(query, BookInventory.class);
		return result.stream().map(ConversionUtil::convert).collect(Collectors.toList());
	}

	@Override
	public List<MediaPost> findMedia(final String isbn) throws BookDoesNotExist {
		BookInventory bookInventory = bookInventoryRepo.findByIsbn(isbn).orElseThrow(BookDoesNotExist::new);
		List<MediaPost> resultList = new ArrayList<MediaPost>();
		try {
			final List<String> searchResultList = elasticSearchClient.orSearchQuery(bookInventory.getTitle());
			resultList = searchResultList.stream().map(this::fromString).collect(Collectors.toList());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error in fetching posts from ElasticSearch", e);
		}
		return resultList;
	}

	private MediaPost fromString(final String json) {
		return gson.fromJson(json, MediaPost.class);
	}

}

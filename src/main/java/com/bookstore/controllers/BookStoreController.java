package com.bookstore.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.book.exception.BookDoesNotExist;
import com.bookstore.book.exception.InsufficentData;
import com.bookstore.book.exception.OutOfStock;
import com.bookstore.book.model.Book;
import com.bookstore.book.services.BookService;
import com.bookstore.book.utils.ValidationUtil;
import com.bookstore.constants.ApiConstants;
import com.bookstore.constants.Messages;
import com.bookstore.media.model.MediaPost;
import com.bookstore.models.ServerResponse;
import com.bookstore.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RestController
@RequestMapping(ApiConstants.V1 + ApiConstants.BOOK)
public class BookStoreController {

	private static final Logger logger = Logger.getLogger(BookStoreController.class.getName());

	@Autowired
	private BookService bookService;
	@Autowired
	private TransactionService transactionService;

	private Gson gson = new Gson();
	private ObjectMapper objectMapper = new ObjectMapper();

	@PostMapping(ApiConstants.ADD)
	public ServerResponse<JsonNode> addBook(@RequestBody String request) {

		Book book = null;

		try {
			book = gson.fromJson(request, Book.class);
			ValidationUtil.validateBook(book);
		} catch (NullPointerException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getServiceFailedResponseWithException("", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getDefaultServiceFailedResponse(Messages.EXCEPTION_OCCURS);
		}
		bookService.addBook(book.getISBN(), book.getTitle(), book.getAuthor(), book.getPrice());
		return ServerResponse.getDefaultServiceOkResponse();
	}

	@PutMapping(ApiConstants.PURCHASE)
	public ServerResponse<JsonNode> purchaseBooks(@RequestBody String request,
			@RequestParam(required = true) String from, @RequestParam(required = true) String paymentMode) {

		Map<String, Integer> stock = new HashMap<String, Integer>();

		try {

			stock = gson.fromJson(request, new TypeToken<Map<String, Integer>>() {
			}.getType());
			transactionService.purchase(stock, from, paymentMode);
		} catch (NullPointerException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getServiceFailedResponseWithException("", e);
		} catch (BookDoesNotExist e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getServiceFailedResponseWithException("", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getDefaultServiceFailedResponse(Messages.EXCEPTION_OCCURS);
		}
		return ServerResponse.getDefaultServiceOkResponse();
	}

	@GetMapping(ApiConstants.SEARCH)
	public ServerResponse<JsonNode> searchBooks(@RequestParam(required = false) String isbn,
			@RequestParam(required = false) String title, @RequestParam(required = false) String author,
			@RequestParam(required = false, defaultValue = "10") Integer size,
			@RequestParam(required = false, defaultValue = "0") Integer limit) {
		List<Book> result = new ArrayList<Book>();
		try {
			result = bookService.findBookByParams(isbn, title, author, size, limit);
		} catch (InsufficentData e) {
			logger.log(Level.SEVERE, Messages.SEARCH_PARAMS_NULL);
			return ServerResponse.getDefaultServiceFailedResponse(Messages.SEARCH_PARAMS_NULL);
		}catch (NullPointerException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getServiceFailedResponseWithException("", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getDefaultServiceFailedResponse(Messages.EXCEPTION_OCCURS);
		}
		return new ServerResponse<JsonNode>(objectMapper.valueToTree(result));
	}

	@PutMapping(ApiConstants.SELL)
	public ServerResponse<JsonNode> sellBooks(@RequestBody String request, @RequestParam(required = true) String to,
			@RequestParam(required = true) String paymentMode) {

		Map<String, Integer> stock = new HashMap<String, Integer>();

		try {

			stock = gson.fromJson(request, new TypeToken<Map<String, Integer>>() {
			}.getType());
			transactionService.sell(stock, to, paymentMode);
		} catch (NullPointerException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getServiceFailedResponseWithException("", e);
		} catch (BookDoesNotExist e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getServiceFailedResponseWithException("", e);
		} catch (OutOfStock e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getServiceFailedResponseWithException("", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getDefaultServiceFailedResponse(Messages.EXCEPTION_OCCURS);
		}
		return ServerResponse.getDefaultServiceOkResponse();
	}

	@GetMapping(ApiConstants.MEDIA)
	public ServerResponse<JsonNode> searchMedia(@RequestParam(required = true) String isbn) {

		List<MediaPost> result = null;
		try {
			result = bookService.findMedia(isbn);
		} catch (BookDoesNotExist e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getServiceFailedResponseWithException("", e);
		} catch (NullPointerException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getServiceFailedResponseWithException("", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return ServerResponse.getDefaultServiceFailedResponse(Messages.EXCEPTION_OCCURS);
		}
		return new ServerResponse<JsonNode>(objectMapper.valueToTree(result));
	}

}

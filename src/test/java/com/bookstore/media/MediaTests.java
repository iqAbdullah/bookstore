package com.bookstore.media;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bookstore.book.Repo.BookInventoryRepo;
import com.bookstore.book.exception.BookDoesNotExist;
import com.bookstore.book.services.BookService;
import com.bookstore.es.client.ElasticSearchClient;
import com.bookstore.media.entiy.MediaPostEntity;
import com.bookstore.media.model.MediaPost;
import com.bookstore.media.repo.MediaPostsRepo;
import com.bookstore.media.services.MediaService;
import com.bookstore.media.utils.ConversionUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@SpringBootTest
public class MediaTests {

	private final String author = "firstname lastname";
	private final String title = "520941e8791011eabc550242ac130003";
	private final Integer price = 100;
	private final String ISBN = "isbn1";
	private final String test1 = "test1";
	private final String test2 = "test2";
	private final String title1 = "Elasticsearch test 520941e8791011eabc550242ac130003 ";
	private final String title2 = "Prueba de Elasticsearch";
	private final String body1 = "Hello, This is elastic Search testing. goodbye.";
	private final String body2 = "Hola, esta es una prueba de Elasticsearch. adiós. 520941e8791011eabc550242ac130003 ";

	private Gson gson = new Gson();

	@Autowired
	private ElasticSearchClient elasticSearchClient;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private MediaPostsRepo mediaPostsRepo;
	@Autowired
	private BookService bookService;
	@Autowired
	private BookInventoryRepo bookInventoryRepo;

	private List<MediaPost> prepareList() {
		final MediaPost post1 = MediaPost.builder().id(test1).userId(test1).title(title1).body(body1).build();
		final MediaPost post2 = MediaPost.builder().id(test2).userId(test2).title(title2).body(body2).build();
		List<MediaPost> mediaPostList = new ArrayList<MediaPost>();
		mediaPostList.add(post1);
		mediaPostList.add(post2);
		return mediaPostList;
	}

	private void clearPosts() throws IOException {
		mediaPostsRepo.deleteById(test1);
		mediaPostsRepo.deleteById(test2);
		elasticSearchClient.delete(test1);
		elasticSearchClient.delete(test2);
	}

	@Test
	public void insertMediaPosts() throws JsonSyntaxException, IOException {
		MediaPost post1C1 = prepareList().get(0);

		mediaService.handleMediaPost(prepareList());

		final Optional<MediaPostEntity> post1Oprional = mediaPostsRepo.findById(test1);

		assertTrue(post1Oprional.isPresent());

		MediaPost post1C2 = ConversionUtil.convert(post1Oprional.get());

		MediaPost post1C3 = gson.fromJson(elasticSearchClient.get(test1), MediaPost.class);

		clearPosts();

		assertTrue(post1C1.compare(post1C2));
		assertTrue(post1C1.compare(post1C3));
	}

	@Test
	public void searchMediaPostsSuccess() throws JsonSyntaxException, IOException, BookDoesNotExist {

		bookService.addBook(ISBN, title, author, price);
		mediaService.handleMediaPost(prepareList());

		List<MediaPost> searchresult = bookService.findMedia(ISBN);

		clearPosts();
		bookInventoryRepo.deleteByIsbn(ISBN);

		assertTrue(searchresult.size() >= 2);

		String searchString = gson.toJson(searchresult);

		assertTrue(searchString.contains(title));

	}

	@Test
	public void searchMediaPostsBookDoesNotExistFails() throws JsonSyntaxException, IOException {

		mediaService.handleMediaPost(prepareList());

		assertThrows(BookDoesNotExist.class, () -> bookService.findMedia(ISBN));

		clearPosts();

	}

}

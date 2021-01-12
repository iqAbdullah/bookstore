package com.bookstore.media.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.bookstore.es.client.ElasticSearchClient;
import com.bookstore.media.entiy.MediaPostEntity;
import com.bookstore.media.model.MediaPost;
import com.bookstore.media.repo.MediaPostsRepo;
import com.bookstore.media.utils.ConversionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class MediaService {

	private static final Logger LOGGER = Logger.getLogger(MediaService.class.getName());

	@Value("${media.source.api}")
	private String mediaUrl;

	@Value("${media.index}")
	private String esIndex;

	@Value("${media.type}")
	private String esType;

	private final long cronexpression = 1000*60*60;

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private MediaPostsRepo mediaPostRepo;
	@Autowired
	private ElasticSearchClient elasticSerchClient;

	private Gson gson = new Gson();

	private List<MediaPost> fetchMedisPosts()
			throws RestClientException, URISyntaxException, JsonMappingException, JsonProcessingException {
		final String result = restTemplate.getForObject(new URIBuilder(mediaUrl).build(), String.class);
		final List<MediaPost>  mediaPostsList= gson.fromJson(result, new TypeToken<List<MediaPost>>() {
		}.getType());
		return mediaPostsList;
	}

	private Boolean postExists(final MediaPost mediaPost) {
		return !mediaPostRepo.findById(mediaPost.getId()).isPresent();
	}

	private void saveMongo(final List<MediaPost> mediaPostList) {
		final List<MediaPostEntity> mediaPostEntityList = mediaPostList.parallelStream().map(ConversionUtil::convert)
				.collect(Collectors.toList());
		mediaPostRepo.saveAll(mediaPostEntityList);
	}
	
	public void handleMediaPost(final List<MediaPost> mediaPosts) {
		try {
			elasticSerchClient.insertBulk(mediaPosts);
			saveMongo(mediaPosts);
			LOGGER.log(Level.INFO,"Updated MediaPosts");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Unable to update data in ElasticSearch", e);
		}
	}

	@Scheduled(fixedRate = cronexpression)
	private void updateMediaPosts() {
		try {
			final List<MediaPost> mediaPosts = fetchMedisPosts().stream().filter(this::postExists)
					.collect(Collectors.toList());
			if (mediaPosts.isEmpty())
				return;
			handleMediaPost(mediaPosts);
		} catch (RestClientException e) {
			LOGGER.log(Level.SEVERE, "Unable to fetch media posts", e);
		} catch (JsonMappingException e) {
			LOGGER.log(Level.SEVERE, "Unable to parse Media Posts", e);
		} catch (JsonProcessingException e) {
			LOGGER.log(Level.SEVERE, "Unable to parse Media Posts", e);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE, "Unable to consume media Url", e);
		}

	}

}

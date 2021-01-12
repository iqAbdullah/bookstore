package com.bookstore.media.utils;

import com.bookstore.media.entiy.MediaPostEntity;
import com.bookstore.media.model.MediaPost;

public class ConversionUtil {

	public static MediaPostEntity convert(final MediaPost mediaPost) {
		return MediaPostEntity.builder()
				.id(mediaPost.getId())
				.userId(mediaPost.getUserId())
				.title(mediaPost.getTitle())
				.body(mediaPost.getBody())
				.build();
	}
	
	public static MediaPost convert(final MediaPostEntity mediaPostEntity) {
		return MediaPost.builder()
				.id(mediaPostEntity.getId())
				.userId(mediaPostEntity.getUserId())
				.title(mediaPostEntity.getTitle())
				.body(mediaPostEntity.getBody())
				.build();
	}
	
}

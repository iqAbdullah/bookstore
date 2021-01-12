package com.bookstore.media.entiy;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Document(collection = "media_posts")
@AllArgsConstructor
public class MediaPostEntity {
	@Id
	private String id;
	@Field("user_id")
	private String userId;
	private String title;
	private String body;
	
}
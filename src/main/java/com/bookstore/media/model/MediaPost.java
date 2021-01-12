package com.bookstore.media.model;

import com.bookstore.es.model.ESEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MediaPost implements ESEntity {
	private String id;
	private String userId;
	private String title;
	private String body;

	public boolean compare(final MediaPost mediapost) {
		return ((this.id.equals(mediapost.getId()))
				&& (this.userId.equals(mediapost.getUserId()))
				&& (this.title.equals(mediapost.getTitle()))
				&& (this.body.equals(mediapost.getBody())));
	}

}

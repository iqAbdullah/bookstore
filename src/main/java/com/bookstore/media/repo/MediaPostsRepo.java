package com.bookstore.media.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.bookstore.media.entiy.MediaPostEntity;

public interface MediaPostsRepo extends MongoRepository<MediaPostEntity, String>{

	public Optional<MediaPostEntity> findById(final String id);
	
}

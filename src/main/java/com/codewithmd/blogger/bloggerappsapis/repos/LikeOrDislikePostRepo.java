package com.codewithmd.blogger.bloggerappsapis.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codewithmd.blogger.bloggerappsapis.entities.LikeOrDislikePost;

public interface LikeOrDislikePostRepo extends JpaRepository<LikeOrDislikePost, Integer> {

	Optional<LikeOrDislikePost> findByUserIdAndPostId(Integer userId, Integer postId);
}

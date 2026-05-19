package com.codewithmd.blogger.bloggerappsapis.services.interfaces;

import java.io.IOException;

import org.springframework.scheduling.annotation.Async;

import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.PostDto;
import com.codewithmd.blogger.bloggerappsapis.payloads.ShareEmail;

public interface PostService {
	// create
	ResponseModel createPost(PostDto postDto, Integer userId, Integer categoryId);

	// update
	ResponseModel updatePost(PostDto postDto);

	// delete
	ResponseModel deletePost(Integer postId);

	// get All post

	ResponseObjectModel getAllPost(Integer pageNumber, String sortBy, Integer userId);

	ResponseObjectModel getPostById(Integer postId);

	// get all posts by category
	ResponseObjectModel getPostByCategory(Integer categoryId);

	// get all posts by USer
	ResponseObjectModel getPostByUser(Integer userId);

	// search Post
	ResponseObjectModel searchPost(String keyword, Integer pageNumber, String sortBy,Integer userId);

	ResponseObjectModel getPostByUser(Integer userId, Integer pageNumber, String sortBy, String sortDir);

	ResponseObjectModel getPostByCategoryAndUser(Integer categoryId, Integer userId);

	ResponseModel savedPost(Integer userId, Integer postId);

	ResponseModel isPostSave(Integer userId, Integer postId);

	ResponseObjectModel getSavedPostByUser(Integer userId, Integer pageNumber, String sortBy, String sortDir);

//	ResponseObjectModel getSavePostByCategoryAndUser(Integer categoryId, Integer userId);

	ResponseObjectModel getSavePostByCategory(Integer categoryId, Integer userId);

	ResponseModel addLikeAndDisLike(Integer id, Boolean like, Integer userId);

	void sharePost(ShareEmail shareEmail);

	ResponseObjectModel setSubscriber(Integer currentSubsciberId, Integer bloggerUserId);

	ResponseObjectModel getSubscriber(Integer currentSubsciberId, Integer bloggerUserId);

	ResponseObjectModel unSubscribe(Integer currentSubsciberId, Integer bloggerUserId);

	ResponseModel unSavedPost(Integer userId, Integer postId);

	ResponseObjectModel searchPostByUser(String keyword, Integer pageNumber, String sortBy, Integer userId);

	ResponseObjectModel downloadPostInPDF(Integer postId) throws IOException;

	ResponseObjectModel reportPostFeed(Long postId, Long userId);

}

package com.codewithmd.blogger.bloggerappsapis.payloads;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class PostDto {

	private Integer postId;

	private String title;
	@NotEmpty
	@NotNull
	private String content;

	private String imageName;

	private Date date;

	private UserDto user;

	private CategoryDto category;
	
	private Integer likePost;

	private Integer disLikePost;
	
	private Integer numberOfViews=0;
	
	private List<CommentDto> comments = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

	public CategoryDto getCategory() {
		return category;
	}

	public void setCategory(CategoryDto category) {
		this.category = category;
	}

	


	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public List<CommentDto> getComments() {
		return comments;
	}

	public void setComments(List<CommentDto> comments) {
		this.comments = comments;
	}

	public Integer getLikePost() {
		return likePost;
	}

	public void setLikePost(Integer likePost) {
		this.likePost = likePost;
	}

	public Integer getDisLikePost() {
		return disLikePost;
	}

	public void setDisLikePost(Integer disLikePost) {
		this.disLikePost = disLikePost;
	}

	public Integer getNumberOfViews() {
		return numberOfViews;
	}

	public void setNumberOfViews(Integer numberOfViews) {
		this.numberOfViews = numberOfViews;
	}
	
	

}

package com.codewithmd.blogger.bloggerappsapis.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class LikeOrDislikePost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer postId;

	private Integer userId;

	private Boolean likeOrDislike;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Boolean getLikeOrDislike() {
		return likeOrDislike;
	}

	public void setLikeOrDislike(Boolean likeOrDislike) {
		this.likeOrDislike = likeOrDislike;
	}

}

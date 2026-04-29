package com.codewithmd.blogger.bloggerappsapis.payloads;

import java.util.ArrayList;
import java.util.List;

public class ShareEmail {
	private Integer userId;
	private Integer postId;

	List<String> emails = new ArrayList<>();

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

}

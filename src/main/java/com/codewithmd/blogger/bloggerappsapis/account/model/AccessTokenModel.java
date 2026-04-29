package com.codewithmd.blogger.bloggerappsapis.account.model;

public class AccessTokenModel {

	private Long userId;

	public AccessTokenModel() {

	}

	public AccessTokenModel(Long userId) {
		super();
		this.userId = userId;

	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

}

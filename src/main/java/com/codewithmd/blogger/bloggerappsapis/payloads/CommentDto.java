package com.codewithmd.blogger.bloggerappsapis.payloads;

public class CommentDto {

	private int id;

	private String comment;
	
	private String userName;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}



}

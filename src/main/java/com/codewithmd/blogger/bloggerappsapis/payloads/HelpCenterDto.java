package com.codewithmd.blogger.bloggerappsapis.payloads;

public class HelpCenterDto {
	
	private Integer userId;
	
	private String subject;
	
	private String description;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	

}

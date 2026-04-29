package com.codewithmd.blogger.bloggerappsapis.payloads;

public class WelcomeEmailModel {
	
	private String email;
	
	private String userName;
	
	

	public WelcomeEmailModel(String email, String userName) {
		super();
		this.email = email;
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
}

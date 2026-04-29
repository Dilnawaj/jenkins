package com.codewithmd.blogger.bloggerappsapis.account.model;


public class TokenModel {

	private boolean logout;

	private boolean setTokens;

	private String accessToken;

	private String loginToken;

	private String refreshToken;

	public boolean isLogout() {
		return logout;
	}

	public void setLogout(boolean logout) {
		this.logout = logout;
	}

	public boolean isSetTokens() {
		return setTokens;
	}

	public void setSetTokens(boolean setTokens) {
		this.setTokens = setTokens;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getUnknownError() {
		return "{\"errorCode\" : 3024, \"error\":\"Unexpected error occurred, Please try again\"}";
	}

	public String json() {
		return "{ \"logout\": " + logout + ", \"customError\": " + getUnknownError() + " ,\"setTokens\": " + setTokens
				+ ",\"accessToken\":\"" + accessToken + "\", \"loginToken\": \"" + loginToken + "\", \"refreshToken\":\"" + refreshToken
				+ "\"}";
	}

}

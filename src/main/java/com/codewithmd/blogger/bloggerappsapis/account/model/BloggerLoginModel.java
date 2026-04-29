package com.codewithmd.blogger.bloggerappsapis.account.model;
import org.springframework.http.HttpStatus;
import com.codewithmd.blogger.bloggerappsapis.payloads.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class BloggerLoginModel {
	
	private String logintoken;

	private String accessToken;

	private String errorMessage;

	private HttpStatus statusCode = HttpStatus.OK;

	private String errorCode;

	private String refreshToken;

	private Long accessTokenValidity;

	private Long refreshTokenValidity;
	
	private UserDto user;

	@JsonIgnore
	public String getErrorMessageJSON() {
		return "{\"errorCode\" : " + errorCode + ", \"error\":\"" + errorMessage + "\"}";
	}

	public String getLogintoken() {
		return logintoken;
	}

	public void setLogintoken(String logintoken) {
		this.logintoken = logintoken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public HttpStatus getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Long getRefreshTokenValidity() {
		return refreshTokenValidity;
	}

	public void setRefreshTokenValidity(Long refreshTokenValidity) {
		this.refreshTokenValidity = refreshTokenValidity;
	}

	public Long getAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(Long accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

}

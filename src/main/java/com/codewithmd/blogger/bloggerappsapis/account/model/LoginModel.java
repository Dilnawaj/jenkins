package com.codewithmd.blogger.bloggerappsapis.account.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginModel {

	private String email;

	private String password;

	private Boolean rememberMe;

	private String accessToken;

	private String userKey;

	private String dob;

	private String newPassword;

	/**
	 * 
	 * code hold credential return by google login.
	 * 
	 * @param code credential get by google login
	 */
	private String code;

	public String getEmail() {

		if (email == null) {
			return "";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		if (password == null) {
			return "";
		}
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getRememberMe() {
		if (rememberMe == null) {
			return false;
		}
		return rememberMe;
	}

	public void setRememberMe(Boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getCode() {
		if (code == null) {
			return "";
		}
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getNewPassword() {
		if (newPassword == null) {
			return "";
		}
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public boolean checkValidationForUpdatePassword() {
		if (!getNewPassword().equals("") && !getPassword().equals("")) {
			return true;
		}
		return false;
	}

	public boolean checkValidationForPassword() {
		if (!getPassword().equals("")) {
			return true;
		}
		return false;
	}

	public boolean checkValidationForLogin() {
		if (!getEmail().equals("") && !getPassword().equals("")) {
			return true;
		}
		return false;
	}

}

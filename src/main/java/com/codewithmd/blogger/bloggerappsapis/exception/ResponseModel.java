package com.codewithmd.blogger.bloggerappsapis.exception;

import org.springframework.http.HttpStatus;

public class ResponseModel {

	private HttpStatus responseCode;
	
	private Object response;

	public ResponseModel() {

	}

	public ResponseModel(Object response, HttpStatus responseCode) {
		super();
		this.responseCode = responseCode;
		this.response = response;
	}

	public ResponseModel(Object data, HttpStatus status, boolean isError) {
		super();
		if (isError) {
			this.response = "{\"error\" : \"" + data + "\"}";
		} else {
			this.response = "{\"message\" : \"" + data + "\"}";
		}
		this.responseCode = status;
	}

	public HttpStatus getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(HttpStatus noContent) {
		this.responseCode = noContent;
	}

	public Object getResponse() {
		return response;
	}

	public void role(Object response) {
		this.response = response;
	}

	@Override
	public String toString() {
		return "JobApplyResponseModel [responseCode=" + responseCode + ", response=" + response + "]";
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	

}

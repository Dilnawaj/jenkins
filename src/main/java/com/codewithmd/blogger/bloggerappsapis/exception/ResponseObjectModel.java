package com.codewithmd.blogger.bloggerappsapis.exception;

import org.springframework.http.HttpStatus;

public class ResponseObjectModel {
	
	private HttpStatus responseCode;

	private Object response;
	public ResponseObjectModel() {
		super();
	}
	public ResponseObjectModel( Object response,HttpStatus responseCode) {
		super();
		this.responseCode = responseCode;
		this.response = response;
	}

	public HttpStatus getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(HttpStatus responseCode) {
		this.responseCode = responseCode;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

}

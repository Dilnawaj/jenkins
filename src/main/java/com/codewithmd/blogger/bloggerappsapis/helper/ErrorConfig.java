package com.codewithmd.blogger.bloggerappsapis.helper;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:errorMessages.properties")
public class ErrorConfig {


	@Value("${InvalidInput}")
	private String invalidInput;

	@Value("${AddSuccess}")
	private String addSuccess;
//
	@Value("${UnknowError}")
	private String unknowError;

	@Value("${DuplicateError}")
	private String duplicateError;
	
	@Value("${UpdateSuccess}")
	private String updateSuccess;
	
	@Value("${UnexpectedErrorMessage}")
	private String unexpectedErrorMessage;
	
	private static final String FIELD = "{field}";
//

	public String getInvalidInput() {
		return getErrorJSON(invalidInput);
	}

//
	public String getAddSuccess(String field) {
		return getSuccessJSON(addSuccess.replace("{field}", field));
	}
//
	public String getUnknowError() {
		return getErrorJSON(200, unknowError);
	}

	public String getDuplicateError(String field) {
		return duplicateError.replace("{field}", field);
	}

	private String getSuccessJSON(String message) {
		if (!message.trim().endsWith(".")) {
			message = message + ".";
		}
		return "{\"success\" : true, \"message\":\"" + message + "\"}";
	}


	public String getErrorJSON(int errorCode, String message) {
		return getErrorJSON(errorCode + "", message);
	}
//
//	/*
//	 * to-do remove me
//	 */
	public String getUnexpectedErrorCode() {
		return getErrorJSON("", "");
	}

	
	public String getErrorJSON(String errorCode, String message) {
		if (!message.trim().endsWith(".")) {
			message = message + ".";
		}
		if (errorCode == null) {
			return "{\"error\":\"" + message + "\"}";
		} else {
			return "{\"errorCode\" : " + errorCode + ", \"error\":\"" + message + "\"}";
		}
	}


	private String getErrorJSON(String message) {
		if (!message.trim().endsWith(".")) {
			message = message + ".";
		}
		return message;
	}
	public String getUpdateSuccess(String field) {
		return getSuccessJSON(updateSuccess.replace(FIELD, field));
	}

	public String getUnexpectedErrorMessage() {
		return unexpectedErrorMessage;
	}
	
	

}
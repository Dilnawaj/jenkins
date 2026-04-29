package com.codewithmd.blogger.bloggerappsapis.exception;


/**
 * @author Waris Majid created on Sep 2, 2022 last updated on Sep 2, 2022
 */
public class BadRequestException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BadRequestException(String message) {
		super(message);
	}

	public static BadRequestException of(String message) {
		return new BadRequestException(message);
	}
}

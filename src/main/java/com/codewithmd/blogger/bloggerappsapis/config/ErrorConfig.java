package com.codewithmd.blogger.bloggerappsapis.config;

public class ErrorConfig {

	public static String addMessage(String title) {
		String response = "{\"message\":\"{Title}  created successfully.\"}";
		response = response.replace("{Title}", title);
		return response;
	}
	public static String customMessage(String title) {
		String response = "{\"message\":\"{Title}  \"}";
		response = response.replace("{Title}", title);
		return response;
	}
	public static String updateMessage(String title) {
		String response = "{\"message\":\"{Title} updated successfully.\"}";
		response = response.replace("{Title}", title);
		return response;
	}

	public static String deleteMessage(String title, String id) {
		String response = "{\"message\":\"This {Title} id - {id} has been deleted.\"}";
		response = response.replace("{Title}", title);
		response = response.replace("{id}", id);
		return response;
	}

	public static String notFoundException(String title, String id) {
		String response = "{\"message\":\"Unable to find {Title} with this id {id}.\"}";
		response = response.replace("{Title}", title);
		response = response.replace("{id}", id);
		return response;
	}

	public static String updateError(String field) {
		String response = "{\"message\":\"There is an error in updating {field} .\"}";
		response = response.replace("{field}", field);
		return response;
	}

	public static String unknownError() {
		return "{\"error\":\"There is an error in the request,Please try again.\"}";

	}

}

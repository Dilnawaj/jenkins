package com.codewithmd.blogger.bloggerappsapis.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.CommentDto;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.CommentService;

@RestController
@RequestMapping("/comment")
@Validated
public class CommentController {
	@Autowired
	private CommentService commentService;

	@CrossOrigin
	@PostMapping(value = "/post/{postId}/user/{userId}/comment", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createComment(@Valid @RequestBody CommentDto commentDto, @PathVariable Integer postId,
			@PathVariable Integer userId,@RequestHeader(value = "userId")Integer authorUserId) {
		System.out.println(authorUserId);
		ResponseModel createComment = this.commentService.createComment(commentDto, postId, userId,authorUserId);
		return new ResponseEntity<>(createComment.getResponse(), createComment.getResponseCode());
	}

	@CrossOrigin
	@PutMapping(produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> updateComment(@Valid @RequestBody CommentDto commentDto) {
		ResponseModel updateComment = this.commentService.updateComment(commentDto);
		return new ResponseEntity<>(updateComment.getResponse(), updateComment.getResponseCode());
	}

	@CrossOrigin
	@DeleteMapping(value = "/{id}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> deleteComment(@PathVariable Integer id) {
		ResponseModel deleteComment = this.commentService.deleteComment(id);
		return new ResponseEntity<>(deleteComment.getResponse(), deleteComment.getResponseCode());
	}

}

package com.codewithmd.blogger.bloggerappsapis.services.interfaces;

import javax.validation.Valid;

import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.CommentDto;

public interface CommentService {

	ResponseModel createComment(CommentDto commentDto, Integer postId, Integer userId,Integer authorUserId);

	ResponseModel deleteComment(Integer commentId);

	ResponseModel updateComment(@Valid CommentDto commentDto);

}

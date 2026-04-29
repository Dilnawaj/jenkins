package com.codewithmd.blogger.bloggerappsapis.services.impl;

import java.util.Optional;
import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.codewithmd.blogger.bloggerappsapis.config.ErrorConfig;
import com.codewithmd.blogger.bloggerappsapis.entities.Comment;
import com.codewithmd.blogger.bloggerappsapis.entities.Post;
import com.codewithmd.blogger.bloggerappsapis.entities.User;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.CommentDto;
import com.codewithmd.blogger.bloggerappsapis.repos.CommentRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.PostRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.UserRepo;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.CommentService;

@Service
public class CommentServiceImpl implements CommentService {

	@Autowired
	private PostRepo postRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private CommentRepo commentRepo;

	@Autowired
	private ModelMapper modelMapper;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ResponseModel createComment(CommentDto commentDto, Integer postId, Integer userId,Integer authorUserId) {
		try {
			Optional<Post> post = postRepo.findById(postId);
			Optional<User> user = userRepo.findById(userId);
			Comment comment = this.modelMapper.map(commentDto, Comment.class);
			if (post.isPresent()) {
				comment.setPost(post.get());
			}
			if (user.isPresent()) {
				comment.setUser(user.get());
			}
			commentRepo.save(comment);
			commentDto=this.modelMapper.map(comment, CommentDto.class);
			commentDto.setUserName(user.get().getName());
			return new ResponseModel( commentDto, HttpStatus.CREATED);
		} catch (Exception e) {
			logger.error("createComment ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel deleteComment(Integer commentId) {
		try {
			Optional<Comment> comment = this.commentRepo.findById(commentId);
			if (!comment.isEmpty()) {
				this.commentRepo.delete(comment.get());
				return new ResponseModel(ErrorConfig.deleteMessage("Comment", commentId.toString()), HttpStatus.OK);
			} else {
				return new ResponseModel(ErrorConfig.notFoundException("Comment", commentId.toString()),
						HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error("deleteUser ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel updateComment(@Valid CommentDto commentDto) {
		try {
			Optional<Comment> comment = commentRepo.findById(commentDto.getId());
			if (!comment.isEmpty()) {
				comment.get().setComment(commentDto.getComment());
				commentRepo.save(comment.get());
				return new ResponseModel(ErrorConfig.updateMessage("Comment"), HttpStatus.ACCEPTED);
			} else {
				return new ResponseModel(ErrorConfig.updateError("Comment"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("updateUser ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}
}

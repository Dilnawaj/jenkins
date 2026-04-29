package com.codewithmd.blogger.bloggerappsapis.repos;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codewithmd.blogger.bloggerappsapis.entities.Comment;
import com.codewithmd.blogger.bloggerappsapis.entities.Post;
import com.codewithmd.blogger.bloggerappsapis.entities.User;

public interface CommentRepo extends JpaRepository<Comment, Integer> {

	List<Comment> findByUser(User u);
	
	List<Comment> findByPost(Post p);
	

}

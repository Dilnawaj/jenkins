package com.codewithmd.blogger.bloggerappsapis.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.codewithmd.blogger.bloggerappsapis.entities.SavePost;

public interface SavePostRepo extends JpaRepository<SavePost, Integer> {

	Optional<SavePost> findByUserIdAndPostId(Integer userId, Integer postId);

	List<SavePost> findByUserId(Integer userId);
	List<SavePost> findByUserIdOrderBySavedAtDesc(Integer userId);
	

//Page<Post> findByUserAndPost(User user,Post p, Pageable pp);

}

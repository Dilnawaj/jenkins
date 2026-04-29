package com.codewithmd.blogger.bloggerappsapis.repos;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codewithmd.blogger.bloggerappsapis.entities.Subscribe;

public interface SubscribeRepo extends JpaRepository<Subscribe, Integer> {
	@Query("select s.currentSubsciberId from Subscribe s where s.bloggerUserId = ?1")
	List<Integer> findByBloggerUserId(Integer bloggerUserId);
	
	@Query("select s.bloggerUserId from Subscribe s where s.currentSubsciberId = ?1")
	List<Integer> findByCurrentSubsciberId(Integer currentSubsciberId);

	Optional<Subscribe> findByBloggerUserIdAndCurrentSubsciberId(Integer bloggerUserId, Integer currentSubsciberId);

	
}

package com.codewithmd.blogger.bloggerappsapis.repos;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codewithmd.blogger.bloggerappsapis.entities.Category;
import com.codewithmd.blogger.bloggerappsapis.entities.Comment;
import com.codewithmd.blogger.bloggerappsapis.entities.Post;
import com.codewithmd.blogger.bloggerappsapis.entities.SavePost;
import com.codewithmd.blogger.bloggerappsapis.entities.User;

public interface PostRepo extends JpaRepository<Post, Integer> {
	@Query("select p from Post p where p.user = ?1 and p.suspendPost = false ")
	List<Post> findByUser(User u, Sort sort);

	List<Post> findByUser(User u);

	List<Post> findByCategory(Category c);

	List<Post> findByCategoryAndUser(Category c, User u);

	@Query("select p from Post p where p.isPostContentChecked = false and p.suspendPost = false")
	List<Post> findAllPostUnCheckedContent();

	@Query("select p from Post p where p.susbscriberEmail = false and p.suspendPost = false")
	List<Post> findAllPostNewContent();

	@Modifying
	@Transactional
	@Query(value = "Update Post p set p.isPostContentChecked = true WHERE p.isPostContentChecked = false ")
	public void updatePostUnCheckedContent();

	@Modifying
	@Transactional
	@Query(value = "Update Post p set p.suspendPost = true WHERE p.postId in ?1 ")
	public void updatePostBysuspendPost(List<Integer> ids);

	// b.user_id =:userId
	@Modifying
	@Transactional
	@Query("UPDATE Post p SET p.suspendPost = true WHERE p.postId = :id")
	void updatePostByWarningUser(@Param("id") Integer id);

	@Query("select count(p) from Post p where p.postId =?1 and p.suspendPost = false")
	public int checkId(Integer id);

	@Query("select p from Post p where p.postId in ?1 and p.suspendPost = false")
	public List<Post> getPostByIds(List<Integer> ids);

	@Query("SELECT p FROM Post p where p.suspendPost = false ORDER BY p.likePost desc ")
	public Page<Post> getPostsSortedByLike(Pageable p);

	@Query("SELECT p FROM Post p where p.suspendPost = false ORDER BY p.numberOfViews desc")
	public Page<Post> getPostsSortedByViews(Pageable p);

	@Query("SELECT p FROM Post p where p.suspendPost = false ORDER BY  p.date ASC")
	public Page<Post> getPostsSortedByOld(Pageable p);

	@Query("SELECT p FROM Post p where p.suspendPost = false ORDER BY  p.date DESC")
	public Page<Post> getPostsSortedByNew(Pageable p);

	@Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments c where p.suspendPost = false GROUP BY p.id ORDER BY COUNT(c) DESC")
	List<Post> getPostsSortedByComments();

	@Query("SELECT p FROM Post p where p.suspendPost = false ORDER BY  p.recommendedPost desc")
	public Page<Post> getRecommendedPosts(Pageable p);

	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key ORDER BY  p.date DESC")
	List<Post> findByKeyword(@Param("key") String key);

	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and (p.user.id = :userId) AND (p.title LIKE %:key% OR p.content LIKE %:key% OR u.name LIKE %:key%) ORDER BY p.date DESC")
	List<Post> findByKeyword(@Param("key") String key, @Param("userId") Integer userId);

	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key ORDER BY  p.recommendedPost desc")
	List<Post> findByKeywordAndRecommend(@Param("key") String key);

	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key  ORDER BY  p.date DESC")
	List<Post> findByKeywordAndNew(@Param("key") String key);

	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and (p.user.id = :userId) AND (p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key)  ORDER BY  p.date DESC")
	List<Post> findByKeywordAndNew(@Param("key") String key, @Param("userId") Integer userId);

	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key  ORDER BY  p.date ASC")
	List<Post> findByKeywordAndOld(@Param("key") String key);

	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and (p.user.id = :userId) AND (p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key)  ORDER BY  p.date ASC")
	List<Post> findByKeywordAndOld(@Param("key") String key, @Param("userId") Integer userId);
	
	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and (p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key) ORDER BY size(p.comments) DESC")
	List<Post> findByKeywordAndComments(@Param("key") String key);



	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key   ORDER BY p.numberOfViews desc")
	List<Post> findByKeywordAndViews(@Param("key") String key);

	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.suspendPost = false and p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key   ORDER BY p.likePost desc")
	List<Post> findByKeywordAndLike(@Param("key") String key);

	@Query("select p from Post p where p.user.id in ?1 order by p.date desc")
	public Page<Post> getPostByUserIds(List<Integer> userIds, Pageable p);

	@Query("SELECT p FROM Post p LEFT JOIN p.user u WHERE p.title LIKE :key OR p.content LIKE :key OR u.name LIKE :key and p.user.id in :userId ORDER BY p.date desc")
	public List<Post> getPostByKeywordAndUserIds(@Param("key") String key, List<Integer> userId);

	@Modifying
	@Transactional
	@Query(value = "UPDATE Post p " + "LEFT JOIN Comment c ON p.post_id = c.post_post_id "
			+ "SET p.recommended_post = (" + "  (p.number_of_views / 10) + "
			+ "  IFNULL((SELECT COUNT(*) FROM Comment WHERE post_post_id = p.post_id), 0) + "
			+ "  IFNULL((p.like_post - IFNULL(p.dis_like_post, 0)), 0)" + ")", nativeQuery = true)
	void updateRecommendationPost();

	// Post findByUserAndPost(User user,Post p);

//	Optional<Post> findByUserIdAndPostId(Integer userId, Integer postId);
}

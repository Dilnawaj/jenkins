package com.codewithmd.blogger.bloggerappsapis.repos;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.codewithmd.blogger.bloggerappsapis.entities.User;
import com.codewithmd.blogger.bloggerappsapis.payloads.WelcomeEmailModel;

public interface UserRepo extends JpaRepository<User, Integer> {

	Optional<User> findByEmail(String email);
	
	Optional<User> findByDobAndEmail(String dob,String email);
	
	List<User> findByVerificationCode(String code);
	
	@Query("SELECT new com.codewithmd.blogger.bloggerappsapis.payloads.WelcomeEmailModel(u.email, u.name) FROM User u  WHERE u.welcomeEmail=false and u.isPasswordSet = true and u.password is not null")
	public List<WelcomeEmailModel> getIdsOfNewUsers();
	@Query("SELECT u FROM User u  WHERE u.isGoogleAccount = false  and u.isPasswordSet = false  and u.userType = 'NORMAL_USER' ")
	public List<User> getIdsOfUselessUsers();

	@Query("SELECT u FROM User u  WHERE  u.isPasswordSet = false  and u.userType = 'ADMIN' ")
	public List<User> getIdsOfUselessAdmins();

	@Modifying
	@Transactional
	@Query(value = "Update User u set u.welcomeEmail = true WHERE u.welcomeEmail = false and  u.isPasswordSet = true")
	public void updateWelcomeStatus();
	
	@Query("select count(u) from User u where u.id =?1")
	public int checkId(Integer id);

	@Modifying
	@Transactional
	void deleteByEmail(String email);

	

}

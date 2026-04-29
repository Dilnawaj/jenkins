package com.codewithmd.blogger.bloggerappsapis.repos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codewithmd.blogger.bloggerappsapis.entities.Category;

public interface CategoryRepo extends JpaRepository<Category, Integer> {

	@Query("select c from Category c where c.isCategoryRequest = false")
	List<Category> findAllCategory();
	
	@Query("select c from Category c where c.isCategoryRequest = true")
	List<Category> findAllCategoryRequest();

}

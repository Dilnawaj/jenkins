package com.codewithmd.blogger.bloggerappsapis.repos;

import java.util.List;

import com.codewithmd.blogger.bloggerappsapis.payloads.CategoryName;
import com.codewithmd.blogger.bloggerappsapis.payloads.WelcomeEmailModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codewithmd.blogger.bloggerappsapis.entities.Category;

public interface CategoryRepo extends JpaRepository<Category, Integer> {

	@Query("select c from Category c where c.isCategoryRequest = false")
	List<Category> findAllCategory();
	
	@Query("select c from Category c where c.isCategoryRequest = true")
	List<Category> findAllCategoryRequest();

    @Query("SELECT new com.codewithmd.blogger.bloggerappsapis.payloads.CategoryName(c.categoryTitle) FROM Category c ")
    public List<CategoryName> getAllCategoryName();

}

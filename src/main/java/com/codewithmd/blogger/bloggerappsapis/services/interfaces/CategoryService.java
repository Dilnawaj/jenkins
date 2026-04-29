package com.codewithmd.blogger.bloggerappsapis.services.interfaces;

import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.CategoryDto;

public interface CategoryService {
// create
	ResponseModel createCategory(CategoryDto categoryDto);

//update
	ResponseModel updateCategory(CategoryDto categoryDto);

//delete 
	ResponseModel deleteCategory(Integer categoryId);

// get
	ResponseObjectModel getCategory(Integer categoryId);

// getAll
	ResponseObjectModel getAllCategories();

	ResponseModel createCategoryRequest(Integer categoryId);

	ResponseObjectModel getAllRequestCategories();

}

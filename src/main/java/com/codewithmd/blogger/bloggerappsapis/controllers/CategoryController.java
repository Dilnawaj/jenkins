package com.codewithmd.blogger.bloggerappsapis.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.CategoryDto;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.CategoryService;

@RestController
@RequestMapping("/categorie")
@Validated
public class CategoryController {
	@Autowired
	private CategoryService categoryService;
	
	@CrossOrigin
	@PostMapping(value = "/{id}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createCategoryRequest(@PathVariable Integer id) {
		ResponseModel createCategory = this.categoryService.createCategoryRequest(id);
		return new ResponseEntity<>(createCategory.getResponse(), createCategory.getResponseCode());
	}

	@CrossOrigin
	@PostMapping(produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
		ResponseModel createCategory = this.categoryService.createCategory(categoryDto);
		return new ResponseEntity<>(createCategory.getResponse(), createCategory.getResponseCode());
	}

	// PUT ->Update Category
	@CrossOrigin
	@PutMapping(produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> updateCategory(@Valid @RequestBody CategoryDto categoryDto) {
		ResponseModel updateCategory = this.categoryService.updateCategory(categoryDto);
		return new ResponseEntity<>(updateCategory.getResponse(), updateCategory.getResponseCode());
	}

	// DELETE -> delete Category

	@CrossOrigin
	@DeleteMapping(value = "/{id}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> deleteCategory(@PathVariable Integer id) {
		ResponseModel deleteCategory = this.categoryService.deleteCategory(id);
		return new ResponseEntity<>(deleteCategory.getResponse(), deleteCategory.getResponseCode());
	}

	// GET -> Category Get
	@CrossOrigin
	@GetMapping("/{id}/category")
	public ResponseEntity<Object> getOneCategory(@PathVariable Integer id) {
		ResponseObjectModel getOneCategory = this.categoryService.getCategory(id);
		return new ResponseEntity<>(getOneCategory.getResponse(), getOneCategory.getResponseCode());
	}

	// GET -> Category All
	@CrossOrigin
	@GetMapping
	public ResponseEntity<Object> getAll() {
		ResponseObjectModel allCategory = this.categoryService.getAllCategories();
		return new ResponseEntity<>(allCategory.getResponse(), allCategory.getResponseCode());
	}
	@CrossOrigin
	@GetMapping("/request")
	public ResponseEntity<Object> getAllRequestCategory() {
		ResponseObjectModel allCategory = this.categoryService.getAllRequestCategories();
		return new ResponseEntity<>(allCategory.getResponse(), allCategory.getResponseCode());
	}
}

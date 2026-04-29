package com.codewithmd.blogger.bloggerappsapis.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.codewithmd.blogger.bloggerappsapis.config.ErrorConfig;
import com.codewithmd.blogger.bloggerappsapis.entities.Category;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.CategoryDto;
import com.codewithmd.blogger.bloggerappsapis.repos.CategoryRepo;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepo categoryRepo;

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Autowired
	private ModelMapper modelMapper;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ResponseModel createCategory(CategoryDto categoryDto) {
		try {
		
			Category category = this.modelMapper.map(categoryDto, Category.class);
			category.setIsCategoryRequest(true);
			this.categoryRepo.save(category);
			return new ResponseModel(ErrorConfig.addMessage("Category"), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("createCategory ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}
public ResponseModel createCategoryRequest(Integer categoryId) {
	try {
		

		Category category = categoryRepo.findById(categoryId).get();
		category.setIsCategoryRequest(false);
		this.categoryRepo.save(category);
		return new ResponseModel(ErrorConfig.addMessage("Category"), HttpStatus.OK);
	} catch (Exception e) {
		logger.error("createCategoryRequest ", e);
		return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
	}
	}

	public ResponseModel updateCategory(CategoryDto categoryDto) {
		try {
			Optional<Category> categoryOptional = categoryRepo.findById(categoryDto.getCategoryId());
			if (categoryOptional.isPresent()) {
				Category category = categoryOptional.get();
				category.setCategoryTitle(categoryDto.getCategoryTitle());
				category.setCategoryDescription(categoryDto.getCategoryDescription());
				this.categoryRepo.save(category);
				return new ResponseModel(ErrorConfig.updateMessage("Category"), HttpStatus.ACCEPTED);
			} else {
				return new ResponseModel(ErrorConfig.updateError("Catagory"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("updateCategory ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel deleteCategory(Integer categoryId) {
		try {
			Category category = this.categoryRepo.getById(categoryId);
			if (category != null) {
				this.categoryRepo.delete(category);
				return new ResponseModel(ErrorConfig.deleteMessage("Category", categoryId.toString()), HttpStatus.OK);
			} else {
				return new ResponseModel(ErrorConfig.notFoundException("Category", categoryId.toString()),
						HttpStatus.NOT_FOUND);
			}

		} catch (Exception e) {
			logger.error("deleteCategory ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseObjectModel getCategory(Integer categoryId) {
		CategoryDto categoryDto = new CategoryDto();
		try {
			Category category = this.categoryRepo.getById(categoryId);
			categoryDto = this.modelMapper.map(category, CategoryDto.class);
			return new ResponseObjectModel(categoryDto, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("getCategory ", e);
			return new ResponseObjectModel(categoryDto, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseObjectModel getAllCategories() {
		List<CategoryDto> categoryDto = new ArrayList<>();
		try {
			List<Category> category = this.categoryRepo.findAllCategory();
			categoryDto = category.stream().map(cat -> this.modelMapper.map(cat, CategoryDto.class))
					.collect(Collectors.toList());
			return new ResponseObjectModel(categoryDto, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("getAllCategories ", e);
			return new ResponseObjectModel(categoryDto, HttpStatus.BAD_REQUEST);


		}
	}

	public ResponseObjectModel getAllRequestCategories() {
		List<CategoryDto> categoryDto = new ArrayList<>();
		try {
			List<Category> category = this.categoryRepo.findAllCategoryRequest();
			categoryDto = category.stream().map(cat -> this.modelMapper.map(cat, CategoryDto.class))
					.collect(Collectors.toList());
			return new ResponseObjectModel(categoryDto, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("getAllCategories ", e);
			return new ResponseObjectModel(categoryDto, HttpStatus.BAD_REQUEST);


		}
	}
}


	

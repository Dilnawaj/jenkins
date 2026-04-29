package com.codewithmd.blogger.bloggerappsapis.payloads;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {
	
	private Integer categoryId;
	
	@NotEmpty
	@NotNull
	@Size(min = 4, message = "Category Title  must be min of 4 characters")
	private String categoryTitle;
	
	@NotNull
	private String categoryDescription;

	public CategoryDto() {

	}

	public CategoryDto(Integer categoryId, String categoryTitle, String categoryDescription) {
		this.categoryId = categoryId;
		this.categoryTitle = categoryTitle;
		this.categoryDescription = categoryDescription;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryTitle() {
		return categoryTitle;
	}

	public void setCategoryTitle(String categoryTitle) {
		this.categoryTitle = categoryTitle;
	}

	public String getCategoryDescription() {
		return categoryDescription;
	}

	public void setCategoryDescription(String categoryDescription) {
		this.categoryDescription = categoryDescription;
	}

}

package com.codewithmd.blogger.bloggerappsapis.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Subscribe {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer bloggerUserId;

	private Integer currentSubsciberId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	

	public Integer getBloggerUserId() {
		return bloggerUserId;
	}

	public void setBloggerUserId(Integer bloggerUserId) {
		this.bloggerUserId = bloggerUserId;
	}

	public Integer getCurrentSubsciberId() {
		return currentSubsciberId;
	}

	public void setCurrentSubsciberId(Integer currentSubsciberId) {
		this.currentSubsciberId = currentSubsciberId;
	}


}

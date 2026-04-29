package com.codewithmd.blogger.bloggerappsapis.account.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Role {

	@Id
	private Integer roleId;

	private String name;

	private Integer priority;

	private Boolean isDefault;

	private Boolean isEditable;

	private String userType;

	public Role() {

	}

	public Role(Integer roleId, String name, Integer priority, Boolean isDefault, Boolean isEditable, String userType) {
		super();
		this.roleId = roleId;
		this.name = name;
		this.priority = priority;
		this.isDefault = isDefault;
		this.isEditable = isEditable;
		this.userType = userType;
	}

	
	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public Boolean getIsEditable() {
		return isEditable;
	}

	public void setIsEditable(Boolean isEditable) {
		this.isEditable = isEditable;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	

}

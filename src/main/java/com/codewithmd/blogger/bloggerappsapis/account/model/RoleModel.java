package com.codewithmd.blogger.bloggerappsapis.account.model;

public class RoleModel {

	private Integer roleId;

	private String roleName;

	private int priority;

	private boolean isDefault;

	private boolean isEditable;

	private String userType;

	public RoleModel() {

	}

	public RoleModel(Integer roleId, String roleName, Integer priority, Boolean isDefault, Boolean isEditable, String userType) {
		super();
		if (isDefault == null) {
			isDefault = false;
		}
		if (isEditable == null) {
			isEditable = false;
		}
		this.roleId = roleId;
		this.roleName = roleName;
		this.priority = priority;
		this.isDefault = isDefault;
		this.isEditable = isEditable;
		this.userType = userType;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public int getRoleId() {
		if (roleId == null) {
			return 0;
		}
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

}

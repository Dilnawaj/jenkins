package com.codewithmd.blogger.bloggerappsapis.account.model;

public class PermissionRoleMappingModel {

	private boolean permissionAllowed;

	private int id;

	private int roleId;

	private int permissionId;

	public PermissionRoleMappingModel() {

	}

	public PermissionRoleMappingModel(boolean permissionAllowed, int id, int roleId, int permissionId) {
		super();
		this.id = id;
		this.permissionAllowed = permissionAllowed;
		this.roleId = roleId;
		this.permissionId = permissionId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isPermissionAllowed() {
		return permissionAllowed;
	}

	public void setPermissionAllowed(boolean permissionAllowed) {
		this.permissionAllowed = permissionAllowed;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public int getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(int permissionId) {
		this.permissionId = permissionId;
	}

	@Override
	public String toString() {
		return "PermissionRoleMappingModel [permissionAllowed=" + permissionAllowed + ", roleId=" + roleId
				+ ", permissionId=" + permissionId + "]";
	}

}

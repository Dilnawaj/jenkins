package com.codewithmd.blogger.bloggerappsapis.account.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PermissionRecord_Role_Mapping")
public class PermissionRoleMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", insertable = false)
	private Integer id;

	@Column(name = "permissionRecordId")
	private Integer permissionRecordId;

	@Column(name = "roleId")
	private Integer roleId;

	public int getId() {
		if (id == null) {
			return 0;
		}
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getPermissionRecordId() {
		if (permissionRecordId == null) {
			return 0;
		}
		return permissionRecordId;
	}

	public void setPermissionRecordId(Integer permissionRecordId) {
		this.permissionRecordId = permissionRecordId;
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

}

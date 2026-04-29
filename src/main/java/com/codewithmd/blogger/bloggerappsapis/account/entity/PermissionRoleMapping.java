package com.codewithmd.blogger.bloggerappsapis.account.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

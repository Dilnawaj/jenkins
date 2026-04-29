package com.codewithmd.blogger.bloggerappsapis.account.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PermissionRecord")
public class Permission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", insertable = false)
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "code")
	private String code;

	public Permission() {

	}

	public Permission(Integer id, String name, String code) {
		super();
		this.id = id;
		this.name = name;
		this.code = code;
	}

	public int getId() {
		if (id == null) {
			return 0;
		}
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		if (name == null) {
			return "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		if (code == null) {
			return "";
		}
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "PermissionEntity [id=" + id + ", name=" + name + ", code=" + code + "]";
	}

}

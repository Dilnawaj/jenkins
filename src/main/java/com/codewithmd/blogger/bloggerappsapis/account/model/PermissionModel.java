package com.codewithmd.blogger.bloggerappsapis.account.model;

public class PermissionModel {

	private int id;

	private String name;

	private String code;

	public PermissionModel() {

	}

	public PermissionModel(int id, String name, String code) {
		super();
		this.id = id;
		this.name = name;
		this.code = code;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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
		return "PermissionModel [id=" + id + ", name=" + name + ", code=" + code + "]";
	}

}

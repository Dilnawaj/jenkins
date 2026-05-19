package com.codewithmd.blogger.bloggerappsapis.account.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Client_Role_Mapping")
public class ClientRole {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", insertable = false)
	private Long id;

	@Column(name = "clientId")
	private Long clientId;

	@Column(name = "roleId")
	private Integer roleId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public ClientRole(Long id, Long clientId, Integer roleId) {
		super();
		this.id = id;
		this.clientId = clientId;
		this.roleId = roleId;
	}

	public ClientRole() {
		super();
	}

	@Override
	public String toString() {
		return "ClientRole [id=" + id + ", clientId=" + clientId + ", roleId=" + roleId + "]";
	}

}

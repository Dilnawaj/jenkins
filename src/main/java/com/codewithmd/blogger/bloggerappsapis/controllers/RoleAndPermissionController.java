package com.codewithmd.blogger.bloggerappsapis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codewithmd.blogger.bloggerappsapis.account.model.ClientRoleModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionRoleMappingModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.RoleModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.RoleAndPermissionService;

@RestController
@RequestMapping("/security")
public class RoleAndPermissionController {
	@Autowired
	private RoleAndPermissionService roleAndPermissionService;

	@PostMapping(value = "/role", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createRole(@RequestBody RoleModel roleModel) {
		ResponseModel createRole = this.roleAndPermissionService.createRole(roleModel);
		return new ResponseEntity<>(createRole.getResponse(), createRole.getResponseCode());
	}

	@PostMapping(value = "/permission", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createPermission(@RequestBody PermissionModel permissionModel) {
		ResponseModel createPermission = this.roleAndPermissionService.createPermission(permissionModel);
		return new ResponseEntity<>(createPermission.getResponse(), createPermission.getResponseCode());
	}

	@PostMapping(value = "/permissionrole", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createPermission(@RequestBody PermissionRoleMappingModel permissionRoleMappingModel) {
		ResponseModel createPermissionRole = this.roleAndPermissionService
				.createPermissionRoleMapping(permissionRoleMappingModel);
		return new ResponseEntity<>(createPermissionRole.getResponse(), createPermissionRole.getResponseCode());
	}

	@PostMapping(value = "/clientrole", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createClientRoleMapping(@RequestBody ClientRoleModel clientRoleModel) {
		ResponseModel createClientRole = this.roleAndPermissionService.createClientRoleMapping(clientRoleModel);
		return new ResponseEntity<>(createClientRole.getResponse(), createClientRole.getResponseCode());
	}

	@PutMapping(value = "/role", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> updateRole(@RequestBody RoleModel roleModel) {
		ResponseModel updateRole = this.roleAndPermissionService.updateRole(roleModel);
		return new ResponseEntity<>(updateRole.getResponse(), updateRole.getResponseCode());
	}

	@PutMapping(value = "/permission", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> updatePermission(@RequestBody PermissionModel permissionModel) {
		ResponseModel updatePermission = this.roleAndPermissionService.updatePermission(permissionModel);
		return new ResponseEntity<>(updatePermission.getResponse(), updatePermission.getResponseCode());
	}

	@PutMapping(value = "/permissionrole", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> updatePermissionRole(
			@RequestBody PermissionRoleMappingModel permissionRoleMappingModel) {
		ResponseModel updatePermissionRole = this.roleAndPermissionService
				.updatePermissionRoleMapping(permissionRoleMappingModel);
		return new ResponseEntity<>(updatePermissionRole.getResponse(), updatePermissionRole.getResponseCode());
	}

	@PutMapping(value = "/clientrole", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> updateClientRoleMapping(@RequestBody ClientRoleModel clientRoleModel) {
		ResponseModel updateClientRole = this.roleAndPermissionService.updateClientRoleMapping(clientRoleModel);
		return new ResponseEntity<>(updateClientRole.getResponse(), updateClientRole.getResponseCode());
	}

	@GetMapping(value = "/role", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getRole() {
		ResponseModel getRole = this.roleAndPermissionService.getRole();
		return new ResponseEntity<>(getRole.getResponse(), getRole.getResponseCode());
	}

	@GetMapping(value = "/permission", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getPermission() {
		ResponseModel getPermission = this.roleAndPermissionService.getPermission();
		return new ResponseEntity<>(getPermission.getResponse(), getPermission.getResponseCode());
	}

	@GetMapping(value = "/permissionrole", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getPermissionRole() {
		ResponseModel getPermissionRole = this.roleAndPermissionService.getPermissionRoleMapping();
		return new ResponseEntity<>(getPermissionRole.getResponse(), getPermissionRole.getResponseCode());
	}

	@GetMapping(value = "/clientrole", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getClientRoleMapping() {
		ResponseModel getClientRole = this.roleAndPermissionService.getClientRoleMapping();
		return new ResponseEntity<>(getClientRole.getResponse(), getClientRole.getResponseCode());
	}

}

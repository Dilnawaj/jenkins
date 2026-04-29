package com.codewithmd.blogger.bloggerappsapis.services.interfaces;

import com.codewithmd.blogger.bloggerappsapis.account.model.ClientRoleModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionRoleMappingModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.RoleModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;

public interface RoleAndPermissionService {

	ResponseModel createRole(RoleModel roleModel);

	ResponseModel createPermission(PermissionModel permissionModel);

	ResponseModel createPermissionRoleMapping(PermissionRoleMappingModel permissionRoleMappingModel);

	ResponseModel createClientRoleMapping(ClientRoleModel clientRoleModel);

	ResponseModel updateRole(RoleModel roleModel);

	ResponseModel updatePermission(PermissionModel permissionModel);

	ResponseModel updatePermissionRoleMapping(PermissionRoleMappingModel permissionRoleMappingModel);

	ResponseModel updateClientRoleMapping(ClientRoleModel clientRoleModel);

	ResponseModel getRole();

	ResponseModel getPermission();

	ResponseModel getPermissionRoleMapping();

	ResponseModel getClientRoleMapping();

}

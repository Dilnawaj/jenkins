package com.codewithmd.blogger.bloggerappsapis.services.impl;

import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.codewithmd.blogger.bloggerappsapis.account.entity.ClientRole;
import com.codewithmd.blogger.bloggerappsapis.account.entity.Permission;
import com.codewithmd.blogger.bloggerappsapis.account.entity.PermissionRoleMapping;
import com.codewithmd.blogger.bloggerappsapis.account.entity.Role;
import com.codewithmd.blogger.bloggerappsapis.account.model.ClientRoleModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionRoleMappingModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.RoleModel;
import com.codewithmd.blogger.bloggerappsapis.account.repo.ClientRoleRepo;
import com.codewithmd.blogger.bloggerappsapis.account.repo.PermissionRepo;
import com.codewithmd.blogger.bloggerappsapis.account.repo.PermissionRoleMappingRepo;
import com.codewithmd.blogger.bloggerappsapis.account.repo.RoleRepo;
import com.codewithmd.blogger.bloggerappsapis.config.ErrorConfig;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.RoleAndPermissionService;

@Service
public class RoleAndPermissionServiceImpl implements RoleAndPermissionService {

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private PermissionRepo permissionRepo;

	@Autowired
	private PermissionRoleMappingRepo permissionRoleMappingRepo;

	@Autowired
	private ClientRoleRepo clientRoleRepo;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ResponseModel createRole(RoleModel roleModel) {
		try {
			Role role = this.modelMapper.map(roleModel, Role.class);
			role.setIsDefault(roleModel.isDefault());
			role.setIsEditable(roleModel.isEditable());
			this.roleRepo.save(role);
			return new ResponseModel(ErrorConfig.addMessage("Role"), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("createRole", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel updateRole(RoleModel roleModel) {
		try {
			Optional<Role> roleOptional = roleRepo.findById(roleModel.getRoleId());
			if (roleOptional.isPresent()) {
				Role role = roleOptional.get();
				role.setName(roleModel.getRoleName());
				role.setUserType(roleModel.getUserType());
				this.roleRepo.save(role);
				return new ResponseModel(ErrorConfig.updateMessage("Role"), HttpStatus.ACCEPTED);
			} else {
				return new ResponseModel(ErrorConfig.updateError("Role"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("updateRole ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel getRole() {
		ResponseModel responseModel = new ResponseModel();
		responseModel.setResponse(roleRepo.findAll());
		responseModel.setResponseCode(HttpStatus.OK);
		return responseModel;
	}

	public ResponseModel createPermission(PermissionModel permissionModel) {
		try {
			Permission permission = this.modelMapper.map(permissionModel, Permission.class);
			this.permissionRepo.save(permission);
			return new ResponseModel(ErrorConfig.addMessage("Permission"), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("createPermission ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel updatePermission(PermissionModel permissionModel) {
		try {
			Optional<Permission> permissionOptional = permissionRepo.findById(permissionModel.getId());
			if (permissionOptional.isPresent()) {
				Permission permission = permissionOptional.get();
				permission.setCode(permissionModel.getCode());
				permission.setName(permissionModel.getName());
				this.permissionRepo.save(permission);
				return new ResponseModel(ErrorConfig.updateMessage("Permission"), HttpStatus.ACCEPTED);
			} else {
				return new ResponseModel(ErrorConfig.updateError("Permission"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("updatePermission ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel getPermission() {
		ResponseModel responseModel = new ResponseModel();
		responseModel.setResponse(permissionRepo.findAll());
		responseModel.setResponseCode(HttpStatus.OK);
		return responseModel;
	}

	public ResponseModel createPermissionRoleMapping(PermissionRoleMappingModel permissionRoleMappingModel) {
		try {
			PermissionRoleMapping permissionRoleMapping = this.modelMapper.map(permissionRoleMappingModel,
					PermissionRoleMapping.class);
			this.permissionRoleMappingRepo.save(permissionRoleMapping);
			return new ResponseModel(ErrorConfig.addMessage("PermissionRoleMapping"), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("createPermissionRoleMapping ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel updatePermissionRoleMapping(PermissionRoleMappingModel permissionRoleMappingModel) {
		try {
			Optional<PermissionRoleMapping> permissionRoleMappingOptional = permissionRoleMappingRepo
					.findById(permissionRoleMappingModel.getId());
			if (permissionRoleMappingOptional.isPresent()) {
				PermissionRoleMapping permissionRoleMapping = permissionRoleMappingOptional.get();
				permissionRoleMapping.setPermissionRecordId(permissionRoleMappingModel.getPermissionId());
				permissionRoleMapping.setRoleId(permissionRoleMappingModel.getRoleId());
				this.permissionRoleMappingRepo.save(permissionRoleMapping);
				return new ResponseModel(ErrorConfig.updateMessage("PermissionRoleMapping"), HttpStatus.ACCEPTED);
			} else {
				return new ResponseModel(ErrorConfig.updateError("PermissionRoleMapping"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("updatePermissionRoleMapping ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel getPermissionRoleMapping() {
		ResponseModel responseModel = new ResponseModel();
		responseModel.setResponse(permissionRoleMappingRepo.findAll());
		responseModel.setResponseCode(HttpStatus.OK);
		return responseModel;
	}

	public ResponseModel createClientRoleMapping(ClientRoleModel clientRoleModel) {
		try {
			ClientRole clientRole = this.modelMapper.map(clientRoleModel, ClientRole.class);
			this.clientRoleRepo.save(clientRole);
			return new ResponseModel(ErrorConfig.addMessage("ClientRoleMapping"), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("createClientRoleMapping ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel updateClientRoleMapping(ClientRoleModel clientRoleModel) {
		try {
			Optional<ClientRole> clientRoleOptional = clientRoleRepo.findById(clientRoleModel.getId());
			if (clientRoleOptional.isPresent()) {
				ClientRole clientRole = clientRoleOptional.get();
				clientRole.setClientId(clientRoleModel.getClientId());
				clientRole.setRoleId(clientRoleModel.getRoleId());
				this.clientRoleRepo.save(clientRole);
				return new ResponseModel(ErrorConfig.updateMessage("ClientRole"), HttpStatus.ACCEPTED);
			} else {
				return new ResponseModel(ErrorConfig.updateError("ClientRole"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("updateClientRoleMapping", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel getClientRoleMapping() {
		ResponseModel responseModel = new ResponseModel();
		responseModel.setResponse(clientRoleRepo.findAll());
		responseModel.setResponseCode(HttpStatus.OK);
		return responseModel;
	}

}

package com.codewithmd.blogger.bloggerappsapis.account.service;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codewithmd.blogger.bloggerappsapis.account.entity.Permission;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionModel;
import com.codewithmd.blogger.bloggerappsapis.account.repo.PermissionRepo;
import com.codewithmd.blogger.bloggerappsapis.helper.ErrorConfig;

@Service
public class PermissionService {

	@Autowired
	ErrorConfig message;

	@Autowired
	PermissionRepo permissionRepo;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static List<PermissionModel> permissions = null;

	private static List<PermissionModel> getPermissions() {
		return permissions;
	}

	private static void setPermissions(List<PermissionModel> permissions) {
		PermissionService.permissions = permissions;
	}

	public List<PermissionModel> getAllPermissions() {
		/*
		 * read from local static variable if that is null, then set it and return it
		 */
		try {
			List<PermissionModel> list = new ArrayList<>();
			if (PermissionService.getPermissions() == null) {
				List<Permission> entities = permissionRepo.findAll();
				PermissionModel model;
				for (Permission entity : entities) {
					model = new PermissionModel(entity.getId(), entity.getName(), entity.getCode());
					list.add(model);
				}
				PermissionService.setPermissions(list);
			}
		} catch (Exception e) {
			logger.error("getAllPermissions", e);
		}
		return PermissionService.getPermissions();
	}

	public String updatePermission(InputStream inputStream) {
		try {
			PermissionModel model = getPermissionRecordModelFromInput(inputStream);
			if (model == null) {
				return message.getInvalidInput();
			}
			Permission permission = null;
			if (model.getId() == 0) {

				permission = new Permission(0, model.getName(), model.getCode());
				if (isDuplicatePermissionName(permission)) {
					return message.getDuplicateError("Permission Name");
				}
				if (isDuplicatePermissionCode(permission)) {
					return message.getDuplicateError("Permission Code");
				}
				permissionRepo.save(permission);
				PermissionService.setPermissions(null);
				return message.getAddSuccess("Permission");
			} else {
				Optional<Permission> permissionOptional = permissionRepo.findById(model.getId());
				permission = permissionOptional.get();
				if (permission != null) {
					permission.setName(model.getName());
					permission.setCode(model.getCode());
					if (isDuplicatePermissionName(permission)) {
						return message.getDuplicateError("Permission Name");
					}
					if (isDuplicatePermissionCode(permission)) {
						return message.getDuplicateError("Permission Code");
					}
					permissionRepo.save(permission);
					PermissionService.setPermissions(null);
					return message.getUpdateSuccess("Permission");
				}
			}
		} catch (Exception e) {
			logger.error("updatePermission", e);
		}
		return message.getUnknowError();
	}

	public PermissionModel getPermissionRecordModelFromInput(InputStream inputStream) {

		try {
			StringBuilder inputData = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line = null;
			while ((line = in.readLine()) != null) {
				inputData.append(line);
			}
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(inputData.toString());
			String[] keys = { "id", "name", "code" };
			PermissionModel model = new PermissionModel();
			for (String key : keys) {
				Object value = json.get(key);
				if (value == null) {
					return null;
				} else if (key.equals("id")) {
					model.setId(Integer.parseInt(value.toString()));
				} else if (key.equals("name")) {
					model.setName(value.toString().trim().replace(" ", "_"));
				} else if (key.equals("code")) {
					model.setCode(value.toString().trim());
				}
			}
			if (!model.getName().isEmpty() && !model.getCode().isEmpty()) {
				return model;
			}
		} catch (Exception e) {
			logger.error("getPermissionRecordModelFromInput", e);
		}
		return null;
	}

	private boolean isDuplicatePermissionName(Permission newPermission) {
		try {
			Permission existingPermissionWithSameName = permissionRepo.findByName(newPermission.getName());
			if (existingPermissionWithSameName == null) {
				return false;
			} else if (existingPermissionWithSameName.getId() != newPermission.getId()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("isDuplicatePermissionName", e);
		}
		return false;
	}

	private boolean isDuplicatePermissionCode(Permission newPermission) {
		try {

			Permission existingPermissionWithSameCode = permissionRepo.findByCode(newPermission.getCode());
			if (existingPermissionWithSameCode == null) {
				return false;
			} else if (existingPermissionWithSameCode.getId() != newPermission.getId()) {
				return true;
			}

		} catch (Exception e) {
			logger.error("isDuplicatePermissionCode", e);
		}
		return false;
	}

}

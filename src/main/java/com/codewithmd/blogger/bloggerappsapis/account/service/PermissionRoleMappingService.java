package com.codewithmd.blogger.bloggerappsapis.account.service;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codewithmd.blogger.bloggerappsapis.account.entity.PermissionRoleMapping;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionRoleMappingModel;
import com.codewithmd.blogger.bloggerappsapis.account.repo.PermissionRoleMappingRepo;

@Service
public class PermissionRoleMappingService {

	@Autowired
	private PermissionRoleMappingRepo permissionRoleMappingRepo;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static List<PermissionRoleMappingModel> permissionRoleMappings = null;

	private static List<PermissionRoleMappingModel> getPermissionRoleMappings() {
		return permissionRoleMappings;
	}

	private static void setPermissionRoleMappings(List<PermissionRoleMappingModel> permissionRoleMappings) {
		PermissionRoleMappingService.permissionRoleMappings = permissionRoleMappings;
	}

	public List<PermissionRoleMappingModel> getAllPermissionRoleMappings() {

		try {
			if (PermissionRoleMappingService.getPermissionRoleMappings() == null) {
				List<PermissionRoleMappingModel> list = new ArrayList<>();
				List<PermissionRoleMapping> entities = permissionRoleMappingRepo.findAll();
				PermissionRoleMappingModel model;
				for (PermissionRoleMapping entity : entities) {
					model = new PermissionRoleMappingModel(true, entity.getId(), entity.getRoleId(),
							entity.getPermissionRecordId());
					list.add(model);
				}
				PermissionRoleMappingService.setPermissionRoleMappings(list);
			}
		} catch (Exception e) {
			logger.error("getAllPermissionRoleMappings", e);
		}
		return PermissionRoleMappingService.getPermissionRoleMappings();
	}

	public List<PermissionRoleMappingModel> getPermissionRoleModelFromInput(InputStream inputStream) {

		List<PermissionRoleMappingModel> list = new ArrayList<>();
		PermissionRoleMappingModel model;
		try {
			StringBuilder inputData = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line = null;
			while ((line = in.readLine()) != null) {
				inputData.append(line);
			}
			JSONParser parser = new JSONParser();
			JSONArray rows = (JSONArray) parser.parse(inputData.toString());
			String[] keys = { "permissionAllowed", "roleId", "permissionId" };
			for (int i = 0; i < rows.size(); i++) {
				JSONArray row = (JSONArray) rows.get(i);
				for (int j = 0; j < row.size(); j++) {
					model = new PermissionRoleMappingModel();
					JSONObject jsonObject = (JSONObject) row.get(j);
					for (String key : keys) {
						Object value = jsonObject.get(key);
						if (value == null) {
							return new ArrayList<>();
						} else if (key.equals("permissionAllowed")) {
							model.setPermissionAllowed(Boolean.parseBoolean(value.toString()));
						} else if (key.equals("roleId")) {
							model.setRoleId(Integer.parseInt(value.toString()));
						} else if (key.equals("permissionId")) {
							model.setPermissionId(Integer.parseInt(value.toString()));
						}
					}
					list.add(model);
				}
			}
		} catch (Exception e) {
			logger.error("getPermissionRecordModelFromInput", e);
			list = new ArrayList<>();
		}
		return list;
	}
}

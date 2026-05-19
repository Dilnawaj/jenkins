package com.codewithmd.blogger.bloggerappsapis.admin.controllers;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codewithmd.blogger.bloggerappsapis.account.model.LoginModel;
import com.codewithmd.blogger.bloggerappsapis.admin.payloads.AdminDto;
import com.codewithmd.blogger.bloggerappsapis.admin.services.AdminService;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;

@RestController
@CrossOrigin
@RequestMapping("admin/account")
@Validated
public class AdminController {

	@Autowired
	private AdminService adminService;

	
	@PostMapping(value = "/signup", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createUser(@RequestBody AdminDto adminDto) {
		ResponseModel createUser = this.adminService.createAdmin(adminDto);
		return new ResponseEntity<>(createUser.getResponse(), createUser.getResponseCode());
	}
	@GetMapping(value = "/googlesignupprocess/{code}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> googlesignup(@PathVariable String code) throws GeneralSecurityException, IOException {

		ResponseModel response = this.adminService.googleSignUp(code);

		return new ResponseEntity<>(response.getResponse(), response.getResponseCode());
	}
	@PostMapping(value = "/forgot", produces = "application/json; charset=utf-8", consumes = "application/json")
	public ResponseEntity<Object> forgotPassword(@RequestBody LoginModel loginModel) {
		ResponseModel responseModel = adminService.forgotPassword(loginModel);
		return new ResponseEntity<>(responseModel.getResponse(), responseModel.getResponseCode());
	}

	@PostMapping(value = "/login", produces = "application/json; charset=utf-8", consumes = "application/json")
	public ResponseEntity<Object> login(@Valid @RequestBody LoginModel loginModel) {
		ResponseModel responseModel = adminService.getLoginModel(loginModel, true);
		return new ResponseEntity<>(responseModel.getResponse(), responseModel.getResponseCode());
	}

	@GetMapping(value = "/permission", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> grantAdminAccess(@RequestParam String email) {
		ResponseModel adminAccess = this.adminService.grantAdminAccess(email);
		return new ResponseEntity<>(adminAccess.getResponse(), adminAccess.getResponseCode());

	}
	@GetMapping(value = "/google/login/{code}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> googleLogin(@PathVariable String code) throws GeneralSecurityException, IOException {
		String email = adminService.getEmailFromGoogleAccessToken(code);
		ResponseModel responseModel = adminService.getLoginModel(email, false);
		return new ResponseEntity<>(responseModel.getResponse(), responseModel.getResponseCode());
	}

}

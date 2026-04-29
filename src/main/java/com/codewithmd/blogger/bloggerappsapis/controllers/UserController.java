package com.codewithmd.blogger.bloggerappsapis.controllers;

import java.io.IOException;


import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.codewithmd.blogger.bloggerappsapis.config.ErrorConfig;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.UserDto;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.FileService;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.UserService;

@RestController
@RequestMapping("/user")
@Validated
@CrossOrigin
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	@Value("${project.image}")
	private String path;

	// POST-> create User

	@PostMapping(value = "/account/signup", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createUser(@RequestBody UserDto userDto) {
		ResponseModel createUser = this.userService.createUser(userDto);
		return new ResponseEntity<>(createUser.getResponse(), createUser.getResponseCode());
	}

	@GetMapping(value = "/account/googlesignupprocess/{code}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> googlesignup(@PathVariable String code) throws GeneralSecurityException, IOException {

		ResponseModel response = this.userService.googleSignUp(code);

		return new ResponseEntity<>(response.getResponse(), response.getResponseCode());
	}

	// PUT ->Update User
	@PutMapping(produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> updateUser(@RequestBody UserDto userDto) {
		ResponseModel updateUser = this.userService.updateUser(userDto);
		Integer a=10;
		a.longValue();
		a.toString();
	
		return new ResponseEntity<>(updateUser.getResponse(), updateUser.getResponseCode());
	}

	// DELETE -> delete User

	@DeleteMapping(value = "/{id}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> deleteUser(@PathVariable Integer id) {
		ResponseModel deleteUser = this.userService.deleteUser(id);
		return new ResponseEntity<>(deleteUser.getResponse(), deleteUser.getResponseCode());
	}

	// GET -> user Get
	@GetMapping("/account/{id}")
	public ResponseEntity<Object> getOne(@PathVariable Integer id) {
		ResponseObjectModel getOneUserDto = this.userService.getUserById(id);
		return new ResponseEntity<>(getOneUserDto.getResponse(), getOneUserDto.getResponseCode());
	}

	// GET -> user All
	@GetMapping
	public ResponseEntity<Object> getAllUser() {
		ResponseObjectModel allUser = this.userService.getAllUsers();
		return new ResponseEntity<>(allUser.getResponse(), allUser.getResponseCode());
	}

	@GetMapping("/real")
	public ResponseEntity<Object> getAllRealUser() {
		ResponseObjectModel allUser = this.userService.getAllRealUsers();
		return new ResponseEntity<>(allUser.getResponse(), allUser.getResponseCode());
	}

	@CrossOrigin
	@PostMapping(value = "/image/upload/{userId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> uploadImage(@RequestParam("image") MultipartFile image, @PathVariable Integer userId,
			@PathVariable(value = "imageName", required = false) String imageName) {
		String fileName = null;
		try {
			ResponseObjectModel responseObjectModel = this.userService.getUserById(userId);
			fileName = this.fileService.uploadImage(path, image, userId, imageName);
			UserDto userDto = (UserDto) responseObjectModel.getResponse();
			userDto.setImageName(fileName);
			this.userService.updateUser(userDto);
			return new ResponseEntity<>(ErrorConfig.updateMessage("Profile Image"), HttpStatus.OK);
		} catch (Exception e) {

			return new ResponseEntity<>(ErrorConfig.unknownError(), HttpStatus.OK);
		}
	}

}

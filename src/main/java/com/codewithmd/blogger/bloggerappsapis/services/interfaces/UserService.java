package com.codewithmd.blogger.bloggerappsapis.services.interfaces;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.UserDto;

public interface UserService {

	ResponseModel createUser(UserDto user);

	ResponseModel updateUser(UserDto user);

	ResponseObjectModel getUserById(Integer userId);

	ResponseObjectModel getAllUsers();
	
	ResponseObjectModel getAllRealUsers();

	ResponseModel deleteUser(Integer userId);

	ResponseModel googleSignUp(String code) throws GeneralSecurityException, IOException;

}

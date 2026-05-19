package com.codewithmd.blogger.bloggerappsapis.admin.services;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.validation.Valid;

import com.codewithmd.blogger.bloggerappsapis.account.model.LoginModel;
import com.codewithmd.blogger.bloggerappsapis.admin.payloads.AdminDto;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;

public interface AdminService {

	ResponseModel createAdmin(AdminDto adminDto);

	ResponseModel googleSignUp(String code) throws GeneralSecurityException, IOException;

	ResponseModel getLoginModel(@Valid LoginModel loginModel, boolean b);

	ResponseModel forgotPassword(LoginModel loginModel);

	ResponseModel grantAdminAccess(String emailAddress);

	String getEmailFromGoogleAccessToken(String code) throws GeneralSecurityException, IOException;

	ResponseModel getLoginModel(String email, boolean b);
}

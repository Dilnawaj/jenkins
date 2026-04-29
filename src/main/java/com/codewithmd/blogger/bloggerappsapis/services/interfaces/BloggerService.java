package com.codewithmd.blogger.bloggerappsapis.services.interfaces;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;


import javax.validation.Valid;

import com.codewithmd.blogger.bloggerappsapis.account.model.LoginModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.payloads.HelpCenterDto;


public interface BloggerService {

	ResponseModel getLoginModel( LoginModel loginModel, boolean validatePassword);

	ResponseModel resetPassword( LoginModel loginModel) throws ParseException;

	ResponseModel forgotPassword(LoginModel loginModel);

	ResponseModel getUpdatePassword(@Valid LoginModel loginModel);

	void helpCenter(HelpCenterDto helpCenter);

	String getEmailFromGoogleAccessToken(String code) throws GeneralSecurityException, IOException;

	ResponseModel getLoginModel(String email, boolean validatePassword);

	ResponseModel updatePasswordAlert(Integer userId);

}

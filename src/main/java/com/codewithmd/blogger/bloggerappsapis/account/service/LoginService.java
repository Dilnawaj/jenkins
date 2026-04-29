package com.codewithmd.blogger.bloggerappsapis.account.service;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.codewithmd.blogger.bloggerappsapis.account.entity.LoginHistory;
import com.codewithmd.blogger.bloggerappsapis.account.model.AccessTokenModel;
import com.codewithmd.blogger.bloggerappsapis.account.repo.LoginHistoryRepo;
import com.codewithmd.blogger.bloggerappsapis.helper.JavaHelper;



@Service
public class LoginService {



	
	@Autowired
	JWTService jwtService;

	@Autowired
	private LoginHistoryRepo loginHistoryRepo;


	public void loginHistory(Long userId, Long companyId, boolean impersonate, String name) {
		LoginHistory newRecord = new LoginHistory();
		newRecord.setUserId(userId);
		Date currentDate = new Date();;
		newRecord.setTime(currentDate);
		newRecord.setMonth(JavaHelper.getMonth(currentDate));
		newRecord.setYear(JavaHelper.getYear(currentDate));
		newRecord.setWeekOfMonth(JavaHelper.getWeekOfMonth(currentDate));
		newRecord.setImpersonate(impersonate);
		newRecord.setName(name);
		loginHistoryRepo.save(newRecord);
	}

//	/**
//	 * <p>
//	 * This method returns the new AccessToken and refreshToken
//	 * 
//	 * @param accessToken
//	 * @param refreshToken
//	 * @return LoginResponseModel
//	 */

	public AccessTokenModel getAccessTokenModel(String accessToken) {
		String subject = jwtService.getSubject(accessToken);
		return new AccessTokenModel(Long.parseLong(subject.split("#")[0]));
	}

//

}

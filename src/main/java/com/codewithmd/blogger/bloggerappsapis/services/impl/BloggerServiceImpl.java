package com.codewithmd.blogger.bloggerappsapis.services.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.codewithmd.blogger.bloggerappsapis.account.model.RoleModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.UserType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.codewithmd.blogger.bloggerappsapis.account.entity.ClientRole;
import com.codewithmd.blogger.bloggerappsapis.account.entity.LoginHistory;
import com.codewithmd.blogger.bloggerappsapis.account.entity.Role;
import com.codewithmd.blogger.bloggerappsapis.account.model.BloggerLoginModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.LoginModel;
import com.codewithmd.blogger.bloggerappsapis.account.repo.ClientRoleRepo;
import com.codewithmd.blogger.bloggerappsapis.account.repo.LoginHistoryRepo;
import com.codewithmd.blogger.bloggerappsapis.account.repo.RoleRepo;
import com.codewithmd.blogger.bloggerappsapis.account.service.JWTService;
import com.codewithmd.blogger.bloggerappsapis.entities.HelpCenter;
import com.codewithmd.blogger.bloggerappsapis.entities.User;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.helper.EmailService;
import com.codewithmd.blogger.bloggerappsapis.helper.EncryptionUtils;
import com.codewithmd.blogger.bloggerappsapis.helper.JavaHelper;
import com.codewithmd.blogger.bloggerappsapis.payloads.HelpCenterDto;
import com.codewithmd.blogger.bloggerappsapis.payloads.UserDto;
import com.codewithmd.blogger.bloggerappsapis.repos.HelpCenterRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.UserRepo;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.BloggerService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class BloggerServiceImpl implements BloggerService {

	@Autowired
	private JWTService jwtService;

	@Autowired
	private LoginHistoryRepo loginHistoryRepo;

	@Value("${jwt.accessTokenExpiryTime}")
	private long accessTokenExpiryTime;

	@Value("${jwt.refreshTokenExpiryTime}")
	private long refreshTokenExpiryTime;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private EmailService emailService;
	@Lazy
	@Autowired
	private UserServieImpl userService;

	@Autowired
	private ClientRoleRepo clientRoleRepo;

	@Autowired
	private HelpCenterRepo helpCenterRepo;

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Value("${clientId}")
	private String clientId;

	/**
	 * this method will validate user details and return token
	 * 
	 * @param req this will consist login detail of user.
	 * @see LoginModel
	 * @return this method will return access-token and refresh-token with validity
	 *         time second.
	 */
	public ResponseModel getLoginModel(LoginModel req, boolean validatePassword) {

		try {
			if (req.checkValidationForLogin() || !validatePassword) {
				String email = req.getEmail().toLowerCase().trim();
				Optional<User> userOptional = userRepo.findByEmail(email);
				if (userOptional.isEmpty() || req.getEmail().isEmpty()) {
					return new ResponseModel("User not found ", HttpStatus.BAD_REQUEST, true);
				} else {
					User user = userOptional.get();
					ClientRole clientRole = clientRoleRepo.findByClientId(user.getId().longValue());

					if (clientRole.getRoleId() != 1) {
						return new ResponseModel("Permission denied", HttpStatus.BAD_REQUEST, true);
					}
					if (user.isSuspendUser()) {
						return new ResponseModel("your account has been suspended due to inappropriate behavior",
								HttpStatus.BAD_REQUEST, true);
					} else if ((!user.isPasswordSet() && validatePassword)
							|| (!(JavaHelper.checkPassword(req.getPassword(),
									EncryptionUtils.decrypt(user.getPassword()), validatePassword)))) {
						return new ResponseModel("Invalid credentials", HttpStatus.BAD_REQUEST, true);

					} else {


						if (!user.isPasswordSet()) {

							user.setGoogleLoginCount(user.getGoogleLoginCount() + 1);
							userRepo.save(user);
						}
						return getLoginModel(user, req.getRememberMe());
					}
				}
			}
		} catch (Exception e) {
			return new ResponseModel("Unexpected error occured", HttpStatus.BAD_REQUEST, true);
		}
		return new ResponseModel("Required parameter missing.", HttpStatus.BAD_REQUEST, true);
	}

	public ResponseModel getLoginModel(User user, Boolean rememberMe) {
		BloggerLoginModel loginModel = new BloggerLoginModel();
		try {
			String accessToken = jwtService.getAccessToken(user.getId().longValue(), null, new ArrayList<>());
			String refreshToken = jwtService.getRefreshToken(user.getId().longValue(), null, rememberMe);
			loginModel.setAccessTokenValidity(accessTokenExpiryTime * 60);
			loginModel.setRefreshTokenValidity(refreshTokenExpiryTime * 60);
			loginModel.setAccessToken(accessToken);
			UserDto userDto = userService.usertoDto(user);

			ClientRole clientRole = clientRoleRepo.findByClientId(Long.valueOf(userDto.getId()));
			Role role = roleRepo.findByRoleId(clientRole.getRoleId());
			userDto.setRole(modelMapper.map(role, RoleModel.class));
			userDto.setPassword(null);
			loginModel.setUser(userDto);
			loginModel.setRefreshToken(refreshToken);
			loginHistorySaveData(user);
			return new ResponseModel(loginModel, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseModel("Unexpected error occured", HttpStatus.BAD_REQUEST, true);

		}
	}

	private void loginHistorySaveData(User user) {
		LoginHistory newRecord = new LoginHistory();
		newRecord.setUserId(user.getId());
		Date currentDate = JavaHelper.getCurrentDate();
		newRecord.setTime(currentDate);
		newRecord.setMonth(JavaHelper.getMonth(currentDate));
		newRecord.setYear(JavaHelper.getYear(currentDate));
		newRecord.setWeekOfMonth(JavaHelper.getWeekOfMonth(currentDate));
		newRecord.setImpersonate(true);
		newRecord.setName(user.getName());
		loginHistoryRepo.save(newRecord);
	}

	public ResponseModel resetPassword(LoginModel loginModel) throws ParseException {

		if (loginModel.checkValidationForPassword()) {
			List<User> users = new ArrayList<>();
			Date currentDate = JavaHelper.getCurrentDate();
			if (loginModel.getCode().equals("")) {
				User user = userRepo.findByEmail(loginModel.getEmail()).get();
				user.setLinkExpiryDate(JavaHelper.getCurrentDate().toString());
				users.add(user);

			} else {
				users = userRepo.findByVerificationCode(loginModel.getCode());
			}

			if (users.size() == 1) {
				User user = users.get(0);
				Date expiryDate = JavaHelper.dateStringToDate(user.getLinkExpiryDate());
				Integer minutes = JavaHelper.getDiffInMinutes(expiryDate, currentDate);
				if (minutes <= 15) {
					user.setPassword(EncryptionUtils.encrypt(loginModel.getPassword()));
					user.setVerificationCode(null);
					user.setPasswordSet(true);
					userRepo.save(user);
					return new ResponseModel("", HttpStatus.OK);
				}
				return new ResponseModel("Link is expire.Please try again.", HttpStatus.BAD_REQUEST, true);
			}

			return new ResponseModel("The link has already been used. Please try to reset your password again.",
					HttpStatus.BAD_REQUEST, true);

		}
		return new ResponseModel("Invalid credentials", HttpStatus.BAD_REQUEST, true);
	}

	public ResponseModel forgotPassword(LoginModel loginModel) {
		Optional<User> userOptional = userRepo.findByDobAndEmail(loginModel.getDob(), loginModel.getEmail());
		if (userOptional.isPresent() && !"".equals(JavaHelper.checkStringValue(userOptional.get().getPassword()))) {
			User user = userOptional.get();
			ClientRole clientRole=	clientRoleRepo.findByClientId(user.getId().longValue());
			
			if(clientRole.getRoleId()!=1)
			{
				return new ResponseModel("Permission denied",
						HttpStatus.BAD_REQUEST, true);
			}
			if (Boolean.FALSE.equals(emailService.sendEmailForReset(user))) {
				return new ResponseModel("An unknown error occurred.Please try again later.", HttpStatus.BAD_REQUEST,
						true);
			}
			user.setLinkExpiryDate(JavaHelper.getCurrentDate().toString());
			userRepo.save(user);
			return new ResponseModel("", HttpStatus.OK, false);
		}
		return new ResponseModel("Invalid credentials", HttpStatus.BAD_REQUEST, true);
	}

	@Override
	public ResponseModel getUpdatePassword(LoginModel loginModel) {

		if (loginModel.checkValidationForUpdatePassword()) {
			Optional<User> user = userRepo.findByEmail(loginModel.getEmail());
			if (user.isPresent()) {

				if (loginModel.getPassword().equals(user.get().getPassword())) {
					user.get().setPassword(EncryptionUtils.encrypt(loginModel.getNewPassword()));
					userRepo.save(user.get());
					return new ResponseModel("Password updated successfully", HttpStatus.OK, false);
				}
				return new ResponseModel("Wrong password entered", HttpStatus.BAD_REQUEST, true);
			}
			return new ResponseModel("Invalid User", HttpStatus.BAD_REQUEST, true);
		}
		return new ResponseModel("Invalid credentials", HttpStatus.BAD_REQUEST, true);
	}

	public void helpCenter(HelpCenterDto helpCenterDto) {
		HelpCenter helperCenter = new HelpCenter();
		helperCenter.setUserId(helpCenterDto.getUserId());
		helperCenter.setSubject(helpCenterDto.getSubject());
		helperCenter.setDescription(helpCenterDto.getDescription());
		helperCenter.setTicketId(getSixDigitRandomNumber());
		helpCenterRepo.save(helperCenter);
		Optional<User> user = userRepo.findById(helpCenterDto.getUserId());
		if (user.isPresent()) {
			String name = user.get().getName();
			String email = user.get().getEmail();
			emailService.sendTicketRecieveEmail(name, email, helperCenter);

		}

		}

	public static int getSixDigitRandomNumber() {
		try {
			SecureRandom secureRandom = new SecureRandom();
			int randomNumber = secureRandom.nextInt(900000) + 100000;
			return randomNumber;
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	public String getEmailFromGoogleAccessToken(String code) throws GeneralSecurityException, IOException {
		// 30-June-2022 @manish get id token from credential
		// first verify credential code with google then it will return us id Token.
		// id token contain all user information.
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
				// Specify the CLIENT_ID
				.setAudience(Collections.singletonList(clientId))
				// Or, if multiple clients access then:
				// .setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
				.build();
		// verify google jwt code
		GoogleIdToken idToken = verifier.verify(code);
		if (idToken != null) {
			// get user details from payload
			Payload payload = idToken.getPayload();
			return payload.getEmail();
		}
		return "";
	}

	@Override
	public ResponseModel getLoginModel(String email, boolean validatePassword) {
		LoginModel loginModel = new LoginModel();
		loginModel.setEmail(email);
		loginModel.setRememberMe(true);
		return getLoginModel(loginModel, validatePassword);
	}

	public ResponseModel updatePasswordAlert(Integer userId) {
		Optional<User> userOpt = userRepo.findById(userId);
		if (userOpt.isPresent()) {
			User user = userOpt.get();
			if (!user.isPasswordSet()) {

				if (user.getGoogleLoginCount() >= 5) {
					user.setGoogleLoginCount(0);
					userRepo.save(user);
					return new ResponseModel("successs", HttpStatus.OK);
				}
				return new ResponseModel("false", HttpStatus.BAD_REQUEST);
			}
			return new ResponseModel("false", HttpStatus.BAD_REQUEST);

		}
		return new ResponseModel("false", HttpStatus.BAD_REQUEST);

	}

}

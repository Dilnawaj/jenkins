package com.codewithmd.blogger.bloggerappsapis.admin.services.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

import javax.transaction.Transactional;

import com.codewithmd.blogger.bloggerappsapis.payloads.UserDto;
import com.codewithmd.blogger.bloggerappsapis.services.impl.UserServieImpl;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.codewithmd.blogger.bloggerappsapis.account.entity.ClientRole;
import com.codewithmd.blogger.bloggerappsapis.account.entity.Role;
import com.codewithmd.blogger.bloggerappsapis.account.model.LoginModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.UserType;
import com.codewithmd.blogger.bloggerappsapis.account.repo.ClientRoleRepo;
import com.codewithmd.blogger.bloggerappsapis.account.repo.RoleRepo;
import com.codewithmd.blogger.bloggerappsapis.account.service.ClientRoleService;
import com.codewithmd.blogger.bloggerappsapis.admin.payloads.AdminDto;
import com.codewithmd.blogger.bloggerappsapis.admin.services.AdminService;
import com.codewithmd.blogger.bloggerappsapis.config.ErrorConfig;
import com.codewithmd.blogger.bloggerappsapis.entities.User;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.helper.EmailService;
import com.codewithmd.blogger.bloggerappsapis.helper.EncryptionUtils;
import com.codewithmd.blogger.bloggerappsapis.helper.JavaHelper;
import com.codewithmd.blogger.bloggerappsapis.repos.UserRepo;
import com.codewithmd.blogger.bloggerappsapis.services.impl.BloggerServiceImpl;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private ClientRoleService clientRoleService;
	@Autowired
	private ClientRoleRepo clientRoleRepo;
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private RoleRepo roleRepo;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private UserServieImpl userServieImpl;

	@Autowired
	private UserRepo userRepo;
	@Autowired
	private BloggerServiceImpl bloggerServiceImpl;
	
	@Value("${clientId}")
	private String clientId;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Transactional
	public ResponseModel createAdmin(AdminDto adminDto) {
       UserDto userDto=this.modelMapper.map(adminDto, UserDto.class);
        userDto.setId(adminIdGenerator());
		userDto.setUserType(UserType.ADMIN.toString());
		userDto.setPassword(EncryptionUtils.encrypt(adminDto.getPassword()));
		userDto.setAbout("I am an Admin");
		return createAdmin(userDto);
	}


	public Role getByUserType(UserType userType) {

		return roleRepo.findByUserType(userType.toString());
	}


		@Override
		public ResponseModel googleSignUp(String code) throws GeneralSecurityException, IOException {
			if (code != null) {
				// 30-June-2022 @manish get id token from credential
				// first verify credential code with google then it will return us id Token.
				// id token contain all user information.
				GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
						new GsonFactory())
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
					String userEmail = payload.getEmail();
					String name = (String) payload.get("name");
					if (userEmail != null && !userEmail.isEmpty() && !userEmail.equals("")) {
						AdminDto adminDto = new AdminDto();
						adminDto.setEmail(userEmail);
						adminDto.setName(name);
						adminDto.setPassword(EncryptionUtils.encrypt(JavaHelper.generateRandomPassword(10, 97, 122)));
						adminDto.setAbout("Google Admin");
						return createAdmin(adminDto);

					}
				}
			}
			return new ResponseModel("Error occur while user signup", HttpStatus.BAD_REQUEST, true);
		}
		public ResponseModel forgotPassword(LoginModel loginModel) {
			Optional<User> userOptional = userRepo.findByDobAndEmail(loginModel.getDob(), loginModel.getEmail());
			if (userOptional.isPresent() && !"".equals(JavaHelper.checkStringValue(userOptional.get().getPassword()))) {
				User user = userOptional.get();
				ClientRole clientRole=	clientRoleRepo.findByClientId(user.getId().longValue());
				
				if(clientRole.getRoleId()!=100)
				{
					return new ResponseModel("Permission denied",
							HttpStatus.BAD_REQUEST, true);
				}
				if (Boolean.FALSE.equals(emailService.sendEmailForResetForAdmin(user))) {
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
		public ResponseModel grantAdminAccess(String emailAddress) {
			
			String email = emailAddress.toLowerCase().trim();
			Optional<User> userOptional = userRepo.findByEmail(email);
			
			if (userOptional.isEmpty()) {
				return new ResponseModel("User not found ", HttpStatus.BAD_REQUEST, true);
			} else {
				User user =userOptional.get();
				
				user.setPasswordSet(true);
			userRepo.save(user);
			emailService.sendEmailToClientForAdminRole(user);
			}

			return new ResponseModel("Admin role granted", HttpStatus.OK, false);
		}

	@Override
	public String getEmailFromGoogleAccessToken(String code) throws GeneralSecurityException, IOException {
	return	bloggerServiceImpl.getEmailFromGoogleAccessToken(code);

	}

	@Override
	public ResponseModel getLoginModel(String email, boolean b) {
		LoginModel loginModel = new LoginModel();
		loginModel.setEmail(email);
		loginModel.setRememberMe(true);
		return getLoginModel(loginModel, b);

	}

	public Integer adminIdGenerator() {
		Integer id;
		while (true) {
			id = JavaHelper.getAdminId();

			if (userRepo.checkId(id) == 0) {
				return id;
			}
		}
	}

	@Transactional
	public ResponseModel createAdmin(UserDto userDto) {
		try {

			if (userDto.checkValidationForRegister()) {

				Optional<User> userExist = userRepo.findByEmail(userDto.getEmail());
				if (userExist.isPresent()) {
					return new ResponseModel("Admin already exist", HttpStatus.BAD_REQUEST, true);
				}
				User user = this.dtoToUser(userDto);

				user.setGoogleAccount(userDto.getAbout().contains("Google"));

				user.setEmail(user.getEmail().toLowerCase().trim());


					user.setSuspendUser(false);
					user.setId(userDto.getId());
					user.setPassword(userDto.getPassword());
					user.setWelcomeEmail(true);
					user.setPasswordSet(false);
				user.setUserType(UserType.ADMIN.toString());
				user.setLinkExpiryDate(JavaHelper.getCurrentDate().toString());

				user.setProfileCreatedDate(JavaHelper.getCurrentDate().toString());

				this.usertoDto(userRepo.save(user));
				Role role = getByUserType( UserType.ADMIN );

				ClientRole newRole = new ClientRole();
				newRole.setClientId(Long.valueOf(user.getId()));
				newRole.setRoleId(role.getRoleId());
				clientRoleService.save(newRole);
				emailService.sendEmailForRegisterAdmin(user);

				return new ResponseModel(ErrorConfig.customMessage("Your admin request has been sent to the super admin. Please wait for up to one day for approval. If your request is approved, you will receive an email confirmation. If the request is not processed within a day, you will need to resubmit your details."), HttpStatus.OK);
			}
			return new ResponseModel("Invalid credentials", HttpStatus.BAD_REQUEST, true);
		} catch (Exception e) {
			logger.error("createAdmin ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

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

					if (clientRole.getRoleId() != 100) {
						return new ResponseModel("Permission denied", HttpStatus.BAD_REQUEST, true);
					}
					if ((!user.isPasswordSet() && validatePassword)
							|| (!(JavaHelper.checkPassword(req.getPassword(),
							EncryptionUtils.decrypt(user.getPassword()), validatePassword)))) {
						return new ResponseModel("Invalid credentials", HttpStatus.BAD_REQUEST, true);

					} else {

						if ( user.getUserType().equals(UserType.ADMIN.toString()) && !user.isPasswordSet()) {

							return new ResponseModel("Invalid credentials", HttpStatus.BAD_REQUEST, true);
						}
						if (!user.isPasswordSet()) {

							user.setGoogleLoginCount(user.getGoogleLoginCount() + 1);
							userRepo.save(user);
						}

						return bloggerServiceImpl.getLoginModel(user, req.getRememberMe());
					}
				}
			}
		} catch (Exception e) {
			return new ResponseModel("Unexpected error occured", HttpStatus.BAD_REQUEST, true);
		}
		return new ResponseModel("Required parameter missing.", HttpStatus.BAD_REQUEST, true);
	}
	public User dtoToUser(UserDto userDto) {
		return this.modelMapper.map(userDto, User.class);
	}

	public UserDto usertoDto(User user) {
		return this.modelMapper.map(user, UserDto.class);
	}

}

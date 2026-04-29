package com.codewithmd.blogger.bloggerappsapis.helper;

import jakarta.mail.*


;


import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.codewithmd.blogger.bloggerappsapis.config.ApiConstants;
import com.codewithmd.blogger.bloggerappsapis.entities.HelpCenter;
import com.codewithmd.blogger.bloggerappsapis.entities.Post;
import com.codewithmd.blogger.bloggerappsapis.entities.User;
import com.codewithmd.blogger.bloggerappsapis.repos.PostRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.UserRepo;

import org.springframework.scheduling.annotation.Async;

@Service
public class EmailService {

	@Autowired
	private UserRepo userRepo;

	@Value("${gmail}")
	private String from;

	@Value("${password.email}")
	private String password;

	@Value("${username.email}")
	private String username;

	@Value("${localhost}")
	private String localhost;
	
	@Autowired
	private PostRepo postRepo;

	public String getFreshVerificationCode() {
		String verificationCode = getVerificationCode();
		List<User> users = userRepo.findByVerificationCode(verificationCode);
		if (users == null || users.isEmpty()) {
			return verificationCode;
		}
		while (!users.isEmpty()) {
			verificationCode = getVerificationCode();
			users = userRepo.findByVerificationCode(verificationCode);
			if (users == null) {
				return verificationCode;
			}
		}
		return verificationCode;
	}

	public String getVerificationCode() {
		String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "*@" + "0123456789" + "abcdefghijklmnopqrstuvwxyz";
		StringBuilder sb = new StringBuilder(20);
		SecureRandom secureRandom = new SecureRandom();
		for (int i = 0; i < 150; i++) {
			int index = secureRandom.nextInt(alphaNumericString.length());

			sb.append(alphaNumericString.charAt(index));
		}

		return sb.toString();
	}

	public String base64ToImage() {

		String logobase64 = null;
		try {
			byte[] fileContent = FileUtils.readFileToByteArray(ResourceUtils.getFile("classpath:logo-xsm.png"));
			if (fileContent != null) {
				logobase64 = Base64.getEncoder().encodeToString(fileContent);
			}
		} catch (Exception e) {

		}
		return logobase64;
	}
	@Async
	public Boolean sendEmailForReset(User user) {

		String subject = "Reset Password";
		String code = getFreshVerificationCode();
		user.setVerificationCode(code);
		String htmlContent = "<html><body><div style=\"max-width:550px ; padding : 18px ; border : 1px solid #dadada ; -webkit-border-radius : 10px; -moz-border-radius :10px ; border-radius : 10px; font-family: Arial, Helvetica, sans-serif; font-size : 15px; color:#495057; \"><p style=\"font-size : 17px ; font-weight : bold;\"> Dear {clientName},</p><p style=\"\">We have received your request to reset your password for your Blogger account. Click on the link below to change your password.</p><table style=\"cursor: pointer;\" cellspacing=\"10\" cellpadding=\"0\" border=\"0\"><tbody><tr><td valign=\"middle\" align=\"center\" style=\"border:none; background-color: #dc3545; -webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px; padding-top: 15px; padding-bottom: 15px; padding-right: 20px; padding-left: 20px;\"><a href=\"{link}\" style=\"color: #ffffff; text-decoration: none; font-family: Helvetica, Arial, sans-serif; font-size: 18px; line-height: 135%; font-weight: normal; border:none; display: block;\" target=\"_blank;\">Reset Password</a></td></tr></tbody></table><p> The link would be valid only for the next <strong style=\"color:#575c60;\">{expirytime}</strong> minutes.</p><p>Need any other assistance? Please write to us at <a href=\"mailto:officialbloggerhub@gmail.com\" style=\"color: #3b5de7; cursor: pointer\">officialbloggerhub@gmail.com</a></p><p style=\"line-height: 30px; color: #303030 display: inline-block;\"><div><div><span>Regards,</span><br/><span style=\"font-size: 18px; color: #303030\">BloggerHub Team</span></div><div><img style=\"height: 70px; margin-top: 10px;\" src=\"{companyLogo}\" height=\"70\"></div></div></p></div></body></html>";
		String link = localhost + "resetpassword?code=" + code;
		htmlContent = htmlContent.replace("{link}", link);
		htmlContent = htmlContent.replace("{clientName}", user.getName());
		htmlContent = htmlContent.replace("{expirytime}", "15");
		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");
		return sendEmail(user.getEmail(), subject, htmlContent,"it.1703302@gmail.com");

	}
	@Async
	public Boolean sendEmailForResetForAdmin(User user) {

		String subject = "Admin Reset Password";
		String code = getFreshVerificationCode();
		user.setVerificationCode(code);
		String htmlContent = "<html><body><div style=\"max-width:550px ; padding : 18px ; border : 1px solid #dadada ; -webkit-border-radius : 10px; -moz-border-radius :10px ; border-radius : 10px; font-family: Arial, Helvetica, sans-serif; font-size : 15px; color:#495057; \"><p style=\"font-size : 17px ; font-weight : bold;\"> Dear {clientName},</p><p style=\"\">We have received your request to reset your password for your Blogger account. Click on the link below to change your password.</p><table style=\"cursor: pointer;\" cellspacing=\"10\" cellpadding=\"0\" border=\"0\"><tbody><tr><td valign=\"middle\" align=\"center\" style=\"border:none; background-color: #dc3545; -webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px; padding-top: 15px; padding-bottom: 15px; padding-right: 20px; padding-left: 20px;\"><a href=\"{link}\" style=\"color: #ffffff; text-decoration: none; font-family: Helvetica, Arial, sans-serif; font-size: 18px; line-height: 135%; font-weight: normal; border:none; display: block;\" target=\"_blank;\">Reset Password</a></td></tr></tbody></table><p> The link would be valid only for the next <strong style=\"color:#575c60;\">{expirytime}</strong> minutes.</p><p>Need any other assistance? Please write to us at <a href=\"mailto:officialbloggerhub@gmail.com\" style=\"color: #3b5de7; cursor: pointer\">officialbloggerhub@gmail.com</a></p><p style=\"line-height: 30px; color: #303030 display: inline-block;\"><div><div><span>Regards,</span><br/><span style=\"font-size: 18px; color: #303030\">BloggerHub Team</span></div><div><img style=\"height: 70px; margin-top: 10px;\" src=\"{companyLogo}\" height=\"70\"></div></div></p></div></body></html>";
		String link = localhost + "admin/resetpassword?code=" + code;
		htmlContent = htmlContent.replace("{link}", link);
		htmlContent = htmlContent.replace("{clientName}", user.getName());
		htmlContent = htmlContent.replace("{expirytime}", "15");
		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");
		return sendEmail(user.getEmail(), subject, htmlContent,"it.1703302@gmail.com");

	}

	@Async
	public void increaseNoOfViews(Post post) {
		post.setNumberOfViews(post.getNumberOfViews()+1);
		postRepo.save(post);
		
	}

	public Boolean sendEmailForBadWords(User user, Post post) {

		String subject = "Warning: Inappropriate Language Usage on our Blog Website";
		String code = getFreshVerificationCode();
		user.setVerificationCode(code);
		String htmlContent = "<div style=\"max-width:550px; padding:18px; border:1px solid #dadada; -webkit-border-radius:10px; -moz-border-radius:10px; border-radius:10px; font-family:Arial, Helvetica, sans-serif; font-size:15px; color:#495057;\"><p style=\"font-size:17px; font-weight:bold;\">Hello {clientName},</p><p>We are writing to address a recent issue on our blog website. It has come to our attention that you used inappropriate language in one of your posts.In your post: {Post} you used abusive word "+'"'+"{abusiveWord}"+'"'+" We take user conduct seriously and strive to maintain a respectful environment for everyone.</p><p>Consider this email a formal warning regarding your use of offensive language. We kindly request that you refrain from such behavior in the future. Failure to comply may result in the removal of your account from our website. We value your participation but must ensure a positive experience for all users. Let's work together to maintain a respectful community.</p><p>You can reach us at <a href=\"mailto:officialbloggerhub@gmail.com\" style=\"color:#3b5de7; cursor:pointer;\">officialbloggerhub@gmail.com</a> if you need assistance.</p><p style=\"line-height:30px; color:#303030; display:inline-block;\"><div><div><span>Regards,</span><br/><span style=\"font-size:18px; color:#303030;\">BloggerHub Team</span></div><div><img style=\"height:70px; margin-top:10px;\" src=\"{companyLogo}\" height=\"70\"></div></div></p></div></body></html>\r\n";
		
		String link = localhost + "resetpassword?code=" + code;
		htmlContent = htmlContent.replace("{link}", link);
		htmlContent = htmlContent.replace("{clientName}", user.getName());
		htmlContent = htmlContent.replace("{abusiveWord}", user.getAbusiveWord().trim());
		htmlContent = htmlContent.replace("{Post}", post.getTitle().trim());
		htmlContent = htmlContent.replace("{expirytime}", "15");
		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");
		return sendEmail(user.getEmail(), subject, htmlContent,"it.1703302@gmail.com");

	}
	
	public Boolean sendWelcomeEmailToUser(String userName, String email) {

		String subject = "Welcome to  BloggerHub - Explore, Engage, and Learn!";

		String htmlContent = "<div style=\"max-width:550px ; padding : 18px ; border : 1px solid #dadada ; -webkit-border-radius : 10px; -moz-border-radius :10px ; border-radius : 10px; font-family: Arial, Helvetica, sans-serif; font-size : 15px; color:#495057; \"><p style=\"font-size : 17px ; font-weight : bold;\">Hello {clientName},</p><p style=\"\">We are thrilled to welcome you to BloggerHub!. </p><p style=\"\">We are thrilled to welcome you to BloggerHub! Thank you for registering and joining our growing community of passionate readers and learners. We're excited to have you on board.</p><p>You can reach us at <a href=\"mailto:officialbloggerhub@gmail.com\" style=\"color: #3b5de7; cursor: pointer\">officialbloggerhub@gmail.com</a> if you need assistance.</p><p style=\"line-height: 30px; color: #303030 display: inline-block;\"><div><div><span>Regards,</span><br/><span style=\"font-size: 18px; color: #303030\">BloggerHub Team</span></div><div><img style=\"height: 70px; margin-top: 10px;\" src=\"{companyLogo}\" height=\"70\"></div></div></p></div>";
		htmlContent = htmlContent.replace("{clientName}", userName);

		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");
		return sendEmail(email, subject, htmlContent,"it.1703302@gmail.com");

	}
	@Async
	public Boolean sendEmailForRegister(User user) {
		
		String subject = "{clientName} Verify your e-mail to complete your BloggerHub sign-up";
		subject = subject.replace("{clientName}", user.getName());

		String htmlContent = "<html><body><div style=\"max-width:550px ; padding : 18px ; border : 1px solid #dadada ; -webkit-border-radius : 10px; -moz-border-radius :10px ; border-radius : 10px; font-family: Arial, Helvetica, sans-serif; font-size : 15px; color:#495057; \"><p style=\"font-size : 17px ; font-weight : bold;\"> Dear {clientName},</p><p style=\"\">Thank you for registering with our platform. Please click on the link below to activate your account.Activation Link will be expired within 15 minutes.</p><table style=\"cursor: pointer;\" cellspacing=\"10\" cellpadding=\"0\" border=\"0\"><tbody><tr><td valign=\"middle\" align=\"center\" style=\"border:none; background-color: #dc3545; -webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px; padding-top: 15px; padding-bottom: 15px; padding-right: 20px; padding-left: 20px;\"><a href=\"{activationLink}\" style=\"color: #ffffff; text-decoration: none; font-family: Helvetica, Arial, sans-serif; font-size: 18px; line-height: 135%; font-weight: normal; border:none; display: block;\" target=\"_blank;\">Activate Account</a></td></tr></tbody></table><p>If you did not register with us, please disregard this email.</p><p>Need any assistance? Please contact our support team at <a href=\"mailto:officialbloggerhub@gmail.com\" style=\"color: #3b5de7; cursor: pointer\">officialbloggerhub@gmail.com</a></p><p style=\"line-height: 30px; color: #303030 display: inline-block;\"><div><div><span>Regards,</span><br/><span style=\"font-size: 18px; color: #303030\">BloggerHub Team</span></div><div><img style=\"height: 70px; margin-top: 10px;\" src=\"{companyLogo}\" height=\"70\"></div></div></p></div></body></html>";

		String link = localhost + "resetpassword?code=" + user.getVerificationCode();
		htmlContent = htmlContent.replace("{activationLink}", link);
		htmlContent = htmlContent.replace("{clientName}", user.getName());
		htmlContent = htmlContent.replace("{expirytime}", "15");
		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");

		return sendEmail(user.getEmail(), subject, htmlContent,"it.1703302@gmail.com");

	}
	@Async
	public Boolean sendEmailToClientForAdminRole(User user) {
		String subject = "Your Admin Account is Now Active";

		String htmlContent = "<html><body><div style=\"max-width: 550px; padding: 18px; border: 1px solid #dadada; border-radius: 10px; font-family: Arial, Helvetica, sans-serif; font-size: 15px; color: #495057;\">"
				+ "<p style=\"font-size: 17px; font-weight: bold;\">Dear {clientName},</p>"
				+ "<p>Your account is now successfully active. You can use your credentials to log in as an Admin.</p>"
				+ "<div style=\"line-height: 30px; color: #303030;\">"
				+ "<p>Thank you,<br/><span style=\"font-size: 18px; color: #303030;\">BloggerHub Team</span></p>"
				+ "<img src=\"{companyLogo}\" alt=\"Company Logo\" style=\"height: 70px; margin-top: 10px;\">"
				+ "</div></div></body></html>";

		htmlContent = htmlContent.replace("{clientName}", user.getName());
		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");

		return sendEmail(user.getEmail(), subject, htmlContent, "dilnawaj4044@gmail.com");
	}

	@Async
	public Boolean sendEmailForRegisterAdmin(User user) {
		
		String subject = "Admin Role Access Request to {clientName}";
		subject = subject.replace("{clientName}", user.getName());

		String htmlContent = "<html><body><div style=\"max-width: 550px; padding: 18px; border: 1px solid #dadada; border-radius: 10px; font-family: Arial, Helvetica, sans-serif; font-size: 15px; color: #495057;\"><p style=\"font-size: 17px; font-weight: bold;\">Dear Dilnawaj Sir,</p><p>{clientName} with emailAddress : {email} has requested access to the Admin role on our Application. If you would like to grant this access, please click the button below.</p><table style=\"cursor: pointer;\" cellspacing=\"10\" cellpadding=\"0\" border=\"0\"><tbody><tr><td valign=\"middle\" align=\"center\" style=\"background-color: #dc3545; border-radius: 10px; padding: 15px 20px;\"><a href=\"{activationLink}\" style=\"color: #ffffff; text-decoration: none; font-family: Helvetica, Arial, sans-serif; font-size: 18px; line-height: 135%; font-weight: normal; display: block;\" target=\"_blank\">Grant Admin Access</a></td></tr></tbody></table><p>If you do not wish to grant admin access, please disregard this email.</p><p>If you need any assistance, please contact our support team at <a href=\"mailto:officialbloggerhub@gmail.com\" style=\"color: #3b5de7;\">officialbloggerhub@gmail.com</a>.</p><div style=\"line-height: 30px; color: #303030;\"><p>Regards,<br/><span style=\"font-size: 18px; color: #303030;\">BloggerHub Team</span></p><img src=\"{companyLogo}\" alt=\"Company Logo\" style=\"height: 70px; margin-top: 10px;\"></div></div></body></html>";

		String link = localhost + "grant/adminrole?email=" + user.getEmail();
		htmlContent = htmlContent.replace("{activationLink}", link);
		htmlContent = htmlContent.replace("{clientName}", user.getName());
		htmlContent = htmlContent.replace("{email}", user.getEmail());
		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");

		return sendEmail("dilnawaj4044@gmail.com", subject, htmlContent,"it.1703302@gmail.com");

	}
@Async
	public Boolean sendEmailToFriends(Optional<User> user, Optional<Post> post, String email) {

		String subject = " Check out this interesting post! Shared by {clientName}";
		subject = subject.replace("{clientName}", user.get().getName());
		String htmlContent = "<div style=\"max-width:550px; padding:18px; border:1px solid #dadada; -webkit-border-radius:10px; -moz-border-radius:10px; border-radius:10px; font-family:Arial, Helvetica, sans-serif; font-size:15px; color:#495057;\">\r\n"
				+ "<p style=\"font-size:17px; font-weight:bold;\">Dear User,</p><p>I hope this email finds you well. Your friend thought you might be interested in an interesting post they came across recently. It's titled <b> {PostTitle} </b> and it's written by <b> {AuthorName} </b>.</p><p>You can read the post by clicking on the following link: <p> </p> <table style=\"cursor: pointer;\" cellspacing=\"10\" cellpadding=\"0\" border=\"0\"><tbody><tr><td valign=\"middle\" align=\"center\" style=\"border:none; background-color: #dc3545; -webkit-border-radius: 100px; -moz-border-radius: 100px; border-radius: 100px; padding-top: 20px; padding-bottom: 20px; padding-right: 40px; padding-left: 40px;\"><a href=\"{PostURL}\" style=\"color: #ffffff; text-decoration: none; font-family: Helvetica, Arial, sans-serif; font-size: 18px; line-height: 135%; font-weight: normal; border:none; display: block;\" target=\"_blank\">Post</a></td></tr></tbody></table> </p><p>Feel free to check it out and share your thoughts. We hope you find it informative and enjoyable.</p><p>You can reach us at <a href=\"mailto:officialbloggerhub@gmail.com\" style=\"color:#3b5de7; cursor:pointer;\">officialbloggerhub@gmail.com</a> if you need assistance.</p><p style=\"line-height:30px; color:#303030; display:inline-block;\"><div><div><span>Regards,</span><br/><span style=\"font-size:18px; color:#303030;\">BloggerHub Team</span></div><div><img style=\"height:70px; margin-top:10px;\" src=\"{companyLogo}\" height=\"70\"></div></div></p></div></body></html>";
		
		String link = localhost + "posts/" + post.get().getPostId();

		htmlContent = htmlContent.replace("{PostURL}", link);
		htmlContent = htmlContent.replace("{PostTitle}", post.get().getTitle());
		htmlContent = htmlContent.replace("{AuthorName}", post.get().getUser().getName());
		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");

		return sendEmail(email, subject, htmlContent,user.get().getEmail());

	}
	@Async
	public Boolean sendNotificationEmail(Optional<User> user, Post post) {

		String subject = " New Post Notification - Stay Updated with  {clientName}";
		subject = subject.replace("{clientName}", post.getUser().getName());
		String htmlContent = "<div style=\"max-width:550px; padding:18px; border:1px solid #dadada; -webkit-border-radius:10px; -moz-border-radius:10px; border-radius:10px; font-family:Arial, Helvetica, sans-serif; font-size:15px; color:#495057;\"><p style=\"font-size:17px; font-weight:bold;\">Dear {clientName},</p><p>We hope this email finds you well. We are excited to inform you that you have successfully subscribed to updates from {bloggerName}'s blog on the BloggerHub platform. We wanted to let you know that {bloggerName} has just published a new post on their blog. Stay ahead of the curve and never miss out on their insightful content again!</p><p> Title: <b>{PostTitle}</b> <br/>Author: <b>{AuthorName}</b></p><p>To read the full post, simply click on the following link:</p><table style=\"cursor: pointer;\" cellspacing=\"10\" cellpadding=\"0\" border=\"0\"><tbody><tr><td valign=\"middle\" align=\"center\" style=\"border:none; background-color: #dc3545; -webkit-border-radius: 100px; -moz-border-radius: 100px; border-radius: 100px; padding-top: 20px; padding-bottom: 20px; padding-right: 40px; padding-left: 40px;\"><a href=\"{PostURL}\" style=\"color: #ffffff; text-decoration: none; font-family: Helvetica, Arial, sans-serif; font-size: 18px; line-height: 135%; font-weight: normal; border:none; display: block;\" target=\"_blank\">Post</a></td></tr></tbody></table><p>Thank you for subscribing to {bloggerName}'s blog. We hope you find their posts engaging and valuable.</p><p>You can reach us at <a href=\"mailto:officialbloggerhub@gmail.com\" style=\"color:#3b5de7; cursor:pointer;\">officialbloggerhub@gmail.com</a> if you need assistance.</p><p style=\"line-height:30px; color:#303030; display:inline-block;\"><div><div><span>Regards,</span><br/><span style=\"font-size:18px; color:#303030;\">BloggerHub Team</span></div><div><img style=\"height:70px; margin-top:10px;\" src=\"{companyLogo}\" height=\"70\"></div></div></p></div>";

		String link = localhost + "posts/" + post.getPostId();
		htmlContent = htmlContent.replace("{clientName}", user.get().getName());
		htmlContent = htmlContent.replace("{bloggerName}", post.getUser().getName());
		htmlContent = htmlContent.replace("{PostURL}", link);
		htmlContent = htmlContent.replace("{PostTitle}", post.getTitle());
		htmlContent = htmlContent.replace("{AuthorName}", post.getUser().getName());
		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");
		return sendEmail(user.get().getEmail(), subject, htmlContent,"it.1703302@gmail.com");

	}
@Async
	public Boolean sendEmail(String to, String subject, String body,String cc) {
		return sendEmail(to, subject, body, cc, "dilnawaj4044@gmail.com", base64ToImage());

	}
	@Async
	public void sendTicketRecieveEmail( String userName,String userEmail,HelpCenter helperCenter) {
		
		String subject = " Ticket Received  {Ticket}";
		subject = subject.replace("{Ticket}", helperCenter.getTicketId().toString());
		String htmlContent = "<div style=\"max-width:550px; padding:18px; border:1px solid #dadada; -webkit-border-radius:10px; -moz-border-radius:10px; border-radius:10px; font-family:Arial, Helvetica, sans-serif; font-size:15px; color:#495057;\"><p style=\"font-size:17px; font-weight:bold;\">Dear {clientName},</p><p>Thank you for reaching out to us. We acknowledge the receipt of your query, and we are committed to providing you with the assistance you need. Your query has been successfully registered with the following details:</p><p>Ticket Id: {Ticket}<br>Date : {Date}</p><p> We appreciate your patience during this time and apologize for any inconvenience caused. We remain committed to resolving your query to your satisfaction.</p><p>You can reach us at <a href=\"mailto:officialbloggerhub@gmail.com\" style=\"color:#3b5de7; cursor:pointer;\">officialbloggerhub@gmail.com</a> if you need assistance.</p><p style=\"line-height:30px; color:#303030; display:inline-block;\"><div><div><span>Regards,</span><br/><span style=\"font-size:18px; color:#303030;\">BloggerHub Team</span></div><div><img style=\"height:70px; margin-top:10px;\" src=\"{companyLogo}\" height=\"70\"></div></div></p></div></body></html>";
		htmlContent = htmlContent.replace("{clientName}", userName);
		htmlContent = htmlContent.replace("{Ticket}", helperCenter.getTicketId().toString());

		htmlContent = htmlContent.replace("{Date}", new Date().toString());

		htmlContent = htmlContent.replace("{companyLogo}", "cid:part1.01030607.06070005@gmail.com");

		 sendEmail(userEmail, subject, htmlContent,"it.1703302@gmail.com");

	}

	public boolean sendEmail(String to, String subject, String body, String fromEmail, String fromName, String ccEmail,
			String bcc, String username, String password, String logobase64, String smtpSeverAddress,
			String calendarFileName, byte[] calendarContent, String calendarContentType) {

		List<String> ccEmails = new ArrayList<>();
		List<String> bccEmails = new ArrayList<>();

		for (String cC : ccEmail.split(",")) {
			ccEmails.add(cC);
		}
		for (String bccEmail : bcc.split(",")) {
			bccEmails.add(bccEmail);
		}
		// smtp properties
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.starttls.enable", true);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.host", smtpSeverAddress);

		// session
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		try {
			Message message = new MimeMessage(session);
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setFrom(new InternetAddress(fromName + " <" + fromEmail + ">"));

			// Set CC recipients if ccEmails list is not null or empty
			if (ccEmails != null && !ccEmails.isEmpty()) {
				Address[] ccAddresses = new Address[ccEmails.size()];
				for (int i = 0; i < ccEmails.size(); i++) {
					ccAddresses[i] = new InternetAddress(ccEmails.get(i));
				}
				message.setRecipients(Message.RecipientType.CC, ccAddresses);
			}

			// Set BCC recipients if bccEmails list is not null or empty
			if (bccEmails != null && !bccEmails.isEmpty()) {
				Address[] bccAddresses = new Address[bccEmails.size()];
				for (int i = 0; i < bccEmails.size(); i++) {
					bccAddresses[i] = new InternetAddress(bccEmails.get(i));
				}
				message.setRecipients(Message.RecipientType.BCC, bccAddresses);
			}

			// Create the HTML part of the email
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(body, "text/html");

			// Create a MimeMultipart object to hold the email content
			MimeMultipart multipart = new MimeMultipart("related");
			multipart.addBodyPart(htmlPart);

			// Create a MimeBodyPart for the logo
			MimeBodyPart logoPart = new MimeBodyPart();

			// Decode the Base64 string to bytes
			byte[] logoBytes = Base64.getDecoder().decode(logobase64);

			// Set the content ID for the logo part
			String contentID = ApiConstants.CONTENTID;
			logoPart.setContentID("<" + contentID + ">");
			logoPart.setContent(logoBytes, "image/png");

			// Add the logo part to the multipart
			multipart.addBodyPart(logoPart);
			// Modify the img tag in the email body to use the content ID
			String modifiedBody = body.replace("src=\"cid:" + ApiConstants.CID + "\"", "src=\"cid:" + contentID + "\"");

			// Create the HTML part with the modified body
			MimeBodyPart modifiedHtmlPart = new MimeBodyPart();
			modifiedHtmlPart.setContent(modifiedBody, "text/html");

			// Add the modified HTML part to the multipart
			multipart.addBodyPart(modifiedHtmlPart);

			// Create a MimeBodyPart for the calendar
			MimeBodyPart calendarPart = new MimeBodyPart();
			calendarPart.setContent(calendarContent, calendarContentType);
			calendarPart.setFileName(calendarFileName);

// Add the calendar part to the multipart
			multipart.addBodyPart(calendarPart);

			// Set the multipart as the content of the message
			message.setContent(multipart);
			message.setSubject(subject);

			// Send the email
			Transport.send(message);

			return true;
		} catch (Exception e) {

		}
		return false;
	}
@Async
	public Boolean sendEmail(String to, String subject, String body, String ccEmail, String bcc, String logobase64) {
		List<String> ccEmails = new ArrayList<>();
		List<String> bccEmails = new ArrayList<>();
		if (ccEmail != null && !"".equals(ccEmail)) {
			for (String cC : ccEmail.split(",")) {
				ccEmails.add(cC);
			}
		}
		if (bcc != null && !"".equals(bcc)) {
			for (String bccEmail : bcc.split(",")) {
				bccEmails.add(bccEmail);
			}
		}

		// smtp properties
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.starttls.enable", true);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.host", "smtp.gmail.com");
	properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

	// session
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		try {
			Message message = new MimeMessage(session);
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setFrom(new InternetAddress(from,"BloggerHub" + " <" + from + ">"));


			// Set CC recipients if ccEmails list is not null or empty
			if (ccEmails != null && !ccEmails.isEmpty()) {
				Address[] ccAddresses = new Address[ccEmails.size()];
				for (int i = 0; i < ccEmails.size(); i++) {
					ccAddresses[i] = new InternetAddress(ccEmails.get(i));
				}
				message.setRecipients(Message.RecipientType.CC, ccAddresses);
			}

			// Set BCC recipients if bccEmails list is not null or empty
			if (bccEmails != null && !bccEmails.isEmpty()) {
				Address[] bccAddresses = new Address[bccEmails.size()];
				for (int i = 0; i < bccEmails.size(); i++) {
					bccAddresses[i] = new InternetAddress(bccEmails.get(i));
				}
				message.setRecipients(Message.RecipientType.BCC, bccAddresses);
			}

			// Create the HTML part of the email
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(body, "text/html");

			// Create a MimeMultipart object to hold the email content
			MimeMultipart multipart = new MimeMultipart("related");
			multipart.addBodyPart(htmlPart);

			// Create a MimeBodyPart for the logo
			MimeBodyPart logoPart = new MimeBodyPart();

			// Decode the Base64 string to bytes
			byte[] logoBytes = Base64.getDecoder().decode(logobase64);

			// Set the content ID for the logo part
			String contentID = "part1.01030607.06070005@gmail.com";
			logoPart.setContentID("<" + contentID + ">");
			logoPart.setContent(logoBytes, "image/png");

			// Add the logo part to the multipart
			multipart.addBodyPart(logoPart);
			// Modify the img tag in the email body to use the content ID
			String modifiedBody = body.replace("src=\"cid:" + "cid:part1.01030607.06070005@gmail.com" + "\"",
					"src=\"cid:" + contentID + "\"");

			// Create the HTML part with the modified body
			MimeBodyPart modifiedHtmlPart = new MimeBodyPart();
			modifiedHtmlPart.setContent(modifiedBody, "text/html");

			// Add the modified HTML part to the multipart
			multipart.addBodyPart(modifiedHtmlPart);

			// Set the multipart as the content of the message
			message.setContent(multipart);
			message.setSubject(subject);

			// Send the email
			Transport.send(message);

			return true;
		} catch (Exception e) {
System.out.println("exception"+e);
		}
	System.out.println("false");
		return false;
	}

	
}

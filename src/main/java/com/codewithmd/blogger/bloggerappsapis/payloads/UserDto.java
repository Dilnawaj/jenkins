package com.codewithmd.blogger.bloggerappsapis.payloads;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.codewithmd.blogger.bloggerappsapis.account.entity.Role;
import com.codewithmd.blogger.bloggerappsapis.account.model.RoleModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.UserType;

public class UserDto {
	private Integer id;
	@NotEmpty
	@NotNull
	@Size(min = 4, message = "Username must be min of 4 characters")
	private String name;

	@Email
	@NotEmpty
	@NotNull
	private String email;
	@NotEmpty
	@NotNull

	private String password;

	private String about;

	private String address;

	private String imageName;

	private String phoneNumber;

	private String profileCreatedDate;

	private GenderEnum gender;

	private String dob;

	private String verificationCode;

	private Integer totalSubscriber = 0;

	private RoleModel role;

	private List<CommentDto> comments = new ArrayList<>();

	private Integer likeCount = 0;

	private Integer dislikeCount = 0;

	private Integer numberOfViews = 0;

	private Integer numberOfPosts = 0;

	private String userType;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		if (name == null) {
			return "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		if (email == null) {
			return "";
		}
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		if (password == null) {
			return "";
		}
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getProfileCreatedDate() {
		return profileCreatedDate;
	}

	public void setProfileCreatedDate(String profileCreatedDate) {
		this.profileCreatedDate = profileCreatedDate;
	}

	public GenderEnum getGender() {
		return gender;
	}

	public void setGender(GenderEnum gender) {
		this.gender = gender;
	}

	public List<CommentDto> getComments() {
		return comments;
	}

	public void setComments(List<CommentDto> comments) {
		this.comments = comments;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public RoleModel getRole() {
		return role;
	}

	public void setRole(RoleModel role) {
		this.role = role;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public Integer getTotalSubscriber() {
		if (totalSubscriber == null) {
			return 0;
		}
		return totalSubscriber;
	}

	public Integer getLikeCount() {
		if (likeCount == null) {
			return 0;
		}
		return likeCount;
	}

	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}

	public Integer getDislikeCount() {
		if (dislikeCount == null) {
			return 0;
		}
		return dislikeCount;
	}

	public void setDislikeCount(Integer dislikeCount) {
		this.dislikeCount = dislikeCount;
	}

	public void setTotalSubscriber(Integer totalSubscriber) {
		this.totalSubscriber = totalSubscriber;
	}

	public String getUserType() {
		if(userType==null)
		{
			return UserType.NORMAL_USER.toString();
		}
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public Integer getNumberOfViews() {
		if (numberOfViews == null) {
			return 0;
		}
		return numberOfViews;
	}

	public void setNumberOfViews(Integer numberOfViews) {
		this.numberOfViews = numberOfViews;
	}

	public Integer getNumberOfPosts() {
		if (numberOfViews == numberOfPosts) {
			return 0;
		}
		return numberOfPosts;
	}

	public void setNumberOfPosts(Integer numberOfPosts) {
		this.numberOfPosts = numberOfPosts;
	}

	public boolean checkValidationForRegister() {
		if (!getName().equals("") && !getEmail().equals("")) {
			return true;
		}
		return false;
	}

}

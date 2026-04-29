package com.codewithmd.blogger.bloggerappsapis.entities;

import java.util.ArrayList;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.codewithmd.blogger.bloggerappsapis.helper.EncryptionUtils;

@Entity
public class User {
	@Id
	private Integer id;
	@Column(nullable = false, length = 100)
	private String name;

	private String email;

	private String password;

	private String about;

	private String address;

	private String phoneNumber;

	private String profileCreatedDate;

	private String gender;

	private String dob;

	private String verificationCode;

	private String linkExpiryDate;

	private String imageName;

	private Boolean welcomeEmail;

	private Integer abusiveContentNo = 0;
	
	private String abusiveWord;

	private boolean isPasswordSet;

	private boolean suspendUser = false;
	
	private boolean isGoogleAccount = false;

	private Integer googleLoginCount = 0;

	private String userType;

	@OneToMany(mappedBy = "user")
	List<Comment> comments = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Post> posts = new ArrayList<>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		if (password == null) {
			return "";
		}
		return EncryptionUtils.decrypt(password);

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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
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

	public String getLinkExpiryDate() {
		return linkExpiryDate;
	}

	public void setLinkExpiryDate(String linkExpiryDate) {
		this.linkExpiryDate = linkExpiryDate;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public boolean isPasswordSet() {
		return isPasswordSet;
	}

	public void setPasswordSet(boolean isPasswordSet) {
		this.isPasswordSet = isPasswordSet;
	}

	public Boolean getWelcomeEmail() {
		if (welcomeEmail == null) {
			return false;
		}
		return welcomeEmail;
	}

	public void setWelcomeEmail(Boolean welcomeEmail) {
		this.welcomeEmail = welcomeEmail;
	}

	public Integer getAbusiveContentNo() {
		if (abusiveContentNo == null) {
			return 0;
		}
		return abusiveContentNo;
	}

	public void setAbusiveContentNo(Integer abusiveContentNo) {
		this.abusiveContentNo = abusiveContentNo;
	}

	public boolean isSuspendUser() {
		return suspendUser;
	}

	public void setSuspendUser(boolean suspendUser) {
		this.suspendUser = suspendUser;
	}

	public Integer getGoogleLoginCount() {
		if (googleLoginCount == null) {
			return 0;
		}
		return googleLoginCount;
	}

	public void setGoogleLoginCount(Integer googleLoginCount) {
		this.googleLoginCount = googleLoginCount;
	}
	
	

	public String getAbusiveWord() {
		return abusiveWord;
	}

	public void setAbusiveWord(String abusiveWord) {
		this.abusiveWord = abusiveWord;
	}
	
	

	public boolean isGoogleAccount() {
		return isGoogleAccount;
	}

	public void setGoogleAccount(boolean isGoogleAccount) {
		this.isGoogleAccount = isGoogleAccount;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", email=" + email + ", password=" + password + ", about=" + about
				+ ", address=" + address + ", phoneNumber=" + phoneNumber + ", profileCreatedDate=" + profileCreatedDate
				+ ", gender=" + gender + ", posts=" + posts + "]";
	}

}

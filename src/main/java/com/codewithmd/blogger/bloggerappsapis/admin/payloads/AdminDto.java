package com.codewithmd.blogger.bloggerappsapis.admin.payloads;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.codewithmd.blogger.bloggerappsapis.account.entity.Role;
import com.codewithmd.blogger.bloggerappsapis.payloads.CommentDto;
import com.codewithmd.blogger.bloggerappsapis.payloads.GenderEnum;

public class AdminDto {
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


	private Role role;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public @NotEmpty @NotNull @Size(min = 4, message = "Username must be min of 4 characters") String getName() {
		return name;
	}

	public void setName(@NotEmpty @NotNull @Size(min = 4, message = "Username must be min of 4 characters") String name) {
		this.name = name;
	}

	public @Email @NotEmpty @NotNull String getEmail() {
		return email;
	}

	public void setEmail(@Email @NotEmpty @NotNull String email) {
		this.email = email;
	}

	public @NotEmpty @NotNull String getPassword() {
		return password;
	}

	public void setPassword(@NotEmpty @NotNull String password) {
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

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
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

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public boolean checkValidationForRegister() {
		if (!getName().equals("") && !getEmail().equals("")) {
			return true;
		}
		return false;
	}

}

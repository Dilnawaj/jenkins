package com.codewithmd.blogger.bloggerappsapis.entities;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Post {

	@Id
	private Integer postId;
	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 10000, nullable = false)
	private String content;

	private String imageName;

	private Date date;

	private Integer likePost = 0;

	private Integer disLikePost = 0;

	private Boolean susbscriberEmail;

	@ManyToOne
	@JoinColumn(name = "categoryId")
	private Category category;

	@ManyToOne
	private User user;

	@OneToMany(mappedBy = "post")
	List<Comment> comments = new ArrayList<>();

	private boolean isPostContentChecked;
	
	private Integer numberOfViews=0;
	
	private Integer recommendedPost = 0;
	
	private boolean suspendPost= false;
	
	


	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public boolean isPostContentChecked() {
		return isPostContentChecked;
	}

	public void setPostContentChecked(boolean isPostContentChecked) {
		this.isPostContentChecked = isPostContentChecked;
	}

	public Integer getLikePost() {
		if (likePost == null) {
			return 0;
		}
		return likePost;
	}

	public void setLikePost(Integer likePost) {
		this.likePost = likePost;
	}

	public Integer getDisLikePost() {
		if (disLikePost == null) {
			return 0;
		}
		return disLikePost;
	}

	public void setDisLikePost(Integer disLikePost) {
		this.disLikePost = disLikePost;
	}

	public Boolean getSusbscriberEmail() {
		if (susbscriberEmail == null) {
			return false;
		}
		return susbscriberEmail;
	}

	public void setSusbscriberEmail(Boolean susbscriberEmail) {
		this.susbscriberEmail = susbscriberEmail;
	}
	
	

	public Integer getNumberOfViews() {
		if(numberOfViews==null)
		{
			return 0;
		}
		return numberOfViews;
	}

	public void setNumberOfViews(Integer numberOfViews) {
		this.numberOfViews = numberOfViews;
	}
	

	public Integer getRecommendedPost() {
		if(recommendedPost==null)
		{
			return 0;
		}
		return recommendedPost;
	}

	public void setRecommendedPost(Integer recommendedPost) {
		this.recommendedPost = recommendedPost;
	}



	

	public boolean isSuspendPost() {
		return suspendPost;
	}

	public void setSuspendPost(boolean suspendPost) {
		this.suspendPost = suspendPost;
	}

	@Override
	public String toString() {
		return "Post [postId=" + postId + ", title=" + title + ", content=" + content + ", imageName=" + imageName
				+ ", date=" + date + ", category=" + category + ", user=" + user + "]";
	}

}

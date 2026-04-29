package com.codewithmd.blogger.bloggerappsapis.services.impl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import com.codewithmd.blogger.bloggerappsapis.config.ApiConstants;
import com.codewithmd.blogger.bloggerappsapis.config.ErrorConfig;
import com.codewithmd.blogger.bloggerappsapis.entities.Category;
import com.codewithmd.blogger.bloggerappsapis.entities.Comment;
import com.codewithmd.blogger.bloggerappsapis.entities.LikeOrDislikePost;
import com.codewithmd.blogger.bloggerappsapis.entities.Post;
import com.codewithmd.blogger.bloggerappsapis.entities.ReportPost;
import com.codewithmd.blogger.bloggerappsapis.entities.SavePost;
import com.codewithmd.blogger.bloggerappsapis.entities.Subscribe;
import com.codewithmd.blogger.bloggerappsapis.entities.User;
import com.codewithmd.blogger.bloggerappsapis.exception.PostResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.helper.EmailService;
import com.codewithmd.blogger.bloggerappsapis.helper.JavaHelper;
import com.codewithmd.blogger.bloggerappsapis.payloads.PostDto;
import com.codewithmd.blogger.bloggerappsapis.payloads.ShareEmail;
import com.codewithmd.blogger.bloggerappsapis.repos.CategoryRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.CommentRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.LikeOrDislikePostRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.PostRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.ReportPostRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.SavePostRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.SubscribeRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.UserRepo;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.PostService;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PostServiceImpl implements PostService {

	@Autowired
	private CategoryRepo categoryRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private PostRepo postRepo;

	@Autowired
	private SavePostRepo savePostRepo;

	@Autowired
	private EmailService emailService;

	@Autowired
	private LikeOrDislikePostRepo likeOrDislikePostRepo;

	@Autowired
	private SubscribeRepo subscribeRepo;

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private ReportPostRepo reportPostRepo;
	
	@Autowired
	private CommentRepo commentRepo;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ResponseModel createPost(PostDto postDto, Integer userId, Integer categoryId) {
		try {
			User user = userRepo.getById(userId);
			Category category = categoryRepo.getById(categoryId);
			Post post = this.modelMapper.map(postDto, Post.class);
			post.setDate(new Date());
			post.setUser(user);
			post.setCategory(category);
			post.setPostId(idGenerator());
			post.setPostContentChecked(false);
			post.setSusbscriberEmail(false);
			this.postRepo.save(post);
			postDto = this.modelMapper.map(post, PostDto.class);

			return new ResponseModel(postDto, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("createCategory ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public Integer idGenerator() {
		Integer id;
		while (true) {
			id = JavaHelper.getId();

			if (postRepo.checkId(id) == 0) {
				return id;
			}
		}
	}

	public ResponseModel updatePost(PostDto postDto) {
		try {
			Optional<Post> postOptional = postRepo.findById(postDto.getPostId());
			if (postOptional.isPresent()) {
				Post post = postOptional.get();
				post.setTitle(postDto.getTitle());
				post.setContent(postDto.getContent());
				post.setImageName(postDto.getImageName());
				post.setDate(postDto.getDate());
				post.setPostContentChecked(false);
				Optional<Category> category = categoryRepo.findById(postDto.getCategory().getCategoryId());
				post.setCategory(category.get());
				this.postRepo.save(post);
				clearCache("saved");
				return new ResponseModel(ErrorConfig.updateMessage("Post"), HttpStatus.ACCEPTED);
			} else {
				return new ResponseModel(ErrorConfig.updateError("Post"), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.error("updatePost ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel deletePost(Integer postId) {
		try {
			Optional<Post> post = this.postRepo.findById(postId);
			if (!post.isEmpty()) {

				clearCache("saved");
				commentRepo.deleteAll(this.commentRepo.findByPost(post.get()));
				this.postRepo.deleteById(post.get().getPostId());
				return new ResponseModel(ErrorConfig.deleteMessage("Post", postId.toString()), HttpStatus.OK);
			} else {
				return new ResponseModel(ErrorConfig.notFoundException("Post", postId.toString()),
						HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			logger.error("deleteUser ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public void deletePost(List<Integer> postIds) {

		this.postRepo.updatePostBysuspendPost(postIds);

	}
	public void warningPost(Integer postId) {

		this.postRepo.updatePostByWarningUser(postId);

	}

	public ResponseObjectModel getPostById(Integer postId) {
		PostDto postDto = new PostDto();
		try {
			Post post = postRepo.getById(postId);
			postDto = this.modelMapper.map(post, PostDto.class);
			postDto.getUser().setPassword("");
			postDto.setNumberOfViews((postDto.getNumberOfViews() + 1));
			postDto.getUser().setTotalSubscriber(subscribeRepo.findByBloggerUserId((post.getUser().getId())).size());
			emailService.increaseNoOfViews(post);
			return new ResponseObjectModel(postDto, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("getPostById ", e);
			return new ResponseObjectModel(postDto, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseObjectModel getPostByCategory(Integer categoryId) {
		List<PostDto> postDto = new ArrayList<>();
		try {
			Category cat = categoryRepo.getById(categoryId);
			List<Post> posts = this.postRepo.findByCategory(cat);
			postDto = posts.stream().map(post -> this.modelMapper.map(post, PostDto.class))
					.collect(Collectors.toList());
			return new ResponseObjectModel(postDto, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("getPostByCategory ", e);
			return new ResponseObjectModel(postDto, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseObjectModel getSavePostByCategory(Integer categoryId, Integer userId) {
		List<PostDto> postDto = new ArrayList<>();
		try {
			Category cat = categoryRepo.getById(categoryId);
			List<Post> postList = this.postRepo.findByCategory(cat);
			List<Post> posts = new ArrayList<>();
			List<SavePost> savePosts = savePostRepo.findByUserId(userId);
			for (SavePost savePost : savePosts) {
				for (Post postData : postList) {
					if (savePost.getPostId().equals(postData.getPostId())) {
						posts.add(postData);
						break;
					}

				}
			}

			postDto = posts.stream().map(post -> this.modelMapper.map(post, PostDto.class))
					.collect(Collectors.toList());
			return new ResponseObjectModel(postDto, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("getPostByCategory ", e);
			return new ResponseObjectModel(postDto, HttpStatus.BAD_REQUEST);
		}
	}

//	public ResponseObjectModel getSavePostByCategoryAndUser(Integer categoryId, Integer userId) {
//		List<PostDto> postDto = new ArrayList<>();
//		try {
//			User user = userRepo.getById(userId);
//			Category cat = categoryRepo.getById(categoryId);
//			List<Post> postList = this.postRepo.findByCategoryAndUser(cat, user);
//			List<Post> posts = new ArrayList<>();
//			List<SavePost> savePosts = savePostRepo.findByUserId(userId);
//			for (SavePost savePost : savePosts) {
//				for (Post postData : postList) {
//					if (savePost.getPostId().equals(postData.getPostId())) {
//						posts.add(postData);
//						break;
//					}
//
//				}
//			}
//			postDto = posts.stream().map(post -> this.modelMapper.map(post, PostDto.class))
//					.collect(Collectors.toList());
//			return new ResponseObjectModel(postDto, HttpStatus.OK);
//		} catch (Exception e) {
//			logger.error("getPostByCategory ", e);
//			return new ResponseObjectModel(postDto, HttpStatus.BAD_REQUEST);
//		}
//	}
	public ResponseObjectModel getPostByCategoryAndUser(Integer categoryId, Integer userId) {
		List<PostDto> postDto = new ArrayList<>();
		try {
			User user = userRepo.getById(userId);
			Category cat = categoryRepo.getById(categoryId);
			List<Post> posts = this.postRepo.findByCategoryAndUser(cat, user);

			postDto = posts.stream().map(post -> this.modelMapper.map(post, PostDto.class))
					.collect(Collectors.toList());
			return new ResponseObjectModel(postDto, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("getPostByCategory ", e);
			return new ResponseObjectModel(postDto, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseObjectModel getPostByUser(Integer userId) {
		List<PostDto> listPost = new ArrayList<>();
		try {
			User user = userRepo.getById(userId);
			Sort sort = Sort.by(Direction.DESC, "date");
			List<Post> posts = this.postRepo.findByUser(user, sort);
			listPost = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class))
					.collect(Collectors.toList());
			return new ResponseObjectModel(listPost, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("getPostByUser ", e);
			return new ResponseObjectModel(listPost, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseObjectModel getAllPost(Integer pageNumber, String sortBy, Integer userId) {
		PostResponseModel postResponseModel = new PostResponseModel();
		try {
			Pageable p = PageRequest.of(pageNumber, ApiConstants.PAGE_SIZE);
			Page<Post> pagePosts = new PageImpl<>(new ArrayList<>());
	
			if (sortBy != null && !"".equals(sortBy)) {
				if (userId != null && sortBy.equalsIgnoreCase("Subscribers")) {
					{
						List<Integer> userIds = subscribeRepo.findByCurrentSubsciberId(userId);
						pagePosts = postRepo.getPostByUserIds(userIds, p);
					}
				} else {
					pagePosts = sortPostByConditiion(sortBy, p);
				}
			}

			else {
				pagePosts = postRepo
						.findAll(PageRequest.of(pageNumber, ApiConstants.PAGE_SIZE, Sort.by("date").descending()));
			}
			List<Post> posts = pagePosts.getContent();
			List<PostDto> postDtos = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class))
					.collect(Collectors.toList());
			postResponseModel.setContent(postDtos);
			postResponseModel.setPageNumber(pagePosts.getNumber());
			postResponseModel.setPageSize(pagePosts.getSize());
			postResponseModel.setTotalElements(pagePosts.getTotalElements());
			postResponseModel.setTotalPages(pagePosts.getTotalPages());
			postResponseModel.setLastPage(pagePosts.isLast());
	
			return new ResponseObjectModel(postResponseModel, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("getAllPost ", e);
			return new ResponseObjectModel(postResponseModel, HttpStatus.BAD_REQUEST);
		}
	}


	private List<Post> sortPostByKeywordAndCondition(String keyword, String sortBy, Integer userId) {
		List<Integer> userIds = new ArrayList<>();
		if (sortBy.equalsIgnoreCase("Subscribers")) {
			userIds = subscribeRepo.findByCurrentSubsciberId(userId);
		}
		sortBy = sortBy.toLowerCase();
		List<Post> pagePosts = new ArrayList<>();
		switch (sortBy) {

		case "newest":
			pagePosts = postRepo.findByKeywordAndNew(keyword);
			break;
		case "oldest":
			pagePosts = postRepo.findByKeywordAndOld(keyword);
			break;
		case "like":
			pagePosts = postRepo.findByKeywordAndLike(keyword);
			break;
		case "comments":
			pagePosts = postRepo.findByKeywordAndComments(keyword);
			break;
		case "views":
			pagePosts = postRepo.findByKeywordAndViews(keyword);
			break;
		case "subscribers":
			pagePosts = postRepo.getPostByKeywordAndUserIds(keyword, userIds);
			break;
		case "recommend":
			pagePosts = postRepo.findByKeywordAndRecommend(keyword);
			break;
		default:
			pagePosts = postRepo.findByKeyword(keyword);
		}

		return pagePosts;

	}

	private Page<Post> sortPostByConditiion(String sortBy, Pageable p) {
		sortBy = sortBy.toLowerCase();
		Page<Post> pagePosts = new PageImpl<>(new ArrayList<>());
		switch (sortBy) {

		case "newest":
			pagePosts = postRepo.getPostsSortedByNew(p);
			break;
		case "oldest":
			pagePosts = postRepo.getPostsSortedByOld(p);
			break;

		case "like":
			pagePosts = postRepo.getPostsSortedByLike(p);
			break;

		case "views":
			pagePosts = postRepo.getPostsSortedByViews(p);
			break;

		case "recommend":
			pagePosts = postRepo.getRecommendedPosts(p);
			break;

		case "comments":
			pagePosts = convertToPage(postRepo.getPostsSortedByComments(), p);
			break;
		default:
			pagePosts = postRepo.getPostsSortedByNew(p);

		}

		return pagePosts;
	}

	// <List<PostDto>
	public ResponseObjectModel searchPost(String keyword, Integer pageNumber, String sortBy, Integer userId) {
		List<PostDto> postDto = new ArrayList<>();
		PostResponseModel postResponseModel = new PostResponseModel();
		try {
			Pageable p = PageRequest.of(pageNumber, ApiConstants.PAGE_SIZE);
			List<Post> postLists = new ArrayList<>();
			if (sortBy == null) {
				postLists = postRepo.findByKeyword("%" + keyword + "%");
			} else {
				postLists = sortPostByKeywordAndCondition("%" + keyword + "%", sortBy, userId);
			}

			Page<Post> pagePosts = convertToPage(postLists, p);

			List<Post> posts = pagePosts.getContent();
			List<PostDto> postDtos = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class))
					.collect(Collectors.toList());
			postResponseModel.setContent(postDtos);
			postResponseModel.setPageNumber(pagePosts.getNumber());
			postResponseModel.setPageSize(pagePosts.getSize());
			postResponseModel.setTotalElements(pagePosts.getTotalElements());
			postResponseModel.setTotalPages(pagePosts.getTotalPages());
			postResponseModel.setLastPage(pagePosts.isLast());
			return new ResponseObjectModel(postResponseModel, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("getAllPost ", e);
			return new ResponseObjectModel(postDto, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseObjectModel searchPostByUser(String keyword, Integer pageNumber, String sortBy, Integer userId) {
		List<PostDto> postDto = new ArrayList<>();
		PostResponseModel postResponseModel = new PostResponseModel();
		try {
			Pageable p = PageRequest.of(pageNumber, ApiConstants.PAGE_SIZE);
			List<Post> postLists = new ArrayList<>();
			if (sortBy == null || sortBy.equalsIgnoreCase("undefined")) {
				postLists = postRepo.findByKeyword("%" + keyword + "%", userId);
			} else {
				postLists = sortPostByKeywordAndUserAndCondition("%" + keyword + "%", sortBy, userId);
			}

			Page<Post> pagePosts = convertToPage(postLists, p);

			List<Post> posts = pagePosts.getContent();
			List<PostDto> postDtos = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class))
					.collect(Collectors.toList());
			postResponseModel.setContent(postDtos);
			postResponseModel.setPageNumber(pagePosts.getNumber());
			postResponseModel.setPageSize(pagePosts.getSize());
			postResponseModel.setTotalElements(pagePosts.getTotalElements());
			postResponseModel.setTotalPages(pagePosts.getTotalPages());
			postResponseModel.setLastPage(pagePosts.isLast());
			return new ResponseObjectModel(postResponseModel, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("getAllPost ", e);
			return new ResponseObjectModel(postDto, HttpStatus.BAD_REQUEST);
		}
	}

	private List<Post> sortPostByKeywordAndUserAndCondition(String keyword, String sortBy, Integer userId) {

		sortBy = sortBy.toLowerCase();
		List<Post> pagePosts = new ArrayList<>();
		switch (sortBy) {

		case "newest":
			pagePosts = postRepo.findByKeywordAndNew(keyword, userId);
			break;
		case "oldest":
			pagePosts = postRepo.findByKeywordAndOld(keyword, userId);
			break;
		default:
			pagePosts = postRepo.findByKeyword(keyword, userId);
		}

		return pagePosts;

	}

	public ResponseObjectModel getPostByUser(Integer userId, Integer pageNumber, String sortBy, String sortDir) {
		List<PostDto> listPost = new ArrayList<>();
		PostResponseModel postResponseModel = new PostResponseModel();
		try {
			User user = userRepo.getById(userId);
	
			Sort sort;
			if (sortDir.equalsIgnoreCase("asc") || sortDir.equalsIgnoreCase("oldest")) {

				sort = Sort.by(sortBy).ascending();
			} else {
				sort = Sort.by(sortBy).descending();
			}
			Pageable p = PageRequest.of(pageNumber, ApiConstants.PAGE_SIZE, sort);

			List<Post> postList = postRepo.findByUser(user, sort);

			Page<Post> pagePosts = getPostByPaginated(postList, p);

			List<Post> posts = pagePosts.getContent();
			List<PostDto> postDtos = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class))
					.collect(Collectors.toList());
			postResponseModel.setContent(postDtos);
			postResponseModel.setPageNumber(pagePosts.getNumber());
			postResponseModel.setPageSize(pagePosts.getSize());
			postResponseModel.setTotalElements(pagePosts.getTotalElements());
			postResponseModel.setTotalPages(pagePosts.getTotalPages());
			postResponseModel.setLastPage(pagePosts.isLast());
		
			return new ResponseObjectModel(postResponseModel, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("getPostByUser ", e);
			return new ResponseObjectModel(listPost, HttpStatus.BAD_REQUEST);
		}
	}

	Page<Post> getPostByPaginated(List<Post> posts, Pageable pageable) {

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;

		List<Post> pageResults;

		if (posts.size() < startItem) {
			pageResults = List.of();
		} else {
			int toIndex = Math.min(startItem + pageSize, posts.size());
			pageResults = posts.subList(startItem, toIndex);
		}

		return new PageImpl<>(pageResults, pageable, posts.size());
	}

	public static Page<Post> convertToPage(List<Post> postList, Pageable pageable) {
		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;

		List<Post> pageList;

		if (postList.size() < startItem) {
			pageList = List.of();
		} else {
			int toIndex = Math.min(startItem + pageSize, postList.size());
			pageList = postList.subList(startItem, toIndex);
		}

		return new PageImpl<>(pageList, pageable, postList.size());
	}

	public ResponseModel unSavedPost(Integer userId, Integer postId) {
		try {
			Optional<SavePost> savePostOpt = savePostRepo.findByUserIdAndPostId(userId, postId);

			if (savePostOpt.isPresent()) {

				savePostRepo.delete(savePostOpt.get());
				return new ResponseModel("Post unSaved", HttpStatus.OK);
			}
			return new ResponseModel("This post is already unSaved", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("getAllPost ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel savedPost(Integer userId, Integer postId) {
		try {
			Optional<SavePost> savePostOpt = savePostRepo.findByUserIdAndPostId(userId, postId);

			if (!savePostOpt.isPresent()) {
				SavePost savePost = new SavePost();
				savePost.setUserId(userId);
				savePost.setPostId(postId);
				savePostRepo.save(savePost);
				
				clearCache("saved");
				return new ResponseModel("Post successfully saved", HttpStatus.OK);
			}

			return new ResponseModel("This post is already saved", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			logger.error("getAllPost ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseModel isPostSave(Integer userId, Integer postId) {
		try {
			Optional<SavePost> savePostOpt = savePostRepo.findByUserIdAndPostId(userId, postId);

			if (!savePostOpt.isPresent()) {

				return new ResponseModel("Post successfully saved", HttpStatus.OK);
			}

			return new ResponseModel("This post is already saved", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("getAllPost ", e);
			return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseObjectModel getSavedPostByUser(Integer userId, Integer pageNumber, String sortBy, String sortDir) {
		PostResponseModel postResponseModel = new PostResponseModel();
		try {

			Sort sort;
			if (sortDir.equalsIgnoreCase("asc")) {

				sort = Sort.by(sortBy).ascending();
			} else {
				sort = Sort.by(sortBy).descending();
			}
			Pageable p = PageRequest.of(pageNumber, ApiConstants.PAGE_SIZE, sort);
			String key = "saved";
			PostResponseModel redisData = (PostResponseModel) redisTemplate.opsForValue().get(key);

		    if (redisData != null) {
		        return new ResponseObjectModel(redisData, HttpStatus.OK);
		    }
			List<SavePost> savePostList = savePostRepo.findByUserIdOrderBySavedAtDesc(userId);

			List<Integer> postIds = new ArrayList<>();

			for (SavePost savePost : savePostList) {
				postIds.add(savePost.getPostId());
			}

			
			List<Post>  postList=getPostsByIdsInOrder(postIds);
			Page<Post> pagePosts = convertToPage(postList, p);

			List<Post> posts = pagePosts.getContent();
			List<PostDto> postDtos = posts.stream().map((post) -> this.modelMapper.map(post, PostDto.class))
					.collect(Collectors.toList());
			postResponseModel.setContent(postDtos);
			postResponseModel.setPageNumber(pagePosts.getNumber());
			postResponseModel.setPageSize(pagePosts.getSize());
			postResponseModel.setTotalElements(pagePosts.getTotalElements());
			postResponseModel.setTotalPages(pagePosts.getTotalPages());
			postResponseModel.setLastPage(pagePosts.isLast());
			if (postResponseModel.getTotalElements()!=0l) {
				redisTemplate.opsForValue().set(key, postResponseModel, 100, TimeUnit.MINUTES);
			}
			return new ResponseObjectModel(postResponseModel, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("getPostByUser ", e);
			return new ResponseObjectModel(new ArrayList<>(), HttpStatus.BAD_REQUEST);
		}
	}
	
	public List<Post> getPostsByIdsInOrder(List<Integer> ids) {
		List<Post> posts = postRepo.getPostByIds(ids);
	    Map<Integer, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getPostId, post -> post));
	    return ids.stream().map(postMap::get).filter(Objects::nonNull).collect(Collectors.toList());
	}
	   public void clearCache(String key) {
	        redisTemplate.delete(key);
	    }
	public ResponseModel addLikeAndDisLike(Integer postId, Boolean like, Integer userId) {
		Optional<Post> postOptional = postRepo.findById(postId);
		if (postOptional.isEmpty()) {
			return new ResponseModel("Post not found", HttpStatus.NOT_FOUND);
		}

		Post post = postOptional.get();
		Optional<LikeOrDislikePost> likeOrDislikePostOptional = likeOrDislikePostRepo.findByUserIdAndPostId(userId,
				postId);

		if (likeOrDislikePostOptional.isPresent()) {
			LikeOrDislikePost likeOrDislikePost = likeOrDislikePostOptional.get();

			if (like.equals(likeOrDislikePost.getLikeOrDislike())) {
				return new ResponseModel("Your " + (like ? "like" : "dislike") + " has already been added",
						HttpStatus.BAD_REQUEST);
			}

			if (like) {
				post.setLikePost(post.getLikePost() + 1);
				post.setDisLikePost(post.getDisLikePost() - 1);
			} else {
				post.setLikePost(post.getLikePost() - 1);
				post.setDisLikePost(post.getDisLikePost() + 1);
			}

			likeOrDislikePost.setLikeOrDislike(like);
			likeOrDislikePostRepo.save(likeOrDislikePost);
		} else {
			LikeOrDislikePost likeOrDislikePost = new LikeOrDislikePost();
			likeOrDislikePost.setPostId(postId);
			likeOrDislikePost.setUserId(userId);
			likeOrDislikePost.setLikeOrDislike(like);

			if (like) {
				post.setLikePost(post.getLikePost() + 1);
			} else {
				post.setDisLikePost(post.getDisLikePost() + 1);
			}

			likeOrDislikePostRepo.save(likeOrDislikePost);
		}

		postRepo.save(post);

		JSONObject json = new JSONObject();
		json.put("message", "Your " + (like ? "like" : "dislike") + " has been successfully added!");
		json.put("like", post.getLikePost());
		json.put("disLike", post.getDisLikePost());

		return new ResponseModel(json.toJSONString(), HttpStatus.OK);
	}
	
	public void sharePost(ShareEmail shareEmail) {
		Optional<User> user = userRepo.findById(shareEmail.getUserId());
		Optional<Post> post = postRepo.findById(shareEmail.getPostId());
		for (String email : shareEmail.getEmails()) {
			emailService.sendEmailToFriends(user, post, email);
		}
		
	}

	public ResponseObjectModel setSubscriber(Integer currentSubsciberId, Integer bloggerUserId) {
		Optional<Subscribe> subscribe = subscribeRepo.findByBloggerUserIdAndCurrentSubsciberId(bloggerUserId,
				currentSubsciberId);

		if (subscribe.isEmpty()) {
			Subscribe subscriber = new Subscribe();
			subscriber.setBloggerUserId(bloggerUserId);
			subscriber.setCurrentSubsciberId(currentSubsciberId);
			subscribeRepo.save(subscriber);
		}

		return new ResponseObjectModel(subscribeRepo.findByBloggerUserId(bloggerUserId).size(), HttpStatus.OK);
	}

	public ResponseObjectModel getSubscriber(Integer currentSubsciberId, Integer bloggerUserId) {
		Optional<Subscribe> subscribe = subscribeRepo.findByBloggerUserIdAndCurrentSubsciberId(bloggerUserId,
				currentSubsciberId);

		if (subscribe.isEmpty()) {
			return new ResponseObjectModel(false, HttpStatus.BAD_REQUEST);
		}
		return new ResponseObjectModel(true, HttpStatus.OK);
	}

	public ResponseObjectModel unSubscribe(Integer currentSubsciberId, Integer bloggerUserId) {
		Optional<Subscribe> subscribe = subscribeRepo.findByBloggerUserIdAndCurrentSubsciberId(bloggerUserId,
				currentSubsciberId);
		subscribeRepo.delete(subscribe.get());
		return new ResponseObjectModel(subscribeRepo.findByBloggerUserId(bloggerUserId).size(), HttpStatus.OK);

	}

	@Override
	public ResponseObjectModel downloadPostInPDF(Integer postId) throws IOException {
		PostDto postDto = new PostDto();

		Post post = postRepo.getById(postId);
		postDto = this.modelMapper.map(post, PostDto.class);
		JSONObject data = getFileDataForJobPerformance(postDto);
		return new ResponseObjectModel(data, HttpStatus.OK);

	}

	private JSONObject getFileDataForJobPerformance(PostDto postDto) throws IOException {
		JSONObject json = new JSONObject();

		String plainText = removeHtmlTags(postDto.getContent());
		if (!plainText.endsWith(".")) {
			plainText += ".";
		}
		plainText = plainText.replaceAll("[\\p{So}]", ""); // Removes symbols like emojis

		// Create a PDF document
		PDDocument document = new PDDocument();

		// Prepare content to be written in the PDF
		PDPage page = new PDPage();
		document.addPage(page);
		PDPageContentStream contentStream = new PDPageContentStream(document, page);

		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20); // Increase font size to 20 only for the title
		float pageWidth = page.getMediaBox().getWidth();
		float stringWidth = PDType1Font.HELVETICA_BOLD.getStringWidth("Post Details") / 1000 * 20; // Calculate
		// string
		// width at
		// font size
		// 20
		float titleX = (pageWidth - stringWidth) / 2; // Calculate the x-coordinate for centering the title
		float contentX = 100; // X-coordinate for the content
		float titleY = 700; // Y-coordinate for the title

		contentStream.beginText();
		contentStream.newLineAtOffset(titleX, 700); // Center align vertically at y-coordinate 700
		contentStream.showText("Post Details");
		contentStream.endText();

		contentStream.setLineWidth(1f); // Set the line width
		contentStream.moveTo(titleX, titleY - 5); // Move to the starting point of the underline
		contentStream.lineTo(titleX + stringWidth, titleY - 5); // Draw the line to the ending point of the
																// underline
		contentStream.stroke();

		// Set the font size back to 12 for the rest of the data
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

		int yOffset = 670;

		// Calculate the required height for the current entry
		float requiredHeight = 45 + 7 * 15; // 45 for the initial offset and 7 lines, each with a height of 15

		// Check if there is enough space for the current entry
		if (yOffset - requiredHeight < 100) { // Check if there is enough space for the next entry
			contentStream.close();

			// Create a new page and continue writing the content on the new page
			page = new PDPage();
			document.addPage(page);
			contentStream = new PDPageContentStream(document, page);
			contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
			yOffset = 700; // Reset yOffset for the new page

			// Begin new text for the new page
			contentStream.beginText();
			contentStream.newLineAtOffset(titleX, 700); // Center align vertically at y-coordinate 700

			contentStream.endText();
		}

		// Rest of the code remains unchanged, but we update the X-coordinate for the
		// content
		contentStream.beginText();
		contentStream.newLineAtOffset(contentX, yOffset);
		contentStream.showText("Author Name: " + postDto.getUser().getName().concat("."));
		contentStream.newLineAtOffset(0, -15);
		contentStream.newLineAtOffset(0, -15);
		contentStream.showText("Post Title: " +  postDto.getTitle().concat(".").replaceAll("[\\p{So}]", ""));

		contentStream.newLineAtOffset(0, -15);
		contentStream.newLineAtOffset(0, -15);

		int maxChunkSize = 70; // Set your desired maximum chunk size
		int textLength = plainText.length();
		boolean flag = true;

		for (int i = 0; i < textLength;) {
			int endIndex = i + maxChunkSize; // Calculate the end index

			if (endIndex < textLength) {
				// Find the last space character within the chunk
				while (endIndex > i && !Character.isWhitespace(plainText.charAt(endIndex))) {
					endIndex--;
				}

				// If no space was found, break the line at maxChunkSize
				if (endIndex == i) {
					endIndex = i + maxChunkSize;
				}
			} else {
				endIndex = textLength; // Use the remaining characters
			}

			String chunk = plainText.substring(i, endIndex);

			if (flag) {
				contentStream.showText("Post Content : " + chunk);
				flag = false;
			} else {
				contentStream.newLineAtOffset(0, -15);
				contentStream.showText(chunk);
			}

			i = endIndex;
		}

		contentStream.newLineAtOffset(0, -15);
		contentStream.newLineAtOffset(0, -15);
		contentStream.showText("Post Category: " + postDto.getCategory().getCategoryTitle().concat("."));

		contentStream.newLineAtOffset(0, -15);
		contentStream.newLineAtOffset(0, -15);
		contentStream.showText("No. of Views: " + postDto.getNumberOfViews() + ".");

		contentStream.endText();

		yOffset -= requiredHeight;

		contentStream.close();

		// Convert the PDF document to Base64
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		document.save(byteArrayOutputStream);
		document.close();
		json.put("Data", Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
		return json;
	}


	public static String removeHtmlTags(String htmlString) {
		// Define a regular expression to match HTML tags
		String regex = "<[^>]+>";

		// Compile the regular expression
		Pattern pattern = Pattern.compile(regex);

		// Use a Matcher to find and replace HTML tags
		Matcher matcher = pattern.matcher(htmlString);
		String plainText = matcher.replaceAll("");

		// Replace HTML entities with their corresponding characters
		plainText = plainText.replaceAll("&nbsp;", " "); // For example, replace &nbsp; with space

		return plainText;
	}

	@Override
	public ResponseObjectModel reportPostFeed(Long postId, Long userId) {
ReportPost reportPost= new ReportPost();
reportPost.setReportedPostId(postId);
reportPost.setReportUserId(userId);
reportPostRepo.save(reportPost);
return new ResponseObjectModel("Success", HttpStatus.OK);
	}
}

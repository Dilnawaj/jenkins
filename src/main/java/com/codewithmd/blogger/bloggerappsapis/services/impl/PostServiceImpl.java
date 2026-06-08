package com.codewithmd.blogger.bloggerappsapis.services.impl;

import com.codewithmd.blogger.bloggerappsapis.config.*;
import com.codewithmd.blogger.bloggerappsapis.entities.*;
import com.codewithmd.blogger.bloggerappsapis.entities.FileStatus;
import com.codewithmd.blogger.bloggerappsapis.payloads.*;
import com.codewithmd.blogger.bloggerappsapis.repos.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.codewithmd.blogger.bloggerappsapis.exception.PostResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.helper.EmailService;
import com.codewithmd.blogger.bloggerappsapis.helper.JavaHelper;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.PostService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private BlogMetricsService metricsService;

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
    private ThirdPartyApi thirdPartyApi;

    @Autowired
    private UploadFileRepo uploadFileRepo;

    @Autowired
    private FileStatusRepo fileStatusRepo;

    @Autowired
    private FileServiceImpl fileService;

    @Autowired
    private SqsMessageListener sqsConsumerService;

    @Autowired
    private JobStatusScheduler jobStatusScheduler;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ResponseModel createPost(PostDto postDto, Integer userId, Integer categoryId) {
        Category category = categoryRepo.getById(categoryId);
        User user = userRepo.getById(userId);
        return createPost(postDto, user, category, "https://" + bucketName + ".s3.amazonaws.com/" + "blog-images/default.PNG");
    }


    public ResponseModel createPost(PostDto postDto, User user, Category category, String imageName) {
        try {
            System.out.println("ImageName " + imageName);
            Post post = this.modelMapper.map(postDto, Post.class);
            post.setDate(new Date());
            post.setUser(user);
            post.setImageName(imageName);
            post.setCategory(category);
            post.setPostId(idGenerator());
            post.setPostContentChecked(false);
            post.setSusbscriberEmail(false);
            this.postRepo.save(post);
            clearCache("saved");
            clearCache("AllPost");
            postDto = this.modelMapper.map(post, PostDto.class);
            metricsService.recordPostCreated();
            return new ResponseModel(postDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("createCategory ", e);
            return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void addSinglePost(BlogAI blogAI, Integer userId, Integer jobId, FileStatus fileStatus) {
        UploadFile uploadFile = uploadFileRepo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("UploadFile not found: " + jobId));


        // ✅ Skip if already processed — prevents double counting
        if (fileStatus.getStatus() == FileTrack.SUCCESS
                || fileStatus.getStatus() == FileTrack.FAILED) {
            logger.warn("⚠️ Already processed: {}, skipping", blogAI.getFileName());
            return;
        }
        try {
            fileStatusRepo.save(changeFileStatus(fileStatus, FileTrack.PROCESSING,""));
            uploadFileRepo.save(changeUploadStatus(uploadFile, FilesUploadTrack.PROCESSING));
            pushBulkStatus(jobId, userId);
            logger.info("🔄 Job {} set to PROCESSING", jobId);  // ✅ add this
            PostDto postDto = modelMapper.map(blogAI, PostDto.class);
            Optional<User> user = userRepo.findById(userId);

            postDto.setUser(this.modelMapper.map(user.get(), UserDto.class));

            Map<String, String> response = thirdPartyApi.getCategoryAndImageBySpringAI(
                    postDto.getTitle(), postDto.getContent());

            Category category = categoryRepo.findCategoryUsingName(response.get("category")).get();
            postDto.setCategory(modelMapper.map(category, CategoryDto.class));

            ResponseModel responseModel = createPost(postDto, user.get(), category, response.get("image"));

            if (responseModel.getResponseCode() == HttpStatus.OK) {
                fileStatusRepo.save(changeFileStatus(fileStatus, FileTrack.SUCCESS,""));
                uploadFileRepo.incrementSuccess(jobId);
                pushBulkStatus(jobId, userId);
            }

        } catch (Exception e) {
            logger.error("❌ Failed to process blog: {}", blogAI.getTitle(), e);
            fileStatusRepo.save(changeFileStatus(fileStatus, FileTrack.FAILED,"Failed to Process Blog :: "+e.getMessage()));
            uploadFileRepo.incrementFailed(jobId);
            pushBulkStatus(jobId, userId);
        }

        // ✅ After each file, check if all files done and update final status
        jobStatusScheduler.scheduleFinalize(jobId,userId);
        pushBulkStatus(jobId, userId);
    }


    private UploadFile changeUploadStatus(UploadFile uploadFile, FilesUploadTrack status) {
        uploadFile.setStatus(status);
        return uploadFile;
    }

    public  FileStatus changeFileStatus(FileStatus fileStatus, FileTrack track,String errorMessage) {
fileStatus.setError_message(errorMessage);
        fileStatus.setStatus(track);
        return fileStatus;

    }

    @Override
    public void uploadFiles(List<FileData> blogData, Integer userId, Integer reqId) {

        UploadFile uploadFile = new UploadFile(reqId, userId, blogData.size(), 0, FilesUploadTrack.UPLOADED, JavaHelper.getCurrentDate(), 0);
        uploadFileRepo.save(uploadFile);
        List<String> fileNames = blogData.stream().map(e -> e.getFileName()).collect(Collectors.toList());
        for (String fileName : fileNames) {
            FileStatus fileStatus = new FileStatus(uploadFile.getFile_id(), FileTrack.PENDING, fileName, "", 0, JavaHelper.getCurrentDate());
            fileStatusRepo.save(fileStatus);
            pushBulkStatus(reqId, userId);
        }
        fileService.uploadToS3(blogData, userId, uploadFile.getFile_id());

    }


    @Override
    public BulkStatus getBulkFileStatus(Integer jobId, Integer userId) {

        BulkStatus uploadFileStatus = uploadFileRepo.getUploadStatus(jobId, userId);
        uploadFileStatus.setFileUploadStatus(fileStatusRepo.getFileStatus(jobId));
        return uploadFileStatus;
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
                clearCache("AllPost");
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
                clearCache("AllPost");
                commentRepo.deleteAll(this.commentRepo.findByPost(post.get()));
                this.postRepo.deleteById(post.get().getPostId());
                metricsService.recordPostDeleted();
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


            String key = "AllPost";

            ResponseObjectModel redisData =
                    (ResponseObjectModel) redisTemplate.opsForValue().get(key);
            logger.info("Redis cache hit for key '{}': {}", key, redisData != null);

            if (redisData != null && sortBy.equalsIgnoreCase("newest") && pageNumber == 0) {
                logger.info("Returning cached data from Redis");
                return redisData;
            }


            if (sortBy != null && !"".equals(sortBy)) {
                if (userId != null && sortBy.equalsIgnoreCase("Subscribers")) {
                    {
                        List<Integer> userIds = subscribeRepo.findByCurrentSubsciberId(userId);
                        pagePosts = postRepo.getPostByUserIds(userIds, p);
                    }
                } else {
                    pagePosts = sortPostByConditiion(sortBy, p);
                }
            } else {
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


            ResponseObjectModel responseObjectModel = new ResponseObjectModel(postResponseModel, HttpStatus.OK);
            if (postResponseModel.getTotalElements() != 0L
                    && sortBy.equalsIgnoreCase("newest")
                    && pageNumber == 0) {
                redisTemplate.opsForValue().set(key, responseObjectModel, 100, TimeUnit.MINUTES);
                logger.info("Cached data in Redis with key '{}'", key);
            }

            return responseObjectModel;
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


            List<Post> postList = getPostsByIdsInOrder(postIds);
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
            if (postResponseModel.getTotalElements() != 0l) {
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
        plainText = plainText.replaceAll("[\\p{So}]", "");

        // ✅ Define fonts ONCE at the top — reuse everywhere
        PDType1Font helveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font helveticaRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        // ✅ Single contentStream — never recreated mid-flow
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        try {
            float pageWidth = page.getMediaBox().getWidth();
            float stringWidth = helveticaBold.getStringWidth("Post Details") / 1000 * 20;
            float titleX = (pageWidth - stringWidth) / 2;
            float contentX = 100;
            float titleY = 700;

            // ── Title ────────────────────────────────────────────────────
            contentStream.setFont(helveticaBold, 20);           // ✅ no deprecation
            contentStream.beginText();
            contentStream.newLineAtOffset(titleX, titleY);
            contentStream.showText("Post Details");
            contentStream.endText();

            // ── Underline beneath title ──────────────────────────────────
            contentStream.setLineWidth(1f);
            contentStream.moveTo(titleX, titleY - 5);
            contentStream.lineTo(titleX + stringWidth, titleY - 5);
            contentStream.stroke();

            // ── Body font (size 12) ──────────────────────────────────────
            contentStream.setFont(helveticaBold, 12);           // ✅ no deprecation

            int yOffset = 670;
            float requiredHeight = 45 + 7 * 15;

            // ── New page if not enough space ─────────────────────────────
            if (yOffset - requiredHeight < 100) {
                contentStream.close();                          // ✅ close BEFORE creating new one

                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(helveticaBold, 12);       // ✅ no deprecation
                yOffset = 700;
            }

            // ── Content ──────────────────────────────────────────────────
            contentStream.beginText();
            contentStream.newLineAtOffset(contentX, yOffset);
            contentStream.showText("Author Name: " + postDto.getUser().getName().concat("."));
            contentStream.newLineAtOffset(0, -15);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Post Title: " + postDto.getTitle().concat(".").replaceAll("[\\p{So}]", ""));
            contentStream.newLineAtOffset(0, -15);
            contentStream.newLineAtOffset(0, -15);

            // ── Post content in chunks ───────────────────────────────────
            int maxChunkSize = 70;
            int textLength = plainText.length();
            boolean flag = true;

            for (int i = 0; i < textLength; ) {
                int endIndex = i + maxChunkSize;

                if (endIndex < textLength) {
                    while (endIndex > i && !Character.isWhitespace(plainText.charAt(endIndex))) {
                        endIndex--;
                    }
                    if (endIndex == i) {
                        endIndex = i + maxChunkSize;
                    }
                } else {
                    endIndex = textLength;
                }

                String chunk = plainText.substring(i, endIndex);

                if (flag) {
                    contentStream.showText("Post Content: " + chunk);
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

        } finally {
            contentStream.close(); // ✅ always closed — even if exception thrown
        }

        // ── Convert to Base64 ────────────────────────────────────────────
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
        ReportPost reportPost = new ReportPost();
        reportPost.setReportedPostId(postId);
        reportPost.setReportUserId(userId);
        reportPostRepo.save(reportPost);
        return new ResponseObjectModel("Success", HttpStatus.OK);
    }

    public void pushBulkStatus(Integer reqId, Integer userId) {

        BulkStatus latestStatus =
                getBulkFileStatus(reqId, userId);

        messagingTemplate.convertAndSend(
                "/topic/upload-status/" + reqId,
                latestStatus
        );
    }


    public int getTotalFiles(Integer jobId) {
        return uploadFileRepo.findById(jobId)
                .map(UploadFile::getTotal_files)
                .orElse(0);
    }
}

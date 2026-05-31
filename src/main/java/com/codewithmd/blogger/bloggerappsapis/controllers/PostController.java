package com.codewithmd.blogger.bloggerappsapis.controllers;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.codewithmd.blogger.bloggerappsapis.config.BlogMetricsService;
import com.codewithmd.blogger.bloggerappsapis.config.FileParserUtil;
import com.codewithmd.blogger.bloggerappsapis.helper.JavaHelper;
import com.codewithmd.blogger.bloggerappsapis.payloads.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.codewithmd.blogger.bloggerappsapis.config.ApiConstants;
import com.codewithmd.blogger.bloggerappsapis.config.ErrorConfig;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.services.impl.PostServiceImpl;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.FileService;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/post")
public class PostController {

	@Autowired
	private PostService postService;

	@Autowired
	private FileService fileService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private BlogMetricsService metricsService;

    @Autowired
    private FileParserUtil fileParserUtil;



    private Logger logger = LoggerFactory.getLogger(this.getClass());

	@CrossOrigin
	@PostMapping(value = "/user/{userId}/category/{categoryId}/post", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> createPost(@Valid @RequestBody PostDto postDto, @PathVariable Integer userId,
			@PathVariable Integer categoryId) {
		ResponseModel createPost = this.postService.createPost(postDto, userId, categoryId);
		return new ResponseEntity<>(createPost.getResponse(), createPost.getResponseCode());
	}

    @CrossOrigin
    @PostMapping(
            value = "/bulk/user/{userId}/post",
            consumes = "multipart/form-data",
            produces = "application/json; charset=utf-8"
    )
    public Map<String,String> createBulkPost(
            @RequestParam("files") MultipartFile[] files,
            @PathVariable Integer userId) throws Exception {

        Integer reqId=   JavaHelper.getId();
        List<FileData> fileData=   FileParserUtil.fetchMultipleFiles(files);
        this.postService.uploadFiles(fileData,userId,reqId);
        return Map.of(
                "message", "Upload successful",
                "reqId", reqId.toString()
        );
    }
//    @CrossOrigin
//    @PostMapping(
//            value = "/bulk/user/{userId}/post",
//            consumes = "multipart/form-data",
//            produces = "application/json; charset=utf-8"
//    )
//    public Map<String,String> createBulkPost(
//            @RequestParam("files") MultipartFile[] files,
//            @PathVariable Integer userId) throws Exception {
//
//      Integer reqId=   JavaHelper.getId();
//         List<FileData> fileData=   FileParserUtil.fetchMultipleFiles(files);
//        this.postService.uploadFiles(fileData,userId,reqId);
//       this.postService.readBlogsFromSqsAndThenParse(userId, reqId);
//        return Map.of(
//                "message", "Upload successful",
//                "reqId", reqId.toString()
//        );
// }


 @CrossOrigin
 @GetMapping("/status/bulk/post/{jobId}/{userId}/status")
 public ResponseEntity<BulkStatus> getFileStatus(@PathVariable Integer jobId, @PathVariable Integer userId)
 {
     return ResponseEntity.status(HttpStatus.OK).body(this.postService.getBulkFileStatus(jobId,userId));
 }

    @CrossOrigin
    @GetMapping("status/user/{userId}/trackId/{trackId}")
    public ResponseEntity<List<BlogAI>> trackAllFiles(
            @PathVariable Integer userId,
            @PathVariable Integer trackId) {  // trackId = jobId

        //List<BlogAI> blogs = sqsConsumerService.readBlogsFromSqs(userId, trackId);
        return ResponseEntity.ok(null);
    }

	@CrossOrigin
	@GetMapping(value = "/{id}/{like}/{userId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> addLikeAndDisLike(@PathVariable Integer id, @PathVariable Boolean like,
			@PathVariable Integer userId) {
		ResponseModel likeAndDisLike = this.postService.addLikeAndDisLike(id, like, userId);
		return new ResponseEntity<>(likeAndDisLike.getResponse(), likeAndDisLike.getResponseCode());
	}

	@CrossOrigin
	@PostMapping(value = "/share", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> sharePost(@RequestBody ShareEmail shareEmail) {
		 this.postService.sharePost(shareEmail);
		return new ResponseEntity<>("Post Successfully Share with your Friends",HttpStatus.OK);
	}

	@CrossOrigin
	@PutMapping(produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> updatePost(@Valid @RequestBody PostDto postDto) {
		ResponseModel updatePost = this.postService.updatePost(postDto);
		return new ResponseEntity<>(updatePost.getResponse(), updatePost.getResponseCode());
	}

	@CrossOrigin
	@DeleteMapping(value = "/{id}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> deletePost(@PathVariable Integer id) {
		ResponseModel deletePost = this.postService.deletePost(id);
		return new ResponseEntity<>(deletePost.getResponse(), deletePost.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/user/{userId}/posts", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getPostByUser(@PathVariable Integer userId) {
		ResponseObjectModel postByUser = this.postService.getPostByUser(userId);
		return new ResponseEntity<>(postByUser.getResponse(), postByUser.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/user/{userId}/post", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getPostByUser(@PathVariable Integer userId,
			@RequestParam(value = "pageNumber", defaultValue = ApiConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(value = "sortByField", defaultValue = ApiConstants.SORT_BY, required = false) PostEnum sortByField,
			@RequestParam(value = "sortBy", required = false) String sortBy) {
		ResponseObjectModel postByUser = this.postService.getPostByUser(userId, pageNumber, sortByField.toString(),
				sortBy);
		return new ResponseEntity<>(postByUser.getResponse(), postByUser.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/user/{userId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getSavedPostByUser(@PathVariable Integer userId,
			@RequestParam(value = "pageNumber", defaultValue = ApiConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(value = "sortBy", defaultValue = ApiConstants.SORT_BY, required = false) PostEnum sortBy,
			@RequestParam(value = "sortDir", defaultValue = ApiConstants.SORT_DIR, required = false) SortDirEnum sortDir) {
		ResponseObjectModel postByUser = this.postService.getSavedPostByUser(userId, pageNumber, sortBy.toString(),
				sortDir.toString());
		return new ResponseEntity<>(postByUser.getResponse(), postByUser.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/category/{categoryId}/posts", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getPostByCategory(@PathVariable Integer categoryId) {
		ResponseObjectModel postByCategory = this.postService.getPostByCategory(categoryId);
		return new ResponseEntity<>(postByCategory.getResponse(), postByCategory.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/save/category/{categoryId}/user/{userId}/posts", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getSavePostByCategory(@PathVariable Integer categoryId,
			@PathVariable Integer userId) {
		ResponseObjectModel postByCategory = this.postService.getSavePostByCategory(categoryId, userId);
		return new ResponseEntity<>(postByCategory.getResponse(), postByCategory.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/category/{categoryId}/user/{userId}/posts", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getPostByCategoryAndUser(@PathVariable Integer categoryId,
			@PathVariable Integer userId) {
		ResponseObjectModel postByCategory = this.postService.getPostByCategoryAndUser(categoryId, userId);
		return new ResponseEntity<>(postByCategory.getResponse(), postByCategory.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/account/{id}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getPostById(@PathVariable Integer id) {
		ResponseObjectModel postById = this.postService.getPostById(id);
		return new ResponseEntity<>(postById.getResponse(), postById.getResponseCode());
	}

	// post Image Upload

	@CrossOrigin
	@GetMapping(value = "/getall/posts", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getAllPosts(
			@RequestParam(value = "pageNumber", defaultValue = ApiConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(value = "sortBy", required = false) String sortBy,
			@RequestHeader(required = false) Integer userId) {

		ResponseObjectModel allPosts = this.postService.getAllPost(pageNumber, sortBy, userId);
		return new ResponseEntity<>(allPosts.getResponse(), allPosts.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/account/search", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> searchPosts(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
			@RequestParam(value = "sortBy", required = false) String sortBy,
			@RequestHeader(required = false) Integer userId) {
		ResponseObjectModel searchPosts = this.postService.searchPost(keyword, pageNumber, sortBy, userId);
		return new ResponseEntity<>(searchPosts.getResponse(), searchPosts.getResponseCode());
	}
	@CrossOrigin
	@GetMapping(value = "/search/user", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> searchPostsByUser(@RequestParam(value = "keyword") String keyword,
			@RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
			@RequestParam(value = "sortBy", required = false) String sortBy,
			@RequestHeader Integer userId) {
		ResponseObjectModel searchPosts = this.postService.searchPostByUser(keyword, pageNumber, sortBy, userId);
		return new ResponseEntity<>(searchPosts.getResponse(), searchPosts.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/{postId}/user/{userId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> savedPost(@PathVariable Integer userId, @PathVariable Integer postId) {
		ResponseModel createPost = this.postService.savedPost(userId, postId);
		return new ResponseEntity<>(createPost.getResponse(), createPost.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/unsave/{postId}/user/{userId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> unSavedPost(@PathVariable Integer userId, @PathVariable Integer postId) {
		ResponseModel createPost = this.postService.unSavedPost(userId, postId);
		return new ResponseEntity<>(createPost.getResponse(), createPost.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/{postId}/user/{userId}/save", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> isPostSave(@PathVariable Integer userId, @PathVariable Integer postId) {
		ResponseModel createPost = this.postService.isPostSave(userId, postId);
		return new ResponseEntity<>(createPost.getResponse(), createPost.getResponseCode());
	}

	@CrossOrigin
	@PostMapping(value = "/image/upload/{postId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> uploadImage(@RequestParam(value="image",required = false) MultipartFile image, @PathVariable Integer postId,
			@RequestParam ( required = false) String imageName) {
		String fileName = null;
		try {

            long start = System.currentTimeMillis();
			ResponseObjectModel responseObjectModel = this.postService.getPostById(postId);
            PostDto postDto = (PostDto) responseObjectModel.getResponse();
			fileName = this.fileService.uploadImage(null, image, postId, imageName,postDto);
            System.out.println("FileName Before "+ fileName);

			postDto.setImageName(fileName);
            System.out.println("FileName After "+ fileName);
			this.postService.updatePost(postDto);
            metricsService.recordUploadTime(System.currentTimeMillis() - start);
			return new ResponseEntity<>(ErrorConfig.updateMessage("Profile Image"), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("uploadImage ", e);
			return new ResponseEntity<>(ErrorConfig.unknownError(), HttpStatus.OK);
		}
	}
//    @Deprecated
//	@CrossOrigin
//	@GetMapping(value = "/image/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
//	public void downloadImage(@PathVariable("imageName") String imageName, HttpServletResponse response)
//			throws IOException {
//		logger.info("This is an informational log message");
//		InputStream resource = this.fileService.getResource(path, imageName);
//		logger.error("This is an error log message");
//		response.setContentType(MediaType.IMAGE_JPEG_VALUE);
//		StreamUtils.copy(resource, response.getOutputStream());
//
//	}

	@CrossOrigin
	@PostMapping(value = "/subscribe/user/{currentSubsciberId}/{bloggerUserId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> setSubscriber(@PathVariable Integer currentSubsciberId,
			@PathVariable Integer bloggerUserId) {
		ResponseObjectModel setSubscriber = this.postService.setSubscriber(currentSubsciberId, bloggerUserId);
		return new ResponseEntity<>(setSubscriber.getResponse(), setSubscriber.getResponseCode());
	}

	@CrossOrigin
	@GetMapping(value = "/subscribe/user/{currentSubsciberId}/{bloggerUserId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> getSubscriber(@PathVariable Integer currentSubsciberId,
			@PathVariable Integer bloggerUserId) {
		ResponseObjectModel getSubscriber = this.postService.getSubscriber(currentSubsciberId, bloggerUserId);
		return new ResponseEntity<>(getSubscriber.getResponse(), getSubscriber.getResponseCode());
	}

	@CrossOrigin
	@PostMapping(value = "/unsubscribe/user/{currentSubsciberId}/{bloggerUserId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> unSubscribe(@PathVariable Integer currentSubsciberId,
			@PathVariable Integer bloggerUserId) {
		ResponseObjectModel unSubscribe = this.postService.unSubscribe(currentSubsciberId, bloggerUserId);
		return new ResponseEntity<>(unSubscribe.getResponse(), unSubscribe.getResponseCode());
	}
	
	@CrossOrigin
	@GetMapping(value = "/download/{postId}", produces = "application/json; charset=utf-8")
	public ResponseEntity<Object> downloadPost(@PathVariable Integer postId) throws IOException {
		ResponseObjectModel getSubscriber = this.postService.downloadPostInPDF( postId);
		return new ResponseEntity<>(getSubscriber.getResponse(), getSubscriber.getResponseCode());
	}
	
	@PostMapping("/report")
	public ResponseEntity<String> reportPost(@RequestParam Long postId,@RequestParam Long userId)
	{
		ResponseObjectModel response = this.postService.reportPostFeed(postId, userId);
		return new ResponseEntity<>(response.getResponse().toString(), response.getResponseCode());
	}
    @GetMapping("/download-all")
    public ResponseEntity<InputStreamResource> downloadAllImages() {
        try {
            // List all objects in S3 bucket under blog-images/
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("blog-images/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            if (listResponse.contents().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No images found in S3");
            }

            // Create a temporary zip file
            File zipFile = File.createTempFile("images-", ".zip");

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (S3Object s3Object : listResponse.contents()) {
                    String key = s3Object.key();
                    String fileName = key.substring(key.lastIndexOf("/") + 1); // extract filename

                    // Skip if not an image
                    if (!fileName.matches(".*\\.(jpg|jpeg|png|PNG|gif)$")) continue;

                    // Download from S3
                    GetObjectRequest getRequest = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build();

                    ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getRequest);

                    // Add to zip
                    zipOutputStream.putNextEntry(new ZipEntry(fileName));
                    StreamUtils.copy(s3ObjectStream, zipOutputStream);
                    zipOutputStream.closeEntry();
                }
            }

            // Return zip as response
            InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"images.zip\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .contentLength(zipFile.length())
                    .body(resource);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create zip file", e);
        }
    }

//	@CrossOrigin
//	@GetMapping(value = "/save/category/{categoryId}/user/{userId}", produces = "application/json; charset=utf-8")
//	public ResponseEntity<Object> getSavePostByCategoryAndUser(@PathVariable Integer categoryId,
//			@PathVariable Integer userId) {
//		ResponseObjectModel postByCategory = this.postService.getSavePostByCategoryAndUser(categoryId, userId);
//		return new ResponseEntity<>(postByCategory.getResponse(), postByCategory.getResponseCode());
//	}

}

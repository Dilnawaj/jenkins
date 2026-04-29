package com.codewithmd.blogger.bloggerappsapis.services.impl;

import java.text.ParseException;
import java.io.IOException;

import com.codewithmd.blogger.bloggerappsapis.account.model.RoleModel;
import org.springframework.beans.factory.annotation.Value;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseModel;
import com.codewithmd.blogger.bloggerappsapis.exception.ResponseObjectModel;
import com.codewithmd.blogger.bloggerappsapis.helper.EmailService;
import com.codewithmd.blogger.bloggerappsapis.helper.EncryptionUtils;
import com.codewithmd.blogger.bloggerappsapis.helper.JavaHelper;
import com.codewithmd.blogger.bloggerappsapis.account.entity.ClientRole;
import com.codewithmd.blogger.bloggerappsapis.account.entity.Role;
import com.codewithmd.blogger.bloggerappsapis.account.model.UserType;
import com.codewithmd.blogger.bloggerappsapis.account.repo.RoleRepo;
import com.codewithmd.blogger.bloggerappsapis.account.service.ClientRoleService;

import com.codewithmd.blogger.bloggerappsapis.config.ErrorConfig;
import com.codewithmd.blogger.bloggerappsapis.entities.Comment;
import com.codewithmd.blogger.bloggerappsapis.entities.Post;
import com.codewithmd.blogger.bloggerappsapis.entities.User;
import com.codewithmd.blogger.bloggerappsapis.payloads.UserDto;
import com.codewithmd.blogger.bloggerappsapis.payloads.WelcomeEmailModel;
import com.codewithmd.blogger.bloggerappsapis.repos.CommentRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.PostRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.SubscribeRepo;
import com.codewithmd.blogger.bloggerappsapis.repos.UserRepo;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class UserServieImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ClientRoleService clientRoleService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private PostServiceImpl postService;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private SubscribeRepo subscribeRepo;

    @Value("${clientId}")
    private String clientId;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional
    public ResponseModel createUser(UserDto userDto) {
        try {

            if (userDto.checkValidationForRegister()) {

                Optional<User> userExist = userRepo.findByEmail(userDto.getEmail());
                if (userExist.isPresent()) {
                    return new ResponseModel("User already exist", HttpStatus.BAD_REQUEST, true);
                }
                User user = this.dtoToUser(userDto);

                user.setGoogleAccount(userDto.getAbout().contains("Google"));

                user.setEmail(user.getEmail().toLowerCase().trim());

                user.setUserType(UserType.NORMAL_USER.toString());


                    user.setLinkExpiryDate(JavaHelper.getCurrentDate().toString());
                    user.setSuspendUser(false);
                    user.setPassword(null);
                    user.setVerificationCode(emailService.getFreshVerificationCode());
                    user.setId(idGenerator());
                    user.setWelcomeEmail(false);
                    if ( !user.isGoogleAccount()  && Boolean.FALSE.equals(emailService.sendEmailForRegister(user))) {
                        return new ResponseModel("An unknown error occurred.Please try again later.",
                                HttpStatus.BAD_REQUEST, true);
                    }

                user.setProfileCreatedDate(JavaHelper.getCurrentDate().toString());

                this.usertoDto(userRepo.save(user));
                Role role = getByUserType( UserType.NORMAL_USER);

                ClientRole newRole = new ClientRole();
                newRole.setClientId(Long.valueOf(user.getId()));
                newRole.setRoleId(role.getRoleId());
                clientRoleService.save(newRole);

                return new ResponseModel(ErrorConfig.addMessage("User"), HttpStatus.OK);
            }
            return new ResponseModel("Invalid credentials", HttpStatus.BAD_REQUEST, true);
        } catch (Exception e) {
            logger.error("createUser ", e);
            return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
        }
    }

    public Integer idGenerator() {
        Integer id;
        while (true) {
            id = JavaHelper.getId();

            if (userRepo.checkId(id) == 0) {
                return id;
            }
        }
    }

    public ResponseModel updateUser(UserDto userDto) {
        try {
            Optional<User> userOptional = userRepo.findById(userDto.getId());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setAbout(userDto.getAbout());
                user.setEmail(userDto.getEmail());
                user.setName(userDto.getName());
                user.setImageName(userDto.getImageName());
                if (!userDto.getPassword().isEmpty()) {
                    user.setPassword(EncryptionUtils.encrypt(userDto.getPassword()));
                }

                userRepo.save(user);
                return new ResponseModel(ErrorConfig.updateMessage("User"), HttpStatus.ACCEPTED);
            } else {
                return new ResponseModel(ErrorConfig.updateError("User"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("updateUser ", e);
            return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseObjectModel getUserById(Integer userId) {
        UserDto userDto = new UserDto();
        try {
            User user = userRepo.getById(userId);
            userDto = this.usertoDto(user);
            userDto.setRole(this.modelMapper.map(roleRepo.findByRoleId(roleRepo.getClientRoleFromClientId(Long.valueOf(userId)).getRoleId()), RoleModel.class));

            List<Post> posts = postRepo.findByUser(user);
            int likeCount = 0;
            int dislikeCount = 0;
            int totalViews = 0;
            for (Post post : posts) {
                likeCount += post.getLikePost();
                dislikeCount += post.getDisLikePost();
                totalViews += post.getNumberOfViews();
            }
            userDto.setProfileCreatedDate(localDate(userDto.getProfileCreatedDate()));
            userDto.setNumberOfPosts(posts.size());
            userDto.setTotalSubscriber(subscribeRepo.findByBloggerUserId(userId).size());
            userDto.setNumberOfViews(totalViews);
            userDto.setLikeCount(likeCount);
            userDto.setDislikeCount(dislikeCount);
            return new ResponseObjectModel(userDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("getUserById ", e);
            return new ResponseObjectModel(userDto, HttpStatus.BAD_REQUEST);
        }
    }

    private String localDate(String dateStr) {

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

        // Define the output date format
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            // Parse the input date string into a Date object
            Date date = inputFormat.parse(dateStr);

            // Format the Date object into the desired output format
            String formattedDate = outputFormat.format(date);
            return formattedDate;

        } catch (Exception e) {
            System.err.println("Error parsing the date: " + e.getMessage());
        }
        return dateStr;

    }

    public ResponseObjectModel getAllUsers() {
        List<UserDto> userDto = new ArrayList<>();
        try {
            List<User> users = this.userRepo.findAll();

            for (User user : users) {
                System.out.println(user.getPassword());
                System.out.println(user.getEmail());
            }
            userDto = users.stream().map(user -> this.usertoDto(user)).collect(Collectors.toList());
            return new ResponseObjectModel(userDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("getAllUsers ", e);
            return new ResponseObjectModel(userDto, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseObjectModel getAllRealUsers() {
        List<UserDto> userDto = new ArrayList<>();
        try {
            List<User> users = this.userRepo.findAll();

            return new ResponseObjectModel(users, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("getAllUsers ", e);
            return new ResponseObjectModel(userDto, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseModel deleteUser(Integer userId) {
        try {
            Optional<User> user = this.userRepo.findById(userId);
            if (!user.isEmpty()) {
                List<Post> posts = postRepo.findByUser(user.get());
                for (Post post : posts) {
                    postService.deletePost(post.getPostId());
                }
                List<Comment> comments = commentRepo.findByUser(user.get());
                commentRepo.deleteAll(comments);
                this.userRepo.delete(user.get());
                return new ResponseModel(ErrorConfig.deleteMessage("User", userId.toString()), HttpStatus.OK);
            } else {
                return new ResponseModel(ErrorConfig.notFoundException("User", userId.toString()),
                        HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("deleteUser ", e);
            return new ResponseModel(ErrorConfig.unknownError(), HttpStatus.BAD_REQUEST);
        }
    }

    @Scheduled(cron = "* */2 * * * *")
    public void welcomeEmailToNewUser() throws ParseException {
        try {

            List<User> users = userRepo.getIdsOfUselessUsers();
            users.stream().forEach(user -> {
                Date expiryDate;
                try {
                    expiryDate = JavaHelper.dateStringToDate(user.getLinkExpiryDate());
                    Integer minutes = JavaHelper.getDiffInMinutes(expiryDate, JavaHelper.getCurrentDate());
                    if (minutes > 15) {
                        userRepo.delete(user);

                    }
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    logger.error("useless user", e);
                }


            });
            users = userRepo.getIdsOfUselessAdmins();
            users.stream().forEach(user -> {
                Date expiryDate;
                try {
                    expiryDate = JavaHelper.dateStringToDate(user.getLinkExpiryDate());
                    Integer minutes = JavaHelper.getDiffInMinutes(expiryDate, JavaHelper.getCurrentDate());
                    if (minutes > 1440) {
                        userRepo.delete(user);

                    }
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    logger.error("useless admin", e);
                }


            });

            List<WelcomeEmailModel> newUsers = userRepo.getIdsOfNewUsers();

            newUsers.stream().forEach(e ->
                    emailService.sendWelcomeEmailToUser(e.getUserName(), e.getEmail())
            );

            postRepo.updateRecommendationPost();


            List<Post> posts = postRepo.findAllPostUnCheckedContent();
            List<Post> newPosts = postRepo.findAllPostNewContent();

            for (Post post : newPosts) {
                post.setSusbscriberEmail(true);
                Integer userId = post.getUser().getId();

                List<Integer> subUserIds = subscribeRepo.findByBloggerUserId(userId);

                for (Integer subUserId : subUserIds) {
                    Optional<User> user = userRepo.findById(subUserId);
                    emailService.sendNotificationEmail(user, post);
                }

                postRepo.save(post);
            }

            for (Post post : posts) {

                String word = containsAbusiveWords(post.getTitle()) != null
                        ? containsAbusiveWords(post.getTitle())
                        : containsAbusiveWords(post.getContent());

                if (word != null) {
                    Integer userId = post.getUser().getId();
                    Optional<User> userOpt = userRepo.findById(userId);
                    User user = userOpt.get();
                    user.setAbusiveWord(word);
                    user.setAbusiveContentNo(user.getAbusiveContentNo() + 1);

                    if (user.getAbusiveContentNo() == 1) {

                        postService.warningPost(post.getPostId());
                        emailService.sendEmailForBadWords(user, post);
                        // send EMail
                    } else if (user.getAbusiveContentNo() == 2) {
                        // suspendUser
                        List<Post> postsOfUser = postRepo.findByUser(user);
                        List<Integer> postIds = postsOfUser.stream().map(p -> p.getPostId()).collect(Collectors.toList());

                        postService.deletePost(postIds);

                        user.setSuspendUser(true);

                    }

                    userRepo.save(user);
                }
            }
            postRepo.updatePostUnCheckedContent();
            userRepo.updateWelcomeStatus();
            logger.info("close Job end");

       } catch (Exception e) {
            logger.error("closeJobs", e);
        }
    }


    public String containsAbusiveWords(String content) {
        List<String> abusiveWords = listOfEncryptAllAbusiveWords();
        for (String word : abusiveWords) {
            String decryptWord = EncryptionUtils.decrypt(word).toLowerCase().trim();
            if (content.toLowerCase().contains(decryptWord)) {
                return decryptWord;
            }
        }
        return null;
    }


    public List<String> listOfEncryptAllAbusiveWords() {
        return Arrays.asList("3MBcHl9DWyPFYYKhDbPDUA==", "1CztNnwgYiOjlo/+xGzWlg==", "1CztNnwgYiOjlo/+xGzWlg==" , "3SDM498iOtYAn1BkNRoVEg==", "zqXKZSIR3sr8x/5rWNsPPQ==",
                "BlVGNhrx561T+8vPPu82WA==", "KvZJotNfTFw+J+h+EfQReA==", "66RV0phDkgIqiQKDMni8Tg==",
                "10KHSgiCvQZgW0WCtCu4cA==", "VLQkDtvF14L1Od29v0japQ==", "eVWuvIvtTX1x0MQjbLkIHA==",
                "LoebPo+ZE5Ey7tP8kiDUaw==", "0WYieOQdraZ8jsazF3p6Bg==", "lasw5CBV5UGpoxG8fo86lA==",
                "xgWZDeuUxrP+bxEGcwuhIg==", "SuJF8Q36l+u+jN+uIpGtVQ==", "APZcf65VS3Z3nqrjWRrPBA==",
                "dAwOshT+qF2Y41ciaz2vdQ==", "BIM7v1L6aMJLP3EvRvkfmg==", "E1RnSf7uA8ewPpXaFnKwhw==",
                "7j0IMwWCRtbvC8dSipqWUA==", "xmso29z7z0Me/WCKEKcwsQ==", "b7KLBMyT8xX3jwsqVUIG/w==",
                "40Yi+hal8xHIvR8ZLHcNQg==", "3SDM498iOtYAn1BkNRoVEg==", "+ws0Td5BFeexuCEBHsm5PQ==",
                "PidcvX+y3uZiDf8Nflavog==", "r0pd2TaJZLiD+yjC0+CAfQ==", "pSTAfSDSE05Q9jQNOD/6TA==",
                "YJtCJWhXnGBH4lr2g+fe/g==", "W81yaMIOidI20k4AeQtyVQ==", "pqTmmxyz+xl6BA98/EoARA==",
                "SXRhug0KKvytM82KGMs5SA==", "SuhIplT2oo+qACFzenvxQw==",
                "u0toMwkZngHZf0BLLktQyw==", "A7E22v6SvGVwdQrapv824A==", "kCts3ld4Y4kurARi5gV7gw==",
                "mqvgZ4Dhorjo3cPw5+XEaQ==", "OcRvOJKwkbMHFfbEGQ3R7A==", "ECd4G6evQtboiWr/pRVIAA==",
                "wHBEnDJ9dVjM7qXPg1enCQ==");

    }


    public User dtoToUser(UserDto userDto) {
        return this.modelMapper.map(userDto, User.class);
    }

    public UserDto usertoDto(User user) {
        return this.modelMapper.map(user, UserDto.class);
    }

    public Role getByUserType(UserType userType) {

        return roleRepo.findByUserType(userType.toString());
    }

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
                    UserDto userDto = new UserDto();
                    userDto.setEmail(userEmail);
                    userDto.setName(name);
                    userDto.setPassword(EncryptionUtils.encrypt(JavaHelper.generateRandomPassword(10, 97, 122)));
                    userDto.setAbout("Google User");
                    return createUser(userDto);

                }
            }
        }
        return new ResponseModel("Error occur while user signup", HttpStatus.BAD_REQUEST, true);

    }
}

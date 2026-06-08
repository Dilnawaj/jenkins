package com.codewithmd.blogger.bloggerappsapis.config;

import com.codewithmd.blogger.bloggerappsapis.account.model.RoleModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.UserType;
import com.codewithmd.blogger.bloggerappsapis.admin.services.impl.AdminServiceImpl;
import com.codewithmd.blogger.bloggerappsapis.entities.User;
import com.codewithmd.blogger.bloggerappsapis.helper.EncryptionUtils;
import com.codewithmd.blogger.bloggerappsapis.payloads.CategoryDto;
import com.codewithmd.blogger.bloggerappsapis.payloads.GenderEnum;
import com.codewithmd.blogger.bloggerappsapis.payloads.UserDto;
import com.codewithmd.blogger.bloggerappsapis.repos.UserRepo;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.CategoryService;
import com.codewithmd.blogger.bloggerappsapis.services.interfaces.RoleAndPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MyApplicationRunner implements ApplicationRunner {
@Autowired
    private AdminServiceImpl adminServiceImpl;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleAndPermissionService roleAndPermissionService;

    @Autowired
    private CategoryService categoryService;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("ApplicationRunner executing at startup... boom boom");


        // Call repository method
       Optional<User> userOpt= userRepo.findByEmail("dilnawaj@gmail.com");
       if(userOpt.isEmpty())
       {
          createDefaultCategory();
          createDefaultRole();
          adminServiceImpl.createAdmin(createDefaultUser()) ;
       }
    }

    private UserDto createDefaultUser() {
        UserDto userModel = new UserDto();
        userModel.setId(786);
        userModel.setPassword(EncryptionUtils.encrypt("Dilnawaj@12345"));
        userModel.setGender(GenderEnum.MALE);
        userModel.setUserType(UserType.ADMIN.toString());
        userModel.setEmail("dilnawaj@gmail.com");
        userModel.setPhoneNumber("8837672536");
        userModel.setAbout("I am a super Admin & owner of  BloggerHub Website");
        userModel.setDob("1998-12-27");
        userModel.setName("Dilnawaj Khan");
        return userModel;
    }

    private void createDefaultRole() {
        this.roleAndPermissionService.createRole(new RoleModel(1,"User",1,false,false, UserType.NORMAL_USER.toString()));
        this.roleAndPermissionService.createRole(new RoleModel(100,"Admin",2,false,false, UserType.ADMIN.toString()));

    }

    private void createDefaultCategory() {
        this.categoryService.createCategory(new CategoryDto(1,"Education","Content related to learning, courses, and academic topics"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Lifestyle","Daily life tips, routines, and personal experiences"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Technology","Tech news, gadgets, programming, and innovations"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Health","Fitness tips, workouts, and health advice"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Fitness","Fitness tips, workouts, and health advice"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Entrepreneurship","Startups, business ideas, and entrepreneurship"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Travel","Travel guides, destinations, and experiences"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Food","Recipes, cooking tips, and food blogs"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Finance","Money management, investing, and finance tips"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Fashion","Clothing, trends, and beauty tips"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Business","Startups, business ideas, and entrepreneurship"), false);
        this.categoryService.createCategory(new CategoryDto(1,"Entertainment","Movies, music, and general entertainment content"), false);


    }
}

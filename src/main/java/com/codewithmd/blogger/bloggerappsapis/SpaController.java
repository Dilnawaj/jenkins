package com.codewithmd.blogger.bloggerappsapis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {
            "/login",
            "/signup",
            "/resetpassword",
            "/{path:^(?!user|api|admin|ws).*}/**"  // ✅ added ws to exclusion
    })
    public String forward() {
        return "forward:/index.html";
    }
}
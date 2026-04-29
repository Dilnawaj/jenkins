package com.codewithmd.blogger.bloggerappsapis.account.service;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.codewithmd.blogger.bloggerappsapis.account.entity.ClientRole;
import com.codewithmd.blogger.bloggerappsapis.account.model.AccessTokenModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.PermissionRoleMappingModel;
import com.codewithmd.blogger.bloggerappsapis.account.model.TokenModel;
import com.codewithmd.blogger.bloggerappsapis.account.repo.RoleRepo;
import com.codewithmd.blogger.bloggerappsapis.exception.BadRequestException;

@Validated
@ControllerAdvice
@Service
public class CustomRequestInterceptor implements HandlerInterceptor, Filter {

	@Autowired
	private PermissionRoleMappingService permissionRoleMappingService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private JWTService jwtService;

	@Autowired
	LoginService loginService;

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest((HttpServletRequest) request);
		try {
			String accessToken = ((HttpServletRequest) request).getHeader("accesstoken");
			if(accessToken!=null)
			{
				AccessTokenModel accessTokenModel = loginService.getAccessTokenModel(accessToken);
				mutableRequest.putHeader("userId", accessTokenModel.getUserId().toString());
			}
		
		}  catch (TokenExpiredException e) {
            logger.error("TokenExpiredException was caught: {}", e.getMessage());
        	throw BadRequestException.of( e.getMessage());
   
        }

		chain.doFilter(mutableRequest, response);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		try {

			logger.info("Request Method :: {}", request.getRequestURI());
			logger.info("Request URL:: {}", request.getRequestURI());
			boolean allowRequest = true;
			String requestUrl = request.getRequestURI();
			String method = request.getMethod();
		

			if (method.equalsIgnoreCase("OPTIONS") || requestUrl.contains("download-all")||requestUrl.contains("getall/")||requestUrl.contains("categ")||requestUrl.contains("image/")||requestUrl.contains("admin/account/")||requestUrl.contains("account/")||requestUrl.contains("background") || requestUrl.contains("webhook/")|| requestUrl.contains("/error")) {
				return true;
			}
			

			String accessToken = request.getHeader("accesstoken")!=null?request.getHeader("accesstoken"):request.getHeader("Accesstoken");


			// for erp we user accessToken not accesstoken
			if (accessToken == null || "".equals(accessToken)) {
				accessToken = request.getHeader("accessToken");
			}

			AccessTokenModel accessTokenModel = loginService.getAccessTokenModel(accessToken);

			for (PermissionModel permission : permissionService.getAllPermissions()) {
				if (requestUrl.startsWith(permission.getCode())) {
					allowRequest = false;
					ClientRole loggedInClientRole = roleRepo.getClientRoleFromClientId(accessTokenModel.getUserId());
					if (loggedInClientRole == null) {
						break;
					}
					List<PermissionRoleMappingModel> permissionList = permissionRoleMappingService
							.getAllPermissionRoleMappings();
					for (PermissionRoleMappingModel permissionRoleMapping : permissionList) {
						if (permissionRoleMapping.getPermissionId() == permission.getId()
								&& permissionRoleMapping.getRoleId() == loggedInClientRole.getRoleId()) {
							allowRequest = true;
							break;
						}
					}

				}
			}
			if (allowRequest) {
				logger.info("allowed URL::{}", request.getRequestURI());
				return true;
			}
		} catch (IllegalArgumentException | JWTVerificationException e) {
			getNewTokens(request, response);
			return false;

		} catch (NullPointerException e) {
			TokenModel model = new TokenModel();
			model.setLogout(true);
			model.setSetTokens(false);
			response.getWriter().write(model.json());
			logger.error("Null Exception so login new", e);
			return false;
		} catch (Exception e) {
			logger.error("preHandle", e);
		}
		logger.info("blocked URL:: {}", request.getRequestURI());
		response.getWriter().write("Request has been blocked");
		response.setStatus(HttpStatus.SC_FORBIDDEN);
		return false;
	}

	private boolean getNewTokens(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String refreshToken = request.getHeader("refreshtoken");
			String loginToken = request.getHeader("logintoken");
			String subject = jwtService.getSubject(refreshToken);
			if (!subject.equals(loginToken)) {
				throw new JWTVerificationException("");
			}
			TokenModel model = new TokenModel();
			model.setAccessToken(jwtService.getNewAccessToken(refreshToken, new ArrayList<>()));
			model.setRefreshToken(jwtService.extendRefreshToken(refreshToken));
			model.setLogout(false);
			model.setSetTokens(true);
			response.getWriter().write(model.json());
		} catch (Exception e) {
			TokenModel model = new TokenModel();
			model.setLogout(true);
			model.setSetTokens(false);
			response.getWriter().write(model.json());
			logger.error("getNewAccessToken", e);
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		return false;
	}

}
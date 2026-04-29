package com.codewithmd.blogger.bloggerappsapis.account.service;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Service
public class JWTService {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.accessTokenExpiryTime}")
	private long accessTokenExpiryTime;

	@Value("${jwt.refreshTokenExpiryTime}")
	private long refreshTokenExpiryTime;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public String getAccessToken(Long userId, String subject, List<String> claims) {
		if (claims == null) {
			claims = new ArrayList<>();
		}

		if (subject == null) {
			subject = userId.toString();
		}

		Algorithm algo = Algorithm.HMAC256(secret.getBytes());
		Builder builder = JWT.create().withSubject(subject)
				.withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiryTime * 60 * 1000))
				.withIssuer("myDomainName");
		for (String claim : claims) {
			builder.withClaim(claim, true);
		}
		return builder.sign(algo);
	}

	public String getRefreshToken(Long userId, Long companyId, String subject, boolean rememberMe) {
		Algorithm algo = Algorithm.HMAC256(secret.getBytes());
		long expiryTime = 2 * accessTokenExpiryTime;
		if (rememberMe) {
			expiryTime = refreshTokenExpiryTime;
		}
		if (companyId == null) {
			companyId = 0l;
		}
		if (subject == null) {
			subject = userId + "#" + companyId;
		}
		Builder builder = JWT.create().withSubject(subject)
				.withExpiresAt(new Date(System.currentTimeMillis() + expiryTime * 60 * 1000))
				.withIssuer("myDomainName");
		return builder.sign(algo);
	}

	public String extendRefreshToken(String refreshToken) {
		Date date = getExpiry(refreshToken);
		String subject = getSubject(refreshToken);
		long millis = date.getTime() + accessTokenExpiryTime * 60 * 1000;

		Algorithm algo = Algorithm.HMAC256(secret.getBytes());

		Builder builder = JWT.create().withSubject(subject).withExpiresAt(new Date(millis)).withIssuer("hiretalent.ai");
		refreshToken = builder.sign(algo);
		logger.info("refresh token extended :: refreshToken {}", refreshToken);
		return refreshToken;
	}

	public Date getExpiry(String accessToken) {
		Algorithm algo = Algorithm.HMAC256(secret.getBytes());
		JWTVerifier verifier = JWT.require(algo).build();
		DecodedJWT decodejwt = verifier.verify(accessToken);
		return decodejwt.getExpiresAt();
	}

	public String getSubject(String accessToken) {
		Algorithm algo = Algorithm.HMAC256(secret.getBytes());
		JWTVerifier verifier = JWT.require(algo).build();
		DecodedJWT decodejwt = verifier.verify(accessToken);
		return decodejwt.getSubject();
	}

	public List<String> getClaims(String accessToken) {
		Algorithm algo = Algorithm.HMAC256(secret.getBytes());
		JWTVerifier verifier = JWT.require(algo).build();
		DecodedJWT decodejwt = verifier.verify(accessToken);
		return decodejwt.getClaims().keySet().stream().collect(Collectors.toList());
	}

	public String getNewAccessToken(String refreshToken, List<String> claims) throws JWTVerificationException {
		if (claims == null) {
			claims = new ArrayList<>();
		}
		String subject = getSubject(refreshToken);
		if (subject != null) {
			return getAccessToken(0l, subject, claims);
		} else {
			throw new JWTVerificationException("subject is not matching");
		}
	}

	public String getNewRefreshToken(String refreshToken) throws JWTVerificationException {

		return extendRefreshTokenByDays(refreshToken, 2);
	}

	public String extendRefreshTokenByDays(String refreshToken, Integer days) {
		Date date = getExpiry(refreshToken);
		String subject = getSubject(refreshToken);
		Date dayAfter = new Date(date.getTime() + TimeUnit.DAYS.toMillis(days));

		Algorithm algo = Algorithm.HMAC256(secret.getBytes());

		Builder builder = JWT.create().withSubject(subject).withExpiresAt(dayAfter).withIssuer("officialbloggerhub");
		refreshToken = builder.sign(algo);
		logger.info("refresh token extended :: refreshToken {}", refreshToken);
		return refreshToken;
	}

	public String getRefreshToken(Long userId, String subject, boolean rememberMe) {
		Algorithm algo = Algorithm.HMAC256(secret.getBytes());
		long expiryTime = 2 * accessTokenExpiryTime;
		if (rememberMe) {
			expiryTime = refreshTokenExpiryTime;
		}

		if (subject == null) {
			subject = userId.toString();
		}
		Builder builder = JWT.create().withSubject(subject)
				.withExpiresAt(new Date(System.currentTimeMillis() + expiryTime * 60 * 1000))
				.withIssuer("myDomainName");
		return builder.sign(algo);
	}



}

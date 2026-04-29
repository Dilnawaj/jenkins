package com.codewithmd.blogger.bloggerappsapis.helper;

import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtils {
	private static SecretKeySpec secretKey;
	private static final Logger logger = LoggerFactory.getLogger(EncryptionUtils.class);
	private static String myKey;
	private static String padding1;
	private static String aESAlgo;

	private EncryptionUtils() {
		Properties properties;
		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("system.properties");
			properties = new Properties();
			properties.load(inputStream);
			myKey = properties.getProperty("dataEncryption.key");
			padding1 = properties.getProperty("AESPadding");
			aESAlgo = properties.getProperty("AESAlgo");
		} catch (Exception e) {
			logger.error("AESEncryption", e);
		}
	}

	public static String decrypt(String strToDecrypt) {
		try {
			if (strToDecrypt == null || "".equals(strToDecrypt)) {
				return strToDecrypt;
			}
			setKey();
			Cipher cipher = Cipher.getInstance(padding1);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			logger.error("decrypt ", e);
		}
		return strToDecrypt;
	}

	public static String encrypt(String strToEncrypt) {
		try {
			if (strToEncrypt == null || "".equals(strToEncrypt)) {
				return strToEncrypt;
			}

			setKey();
			Cipher cipher = Cipher.getInstance(padding1);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			logger.error("encrypt", e);
		}
		return strToEncrypt;
	}

	public static void setKey() {
		MessageDigest sha = null;
		try {
			byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
			sha = MessageDigest.getInstance(aESAlgo);
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		} catch (Exception e) {
			logger.error("setKey", e);
		}
	}
}

package com.codewithmd.blogger.bloggerappsapis.helper;

import org.springframework.security.crypto.bcrypt.BCrypt;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JavaHelper {

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String DATE_FORMAT2 = "yyyy-MM-dd";
	private static final String DATE_FORMAT3 = "dd-MM-yyyy";
	private static final String DATE_FORMAT4 = "MM-dd-yyyy";
	private static final String DATE_FORMAT5 = "dd/MM/yyyy";
	private static final String DATE_FORMAT6 = "yyyy-MM-dd H:mm:ss";
	private static final String DATE_FORMAT7 = "dd/MM/yyyy HH:mm:ss";
	private static final String DATE_FORMAT8 = "MM-dd-yyyy HH:mm:ss";
	private static final String DATE_FORMAT9 = "MM-dd-yyyy H:mm:ss";
	private static final String DATE_FORMAT10 = "dd-MM-yyyy HH:mm:ss";
	private static final String DATE_FORMAT11 = "MM/dd/yyyy";
	private static final String DATE_FORMAT12 = "yyyy-dd-MM";
	private static final String DATE_FORMAT13 = "yyyy/MM/dd";
	private static final String DATE_FORMAT23 = "E MMM dd HH:mm:ss zzz yyyy";

	static Logger logger = LoggerFactory.getLogger(JavaHelper.class);

	public static long dateToDateMillis(Date date) {
		return date.getTime();
	}

	public static Date dateStringToDate(String dateStr) throws ParseException {
		System.out.println("date "+ dateStr);
		boolean flag = isValidFormat(DATE_FORMAT, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT2, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT2);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT23, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT23);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT3, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT3);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT5, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT5);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT6, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT6);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT7, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT7);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT8, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT8);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT9, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT9);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT10, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT10);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT11, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT11);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT12, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT12);
			return format.parse(dateStr);
		}
		flag = isValidFormat(DATE_FORMAT13, dateStr);
		if (flag) {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT13);
			return format.parse(dateStr);
		}

		flag = isValidFormat(DATE_FORMAT4, dateStr);
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT4);
		return format.parse(dateStr);
	}

	public static String getBcrypt(String text) {
		return BCrypt.hashpw(text, BCrypt.gensalt(10));
	}

	public static boolean checkPassword(String plainPassword, String encryptedPassword, boolean validatePassword) {
		return validatePassword ? plainPassword.equalsIgnoreCase(encryptedPassword) : true;
	}

	public static boolean isValidFormat(String format, String value) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(value);
			if (!value.equals(sdf.format(date))) {
				date = null;
			}
		} catch (ParseException ex) {

		}
		return date != null;
	}

	public static int getId() {
		try {
			Random random = SecureRandom.getInstanceStrong();
			return random.nextInt(Integer.MAX_VALUE);

		} catch (Exception e) {
			logger.error("getId", e);
		}
		return 0;
	}
	public static int getAdminId() {
		try {
			SecureRandom random = SecureRandom.getInstanceStrong();
			return random.nextInt(900) + 100; // Generates a value between 100 and 999
		} catch (Exception e) {
			logger.error("getId", e);
		}
		return 0;
	}
	public static Integer getWeekOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.WEEK_OF_MONTH);
	}

	public static Integer getMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	public static Integer getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public static int checkIntegerValue(Object value) {
		if (value == null || value == "")
			return 0;
		else
			return Integer.parseInt("" + value);
	}

	public static String checkStringValue(Object value) {
		if (value == null || value == "")
			return "";
		else
			return value.toString();
	}

	public static Integer getDiffInMinutes(Date startDate, Date endDate) {
		try {
			long diff = endDate.getTime() - startDate.getTime();
			long diffMinutes = diff / (60 * 1000) % 60;
			return (int) diffMinutes;
		} catch (Exception e) {
			return 0;
		}
	}

	public static Date getCurrentDate() {
		return new Date();
	}

	public static String generateRandomPassword(int len, int randNumOrigin, int randNumBound) {
		SecureRandom random = new SecureRandom();
		return random.ints(len, randNumOrigin, randNumBound + 1).mapToObj(i -> String.valueOf((char) i))
				.collect(Collectors.joining());
	}
	 public static String generatePassword() {
	        // Define character sets
	        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
	        String numbers = "0123456789";
	        String specialCharacters = "!@#$%^&*()-_+=<>?";

	        // Create a random instance
	        Random random = new Random();

	        // Ensure at least one character from each set
	        StringBuilder password = new StringBuilder();
	        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
	        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
	        password.append(numbers.charAt(random.nextInt(numbers.length())));
	        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

	        // Fill the remaining characters randomly from all sets
	        String allCharacters = upperCaseLetters + lowerCaseLetters + numbers + specialCharacters;
	        for (int i = 4; i < 8; i++) {
	            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
	        }

	        // Shuffle the characters to ensure randomness
	        return shuffleString(password.toString());
	    }

	    private static String shuffleString(String input) {
	        char[] characters = input.toCharArray();
	        Random random = new Random();
	        for (int i = 0; i < characters.length; i++) {
	            int j = random.nextInt(characters.length);
	            char temp = characters[i];
	            characters[i] = characters[j];
	            characters[j] = temp;
	        }
	        return new String(characters);
	    }
}

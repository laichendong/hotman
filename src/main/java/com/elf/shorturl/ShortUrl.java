package com.elf.shorturl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

/**
 * User: laichendong
 * Date: 12-4-20
 * Time: 下午2:16
 */
public class ShortUrl {
	Logger logger = Logger.getLogger(ShortUrl.class);
	private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

	private static final int URL_LENGTH = 6;

	/**
	 * 将一个字符串hash成一个 URL_LENGTH 长度的字符串
	 * @param longString 长串
	 * @return hash之后的短串
	 */
	public String toShort(String longString) {
		byte[] bytes = DigestUtils.md5(longString);
		int step = bytes.length / URL_LENGTH;
		char[] chars = new char[URL_LENGTH];
		for (int i = 0, j = 0; i < URL_LENGTH * step; i += step) {
			chars[j++] = DIGITS[(Math.abs(bytes[i]) % DIGITS.length)];
		}

		return new String(chars);
	}
}

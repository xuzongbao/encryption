package elven.test;

import java.nio.charset.StandardCharsets;

import elven.encryption.SHA256Util;

/**
 * SHA-256 摘要演示。哈希不是加密。
 */
public class testSHA256 {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) throws Exception {
		System.out.println("算法: " + SHA256Util.ALGORITHM + "（哈希不是加密，不能还原原文）");

		byte[] digest = SHA256Util.hash(DATA.getBytes(StandardCharsets.UTF_8));
		System.out.println("摘要长度(字节): " + digest.length);
		System.out.println(DATA + " SHA-256 : " + SHA256Util.hashHex(DATA.getBytes(StandardCharsets.UTF_8)));

		String tweaked = DATA.substring(0, DATA.length() - 1) + "?";
		System.out.println(tweaked + " SHA-256 : " + SHA256Util.hashHex(tweaked.getBytes(StandardCharsets.UTF_8)));
		System.out.println("改一个字符后摘要完全不同，这叫雪崩效应；但别人仍能自己算出同样的哈希。");
	}
}

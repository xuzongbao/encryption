package elven.test;

import java.nio.charset.StandardCharsets;

import elven.encryption.Base64Util;

/**
 * Base64 编解码演示。Base64 不是加密。
 */
public class testBase64 {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) {
		String base64Result = Base64Util.base64Encrypt(DATA.getBytes(StandardCharsets.UTF_8));
		System.out.println("DATA ========>>>base64编码===========>>>>>>> " + base64Result);

		String base64Plain = Base64Util.base64Decrypt(base64Result);
		System.out.println("DATA ========>>>base64解码===========>>>>>>> " + base64Plain);
	}
}

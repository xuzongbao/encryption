package elven.test;

import elven.encryption.Base64Util;

public class testBase64 {

	//待加密明文
	public static final String DATA = "hi, welcome to my git area!";
	
	public static void main(String[] args) throws Exception {
		String base64Result = Base64Util.base64Encrypt(DATA.getBytes());
		System.out.println("DATA ========>>>base64加密===========>>>>>>> " + base64Result);
		
		String base64Plain = Base64Util.base64Decrypt(base64Result);
		System.out.println("DATA ========>>>base64解密===========>>>>>>> " + base64Plain);
		
	}
}

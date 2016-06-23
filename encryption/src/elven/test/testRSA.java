package elven.test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import elven.encryption.BytesToHex;
import elven.encryption.RSAUtil;

public class testRSA {

	//待加密原文
	public static final String DATA = "hi, welcome to my git area!";
	
	public static void main(String[] args) throws Exception {
		Map<String, Object> keyMap = RSAUtil.initKey();
		
		RSAPublicKey rsaPublicKey = RSAUtil.getpublicKey(keyMap);
		RSAPrivateKey rsaPrivateKey = RSAUtil.getPrivateKey(keyMap);
		System.out.println("RSA PublicKey: " + rsaPublicKey);
		System.out.println("RSA PrivateKey: " + rsaPrivateKey);
		
		byte[] rsaResult = RSAUtil.encrypt(DATA.getBytes(), rsaPublicKey);
		System.out.println(DATA + "====>>>> RSA 加密>>>>====" + BytesToHex.fromBytesToHex(rsaResult));
		
		byte[] plainResult = RSAUtil.decrypt(rsaResult, rsaPrivateKey);
		System.out.println(DATA + "====>>>> RSA 解密>>>>====" + BytesToHex.fromBytesToHex(plainResult));
	}
}

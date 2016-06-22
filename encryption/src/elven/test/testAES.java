package elven.test;

import elven.encryption.AESUtil;
import elven.encryption.BytesToHex;

public class testAES {

	//待加密的原文
	public static final String DATA = "hi, welcome to my git area!";
	
	public static void main(String[] args) throws Exception {
		//获得密钥
		byte[] aesKey = AESUtil.initKey();
		System.out.println("AES 密钥 : " + BytesToHex.fromBytesToHex(aesKey));
		//加密
		byte[] encrypt = AESUtil.encryptAES(DATA.getBytes(), aesKey);
		System.out.println(DATA + " AES 加密 : " + BytesToHex.fromBytesToHex(encrypt));
		
		//解密
		byte[] plain = AESUtil.decryptAES(encrypt, aesKey);
		System.out.println(DATA + " AES 解密 : " + new String(plain));
	}
}

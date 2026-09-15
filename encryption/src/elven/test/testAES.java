package elven.test;

import java.nio.charset.StandardCharsets;

import elven.encryption.AESUtil;
import elven.encryption.BytesToHex;

/**
 * AES-GCM 加解密演示。密文线格式：12 字节 IV || 密文+tag。
 */
public class testAES {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) throws Exception {
		byte[] aesKey = AESUtil.initKey();
		System.out.println("AES 密钥 : " + BytesToHex.fromBytesToHex(aesKey));
		System.out.println("变换 : " + AESUtil.TRANSFORMATION);

		byte[] encrypt = AESUtil.encryptAES(DATA.getBytes(StandardCharsets.UTF_8), aesKey);
		System.out.println(DATA + " AES 加密 : " + BytesToHex.fromBytesToHex(encrypt));
		System.out.println("线格式: 前 " + AESUtil.GCM_IV_LENGTH_BYTES + " 字节是 IV，其余是密文+tag");

		byte[] plain = AESUtil.decryptAES(encrypt, aesKey);
		System.out.println(DATA + " AES 解密 : " + new String(plain, StandardCharsets.UTF_8));
	}
}

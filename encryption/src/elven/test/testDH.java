package elven.test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import elven.encryption.AESUtil;
import elven.encryption.BytesToHex;
import elven.encryption.DHUtil;

/**
 * DH 密钥协商演示：双方算出同一把 AES 密钥，再加密示例明文。
 */
public class testDH {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) throws Exception {
		byte[] publicKey1;
		byte[] privateKey1;
		byte[] secretKey1;
		byte[] publicKey2;
		byte[] privateKey2;
		byte[] secretKey2;

		System.out.println("DH 模数位数: " + DHUtil.KEY_SIZE_BITS + "，派生算法: " + DHUtil.SECRET_ALGORITHM);

		Map<String, Object> keyMap1 = DHUtil.initKey();
		publicKey1 = DHUtil.getPublicKey(keyMap1);
		privateKey1 = DHUtil.getPrivateKey(keyMap1);
		System.out.println("DH 甲方公钥: " + BytesToHex.fromBytesToHex(publicKey1));
		System.out.println("DH 甲方私钥: " + BytesToHex.fromBytesToHex(privateKey1));

		Map<String, Object> keyMap2 = DHUtil.initKey(publicKey1);
		publicKey2 = DHUtil.getPublicKey(keyMap2);
		privateKey2 = DHUtil.getPrivateKey(keyMap2);
		System.out.println("DH 乙方公钥: " + BytesToHex.fromBytesToHex(publicKey2));
		System.out.println("DH 乙方私钥: " + BytesToHex.fromBytesToHex(privateKey2));

		secretKey1 = DHUtil.getSecretKey(publicKey2, privateKey1);
		System.out.println("DH 甲方 本地 AES 密钥: " + BytesToHex.fromBytesToHex(secretKey1));

		secretKey2 = DHUtil.getSecretKey(publicKey1, privateKey2);
		System.out.println("DH 乙方 本地 AES 密钥: " + BytesToHex.fromBytesToHex(secretKey2));

		System.out.println("双方派生密钥是否相同: " + Arrays.equals(secretKey1, secretKey2));

		byte[] encrypted = AESUtil.encryptAES(DATA.getBytes(StandardCharsets.UTF_8), secretKey1);
		System.out.println(DATA + " 用甲方密钥 AES-GCM 加密: " + BytesToHex.fromBytesToHex(encrypted));
		byte[] plain = AESUtil.decryptAES(encrypted, secretKey2);
		System.out.println(DATA + " 用乙方密钥 AES-GCM 解密: " + new String(plain, StandardCharsets.UTF_8));
	}
}

package elven.test;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import elven.encryption.BytesToHex;
import elven.encryption.RSAUtil;

/**
 * RSA-OAEP 公钥加密 / 私钥解密演示（2048 bit）。
 */
public class testRSA {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) throws Exception {
		Map<String, Object> keyMap = RSAUtil.initKey();

		RSAPublicKey rsaPublicKey = RSAUtil.getpublicKey(keyMap);
		RSAPrivateKey rsaPrivateKey = RSAUtil.getPrivateKey(keyMap);
		System.out.println("RSA 模数位数: " + rsaPublicKey.getModulus().bitLength());
		System.out.println("变换: " + RSAUtil.TRANSFORMATION);

		byte[] rsaResult = RSAUtil.encrypt(DATA.getBytes(StandardCharsets.UTF_8), rsaPublicKey);
		System.out.println(DATA + "====>>>> RSA 加密>>>>====" + BytesToHex.fromBytesToHex(rsaResult));

		byte[] plainResult = RSAUtil.decrypt(rsaResult, rsaPrivateKey);
		System.out.println(DATA + "====>>>> RSA 解密>>>>====" + new String(plainResult, StandardCharsets.UTF_8));
	}
}

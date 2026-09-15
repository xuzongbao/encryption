package elven.test;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import elven.encryption.BytesToHex;
import elven.encryption.RSAUtil;

/**
 * RSA-PSS 签名 / 验签演示。私钥签名、公钥验证；这和加密是两件事。
 */
public class testRSASign {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) throws Exception {
		Map<String, Object> keyMap = RSAUtil.initKey();
		RSAPublicKey publicKey = RSAUtil.getpublicKey(keyMap);
		RSAPrivateKey privateKey = RSAUtil.getPrivateKey(keyMap);

		System.out.println("RSA 模数位数: " + publicKey.getModulus().bitLength());
		System.out.println("加密变换: " + RSAUtil.TRANSFORMATION);
		System.out.println("签名变换: " + RSAUtil.SIGNATURE_ALGORITHM);

		byte[] data = DATA.getBytes(StandardCharsets.UTF_8);
		byte[] signature = RSAUtil.sign(data, privateKey);
		System.out.println(DATA + "====>>>> RSA-PSS 签名>>>>====" + BytesToHex.fromBytesToHex(signature));
		System.out.println("公钥验签(原文未改): " + RSAUtil.verify(data, signature, publicKey));

		byte[] tampered = data.clone();
		tampered[0] = (byte) (tampered[0] ^ 0x01);
		System.out.println("公钥验签(翻转第 1 字节): " + RSAUtil.verify(tampered, signature, publicKey));

		byte[] cipher = RSAUtil.encrypt(data, publicKey);
		byte[] plain = RSAUtil.decrypt(cipher, privateKey);
		System.out.println("同一对密钥仍可做 OAEP 加密，解密结果: "
				+ new String(plain, StandardCharsets.UTF_8));
		System.out.println("签名证明「是谁签的 / 有没有被改」，加密证明「只有私钥持有者能读」——不要混用。");
	}
}

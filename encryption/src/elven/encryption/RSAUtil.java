package elven.encryption;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * RSA 非对称加密演示：2048 bit 密钥 + OAEP(SHA-256)。
 * <p>
 * 只演示「公钥加密、私钥解密」一小段数据。RSA 不适合直接加密大文件；
 * 实际系统通常用 RSA 保护对称密钥，再用 AES 加密正文。
 * <p>
 * 变换名里的 {@code ECB} 是 Java 的历史命名，并不表示 RSA 在做分组 ECB。
 * <p>
 * 这是教学示例，不是生产级加密库。
 */
public class RSAUtil {

	public static final String PUBLIC_KEY = "RSAPublicKey";
	public static final String PRIVATE_KEY = "RSAPrivateKey";

	/** 现代入门建议至少 2048 bit；旧代码是 1024。 */
	public static final int KEY_SIZE_BITS = 2048;

	/**
	 * 明确写出填充，避免 {@code Cipher.getInstance("RSA")} 的提供者默认值不透明。
	 * OAEP 比 PKCS#1 v1.5 加密填充更不容易被选择密文攻击。
	 */
	public static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

	private static final OAEPParameterSpec OAEP_SHA256 = new OAEPParameterSpec(
			"SHA-256",
			"MGF1",
			MGF1ParameterSpec.SHA256,
			PSource.PSpecified.DEFAULT);

	/**
	 * 生成 RSA 公钥和私钥。
	 */
	public static Map<String, Object> initKey() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(KEY_SIZE_BITS);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}

	/**
	 * 获得公钥。方法名保持历史拼写 {@code getpublicKey}，方便对照旧示例。
	 */
	public static RSAPublicKey getpublicKey(Map<String, Object> keyMap) {
		RSAPublicKey publicKey = (RSAPublicKey) keyMap.get(PUBLIC_KEY);
		return publicKey;
	}

	/**
	 * 获得私钥。
	 */
	public static RSAPrivateKey getPrivateKey(Map<String, Object> keyMap) {
		RSAPrivateKey privateKey = (RSAPrivateKey) keyMap.get(PRIVATE_KEY);
		return privateKey;
	}

	/**
	 * 公钥加密。2048-bit OAEP(SHA-256) 单块大约只能装 190 字节明文。
	 */
	public static byte[] encrypt(byte[] data, RSAPublicKey publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey, OAEP_SHA256);
		return cipher.doFinal(data);
	}

	/**
	 * 私钥解密。
	 */
	public static byte[] decrypt(byte[] data, RSAPrivateKey privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP_SHA256);
		return cipher.doFinal(data);
	}
}

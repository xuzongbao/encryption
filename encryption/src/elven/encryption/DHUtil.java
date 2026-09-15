package elven.encryption;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

/**
 * Diffie–Hellman 密钥协商演示。
 * <p>
 * 流程：甲方生成 DH 密钥对 → 乙方用甲方公钥参数生成自己的密钥对 →
 * 双方用「对方公钥 + 自己私钥」算出同一份共享密钥材料 → 再派生出 AES-128 密钥。
 * <p>
 * 现代 JDK（例如 OpenJDK 21）里 {@code KeyAgreement.generateSecret("DES")} /
 * {@code generateSecret("AES")} 会被拒绝（不再做不安全的隐式截断 KDF）。
 * 本示例改为 {@code generateSecret()} 取出原始共享字节，再用 SHA-256 取前 16 字节
 * 作为 AES 密钥。这是<b>简化的教学 KDF</b>，不是 HKDF，不能当生产方案。
 * <p>
 * DH 模数使用 2048 bit。这是教学示例，不是生产级密钥交换库。
 * 新系统请优先看 {@link ECDHUtil}（命名曲线、更小的公钥）；本类保留作经典对照。
 */
public class DHUtil {

	public static final String PUBLIC_KEY = "DHPublicKey";
	public static final String PRIVATE_KEY = "DHPrivateKey";

	/** 旧代码是 1024 bit，偏短；演示改为 2048。 */
	public static final int KEY_SIZE_BITS = 2048;

	/** 从共享密钥材料派生出的对称算法，供后续 AESUtil 使用。 */
	public static final String SECRET_ALGORITHM = "AES";

	/** SHA-256 摘要后取前 16 字节，对应 AES-128。 */
	public static final int AES_KEY_LENGTH_BYTES = 16;

	/**
	 * 甲方初始化并返回密钥对。
	 */
	public static Map<String, Object> initKey() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
		keyPairGenerator.initialize(KEY_SIZE_BITS);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		DHPublicKey publicKey = (DHPublicKey) keyPair.getPublic();
		DHPrivateKey privateKey = (DHPrivateKey) keyPair.getPrivate();
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}

	/**
	 * 乙方根据甲方公钥初始化并返回密钥对（复用甲方的 DH 参数 p、g）。
	 */
	public static Map<String, Object> initKey(byte[] key) throws Exception {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
		KeyFactory keyFactory = KeyFactory.getInstance("DH");
		DHPublicKey dhPublicKey = (DHPublicKey) keyFactory.generatePublic(keySpec);
		DHParameterSpec dhParameterSpec = dhPublicKey.getParams();
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
		keyPairGenerator.initialize(dhParameterSpec);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		DHPublicKey publicKey = (DHPublicKey) keyPair.getPublic();
		DHPrivateKey privateKey = (DHPrivateKey) keyPair.getPrivate();
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}

	/**
	 * 根据对方的公钥和自己的私钥，派生出 AES-128 密钥。
	 *
	 * @return 16 字节 AES 密钥，可交给 {@link AESUtil#encryptAES(byte[], byte[])}
	 */
	public static byte[] getSecretKey(byte[] publicKey, byte[] privateKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("DH");
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
		PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
		PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(privateKey);
		PrivateKey priKey = keyFactory.generatePrivate(priKeySpec);

		KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
		keyAgreement.init(priKey);
		keyAgreement.doPhase(pubKey, true);

		// 原始共享密钥材料（不要再 generateSecret("DES")）
		byte[] sharedSecret = keyAgreement.generateSecret();
		return deriveAesKey(sharedSecret);
	}

	/**
	 * 教学用 KDF：SHA-256(sharedSecret) 的前 16 字节。
	 * 生产环境请使用 HKDF 等标准构造。
	 */
	static byte[] deriveAesKey(byte[] sharedSecret) throws Exception {
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] digest = sha256.digest(sharedSecret);
		return Arrays.copyOf(digest, AES_KEY_LENGTH_BYTES);
	}

	/**
	 * 从 Map 中取得公钥编码。
	 */
	public static byte[] getPublicKey(Map<String, Object> keyMap) {
		DHPublicKey key = (DHPublicKey) keyMap.get(PUBLIC_KEY);
		return key.getEncoded();
	}

	/**
	 * 从 Map 中取得私钥编码。
	 */
	public static byte[] getPrivateKey(Map<String, Object> keyMap) {
		DHPrivateKey key = (DHPrivateKey) keyMap.get(PRIVATE_KEY);
		return key.getEncoded();
	}
}

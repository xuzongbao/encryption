package elven.encryption;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyAgreement;

/**
 * ECDH（椭圆曲线 Diffie–Hellman）密钥协商演示。
 * <p>
 * 和 {@link DHUtil} 做的是<b>同一件事</b>：双方各自持有一对密钥，交换公钥后算出同一份
 * 共享秘密，再派生出 AES-128 密钥去加密正文。差别在数学和参数形态：
 * <ul>
 *   <li>{@link DHUtil}：经典有限域 DH。模数 2048 bit，乙方必须复用甲方的 p、g。</li>
 *   <li>本类：椭圆曲线 ECDH。双方独立在同一条<b>命名曲线</b>上生成密钥对，不必传递大素数。</li>
 * </ul>
 * 现代协议默认走椭圆曲线（TLS 1.3、很多即时通讯），经典 DH 更适合当教材对照。
 * <p>
 * 曲线选用 {@code secp256r1}（NIST P-256）。它是 OpenJDK 17/21 里最常见、无需第三方库
 * 就能 {@code KeyPairGenerator.getInstance("EC")} 的命名曲线。公钥体积远小于 DH-2048。
 * <p>
 * 现代 JDK 会拒绝 {@code KeyAgreement.generateSecret("AES")} 这种隐式截断。
 * 本示例与 {@link DHUtil} 相同：{@code generateSecret()} 取出原始共享字节，再 SHA-256
 * 取前 16 字节当 AES 密钥。这是<b>简化的教学 KDF</b>，不是 HKDF（RFC 5869），
 * 不能当生产方案。
 * <p>
 * 额外提供一对很小的 ECDSA（{@code SHA256withECDSA}）方法，用<b>同一对</b> EC 密钥
 * 演示「私钥签名、公钥验签」。生产环境应把「密钥协商」和「签名」分成两套密钥，
 * 这里合在一起只是为了少生成一对钥匙、方便对照。
 * <p>
 * 这是教学示例，不是生产级密钥交换库：没有身份认证、没有证书、防不了中间人。
 */
public class ECDHUtil {

	public static final String PUBLIC_KEY = "ECPublicKey";
	public static final String PRIVATE_KEY = "ECPrivateKey";

	/**
	 * 命名曲线：secp256r1 / NIST P-256 / prime256v1。
	 * OpenJDK 的 {@link ECGenParameterSpec} 用这个标准名。
	 */
	public static final String CURVE_NAME = "secp256r1";

	/** 从共享密钥材料派生出的对称算法，供后续 AESUtil 使用。 */
	public static final String SECRET_ALGORITHM = "AES";

	/** SHA-256 摘要后取前 16 字节，对应 AES-128。 */
	public static final int AES_KEY_LENGTH_BYTES = 16;

	/**
	 * ECDSA 签名算法。哈希写在名字里（SHA-256），编码是常见的 DER。
	 * 这和 RSA 那边的 {@code RSASSA-PSS} 是两套算法，不要混用密钥。
	 */
	public static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

	/**
	 * 在命名曲线上生成一对密钥。甲、乙双方都直接调用本方法即可，
	 * 不必像 {@link DHUtil#initKey(byte[])} 那样先拿到对方公钥再生成。
	 */
	public static Map<String, Object> initKey() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
		keyPairGenerator.initialize(new ECGenParameterSpec(CURVE_NAME));
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
		ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
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
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
		PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
		PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(privateKey);
		PrivateKey priKey = keyFactory.generatePrivate(priKeySpec);

		KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
		keyAgreement.init(priKey);
		keyAgreement.doPhase(pubKey, true);

		// 原始共享密钥材料（不要再 generateSecret("AES")）
		byte[] sharedSecret = keyAgreement.generateSecret();
		return deriveAesKey(sharedSecret);
	}

	/**
	 * 教学用 KDF：SHA-256(sharedSecret) 的前 16 字节。
	 * 与 {@link DHUtil} 相同，便于对照；生产环境请使用 HKDF 等标准构造。
	 */
	static byte[] deriveAesKey(byte[] sharedSecret) throws Exception {
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] digest = sha256.digest(sharedSecret);
		return Arrays.copyOf(digest, AES_KEY_LENGTH_BYTES);
	}

	/**
	 * 用己方私钥对数据做 ECDSA（SHA-256）签名。
	 * <p>
	 * 教学捷径：可以和 ECDH 共用同一对 P-256 密钥。生产环境请分开两套钥匙。
	 */
	public static byte[] sign(byte[] data, byte[] privateKey) throws Exception {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		PrivateKey priKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(priKey);
		signature.update(data);
		return signature.sign();
	}

	/**
	 * 用对方公钥验证 ECDSA（SHA-256）签名。
	 *
	 * @return {@code true} 表示签名与数据匹配
	 */
	public static boolean verify(byte[] data, byte[] signatureBytes, byte[] publicKey) throws Exception {
		if (data == null || signatureBytes == null) {
			throw new IllegalArgumentException("data and signature must not be null");
		}
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(pubKey);
		signature.update(data);
		return signature.verify(signatureBytes);
	}

	/**
	 * 从 Map 中取得公钥编码（X.509 SubjectPublicKeyInfo）。
	 */
	public static byte[] getPublicKey(Map<String, Object> keyMap) {
		ECPublicKey key = (ECPublicKey) keyMap.get(PUBLIC_KEY);
		return key.getEncoded();
	}

	/**
	 * 从 Map 中取得私钥编码（PKCS#8）。
	 */
	public static byte[] getPrivateKey(Map<String, Object> keyMap) {
		ECPrivateKey key = (ECPrivateKey) keyMap.get(PRIVATE_KEY);
		return key.getEncoded();
	}
}

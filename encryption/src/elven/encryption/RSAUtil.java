package elven.encryption;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * RSA 非对称演示：2048 bit 密钥。
 * <p>
 * 同一对密钥可以做两件<b>不同</b>的事：
 * <ul>
 *   <li>加密：公钥加密、私钥解密，变换 {@link #TRANSFORMATION}（OAEP + SHA-256）</li>
 *   <li>签名：私钥签名、公钥验签，变换 {@link #SIGNATURE_ALGORITHM}（RSASSA-PSS + SHA-256）</li>
 * </ul>
 * 签名不能代替加密（任何人都能用公钥验签，签名本身不保密）。
 * RSA 也不适合直接加密大文件；实际系统通常用 RSA 保护对称密钥，再用 AES 加密正文。
 * <p>
 * 加密变换名里的 {@code ECB} 是 Java 的历史命名，并不表示 RSA 在做分组 ECB。
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

	/**
	 * 数字签名算法名：{@code RSASSA-PSS}（JDK 11+ 的 JCA 标准名）。
	 * 哈希算法不写在名字里，而在 {@link PSSParameterSpec} 中明确为 SHA-256 / MGF1-SHA256。
	 * 比 PKCS#1 v1.5 签名（{@code SHA256withRSA}）更不容易被填充伪造攻击。
	 * <p>
	 * 注意：部分文档会写 {@code SHA256withRSA/PSS}，但 OpenJDK 的 SunRsaSign
	 * 不注册这个别名；本示例使用能 {@code getInstance} 成功的 {@code RSASSA-PSS}。
	 */
	public static final String SIGNATURE_ALGORITHM = "RSASSA-PSS";

	/** PSS 盐长度等于 SHA-256 输出（32 字节），这是常见推荐。 */
	public static final int PSS_SALT_LENGTH_BYTES = 32;

	private static final OAEPParameterSpec OAEP_SHA256 = new OAEPParameterSpec(
			"SHA-256",
			"MGF1",
			MGF1ParameterSpec.SHA256,
			PSource.PSpecified.DEFAULT);

	private static final PSSParameterSpec PSS_SHA256 = new PSSParameterSpec(
			"SHA-256",
			"MGF1",
			MGF1ParameterSpec.SHA256,
			PSS_SALT_LENGTH_BYTES,
			1);

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

	/**
	 * 用私钥对数据做 RSASSA-PSS（SHA-256）签名。
	 */
	public static byte[] sign(byte[] data, RSAPrivateKey privateKey) throws Exception {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		Signature signature = newPssSignature();
		signature.initSign(privateKey);
		signature.update(data);
		return signature.sign();
	}

	/**
	 * 用公钥验证 RSASSA-PSS（SHA-256）签名。
	 *
	 * @return {@code true} 表示签名与数据匹配
	 */
	public static boolean verify(byte[] data, byte[] signatureBytes, RSAPublicKey publicKey) throws Exception {
		if (data == null || signatureBytes == null) {
			throw new IllegalArgumentException("data and signature must not be null");
		}
		Signature signature = newPssSignature();
		signature.initVerify(publicKey);
		signature.update(data);
		return signature.verify(signatureBytes);
	}

	private static Signature newPssSignature() throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.setParameter(PSS_SHA256);
		return signature;
	}
}

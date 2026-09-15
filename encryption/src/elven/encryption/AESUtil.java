package elven.encryption;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 对称加密演示：AES-128 / GCM / NoPadding。
 * <p>
 * GCM 同时提供保密性和完整性（认证加密）。每次加密都会生成一个新的 12 字节 IV，
 * 并放在密文最前面。
 * <p>
 * <b>线格式（encryptAES 的返回值 / decryptAES 的入参）：</b>
 * <pre>
 *   [12 字节 IV] + [密文 || 16 字节 GCM tag]
 * </pre>
 * Java 的 {@code Cipher.doFinal} 会把 GCM tag 附加在密文末尾，调用方不必单独处理 tag。
 * <p>
 * 这是教学示例，不是生产级加密库：没有密钥管理、没有密钥派生、没有关联数据（AAD）。
 */
public class AESUtil {

	/** 明确写出算法 / 模式 / 填充，避免 {@code Cipher.getInstance("AES")} 落到 ECB。 */
	public static final String TRANSFORMATION = "AES/GCM/NoPadding";

	/** 演示用 AES-128。现代 JDK 上 192/256 一般也可以，这里保持简单。 */
	public static final int KEY_SIZE_BITS = 128;

	/** GCM 推荐 IV 长度：12 字节（96 bit）。 */
	public static final int GCM_IV_LENGTH_BYTES = 12;

	/** GCM 认证 tag 长度：128 bit（16 字节，由 Cipher 附加在密文后）。 */
	public static final int GCM_TAG_LENGTH_BITS = 128;

	/**
	 * 生成随机 AES-128 密钥。
	 */
	public static byte[] initKey() throws Exception {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(KEY_SIZE_BITS);
		SecretKey secretKey = keyGen.generateKey();
		return secretKey.getEncoded();
	}

	/**
	 * 加密。返回值已按线格式拼好：IV || 密文+tag。
	 */
	public static byte[] encryptAES(byte[] data, byte[] key) throws Exception {
		checkKey(key);
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}

		SecretKey secretKey = new SecretKeySpec(key, "AES");
		byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
		new SecureRandom().nextBytes(iv);

		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
		byte[] ciphertext = cipher.doFinal(data);

		byte[] packed = new byte[iv.length + ciphertext.length];
		System.arraycopy(iv, 0, packed, 0, iv.length);
		System.arraycopy(ciphertext, 0, packed, iv.length, ciphertext.length);
		return packed;
	}

	/**
	 * 解密。{@code data} 必须是 {@link #encryptAES} 产出的线格式。
	 */
	public static byte[] decryptAES(byte[] data, byte[] key) throws Exception {
		checkKey(key);
		if (data == null || data.length < GCM_IV_LENGTH_BYTES + 16) {
			throw new IllegalArgumentException("ciphertext too short for AES-GCM (need IV + tag)");
		}

		byte[] iv = Arrays.copyOfRange(data, 0, GCM_IV_LENGTH_BYTES);
		byte[] ciphertext = Arrays.copyOfRange(data, GCM_IV_LENGTH_BYTES, data.length);

		SecretKey secretKey = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
		return cipher.doFinal(ciphertext);
	}

	private static void checkKey(byte[] key) {
		if (key == null || key.length != KEY_SIZE_BITS / 8) {
			throw new IllegalArgumentException("AES key must be " + (KEY_SIZE_BITS / 8) + " bytes");
		}
	}
}

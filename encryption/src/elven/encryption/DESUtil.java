package elven.encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES 算法演示（<b>不安全 / 仅供对照教材</b>）。
 * <p>
 * DES 只有 56 bit 有效密钥，早已能被穷举。这里故意使用明确的
 * {@code DES/ECB/PKCS5Padding}，方便看到「相同明文块 → 相同密文块」的 ECB 问题。
 * <p>
 * <b>不要</b>在新系统里使用 DES。学习对称加密请看 {@link AESUtil}。
 */
public class DESUtil {

	/** 明确写出 ECB，避免 {@code Cipher.getInstance("DES")} 把默认模式藏起来。 */
	public static final String TRANSFORMATION = "DES/ECB/PKCS5Padding";

	/** DES 有效密钥长度就是 56 bit，这是它不安全的原因之一。 */
	public static final int KEY_SIZE_BITS = 56;

	/**
	 * 生成 DES 密钥。仅用于本演示。
	 */
	public static byte[] initKey() throws Exception {
		KeyGenerator keyGen = KeyGenerator.getInstance("DES");
		keyGen.init(KEY_SIZE_BITS);
		SecretKey secretKey = keyGen.generateKey();
		return secretKey.getEncoded();
	}

	/**
	 * DES-ECB 加密。无 IV；相同 8 字节明文块会得到相同密文块。
	 */
	public static byte[] encryptDES(byte[] data, byte[] key) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, "DES");
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher.doFinal(data);
	}

	/**
	 * DES-ECB 解密。
	 */
	public static byte[] decryptDES(byte[] data, byte[] key) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, "DES");
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(data);
	}
}

package elven.encryption;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 3DES（DESede）算法演示（<b>遗留算法 / 不推荐用于新系统</b>）。
 * <p>
 * 3DES 已被弃用，新项目应使用 AES。这里仍保留它，是因为很多教材会从 DES → 3DES → AES
 * 讲过来。为避免默认 ECB，演示使用 {@code DESede/CBC/PKCS5Padding}，并在密文前附上随机 IV。
 * <p>
 * <b>线格式（encrypt3DES 的返回值 / decrypt3DES 的入参）：</b>
 * <pre>
 *   [8 字节 IV] + [PKCS5 填充后的 CBC 密文]
 * </pre>
 * 学习现代对称加密请看 {@link AESUtil}。
 */
public class DESede {

	/** 明确写出 CBC，避免 {@code Cipher.getInstance("DESede")} 落到 ECB。 */
	public static final String TRANSFORMATION = "DESede/CBC/PKCS5Padding";

	/** 168 bit 三密钥 3DES（KeyGenerator 可指定 112 或 168）。 */
	public static final int KEY_SIZE_BITS = 168;

	/** DES 分组是 8 字节，CBC IV 同长。 */
	public static final int IV_LENGTH_BYTES = 8;

	/**
	 * 生成 3DES 密钥。仅用于本演示。
	 */
	public static byte[] initKey() throws Exception {
		KeyGenerator keyGen = KeyGenerator.getInstance("DESede");
		keyGen.init(KEY_SIZE_BITS);
		SecretKey secretKey = keyGen.generateKey();
		return secretKey.getEncoded();
	}

	/**
	 * 3DES-CBC 加密。返回 IV || 密文。
	 */
	public static byte[] encrypt3DES(byte[] data, byte[] key) throws Exception {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		SecretKey secretKey = new SecretKeySpec(key, "DESede");
		byte[] iv = new byte[IV_LENGTH_BYTES];
		new SecureRandom().nextBytes(iv);

		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
		byte[] ciphertext = cipher.doFinal(data);

		byte[] packed = new byte[iv.length + ciphertext.length];
		System.arraycopy(iv, 0, packed, 0, iv.length);
		System.arraycopy(ciphertext, 0, packed, iv.length, ciphertext.length);
		return packed;
	}

	/**
	 * 3DES-CBC 解密。{@code data} 必须是 {@link #encrypt3DES} 产出的线格式。
	 */
	public static byte[] decrypt3DES(byte[] data, byte[] key) throws Exception {
		if (data == null || data.length < IV_LENGTH_BYTES + IV_LENGTH_BYTES) {
			throw new IllegalArgumentException("ciphertext too short for 3DES-CBC (need IV + one block)");
		}
		byte[] iv = Arrays.copyOfRange(data, 0, IV_LENGTH_BYTES);
		byte[] ciphertext = Arrays.copyOfRange(data, IV_LENGTH_BYTES, data.length);

		SecretKey secretKey = new SecretKeySpec(key, "DESede");
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
		return cipher.doFinal(ciphertext);
	}
}

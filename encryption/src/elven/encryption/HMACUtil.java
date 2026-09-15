package elven.encryption;

import java.security.MessageDigest;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC-SHA-256 演示：带密钥的消息认证码。
 * <p>
 * HMAC 能检测篡改，并证明持有同一把密钥的一方才能算出这个值；
 * <b>它不加密</b>，原文仍然是明文。需要同时保密和防篡改时，
 * 请优先看 {@link AESUtil} 的 AES-GCM（AEAD），而不是自己拼「先加密再 HMAC」。
 * <p>
 * 这是教学示例，不是生产级密码学库。
 */
public class HMACUtil {

	public static final String ALGORITHM = "HmacSHA256";

	/** 演示用 HMAC 密钥长度：256 bit。 */
	public static final int KEY_SIZE_BITS = 256;

	/** HMAC-SHA-256 输出固定 32 字节。 */
	public static final int MAC_LENGTH_BYTES = 32;

	/**
	 * 生成随机 HMAC-SHA-256 密钥。
	 */
	public static byte[] initKey() throws Exception {
		KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
		keyGen.init(KEY_SIZE_BITS);
		SecretKey secretKey = keyGen.generateKey();
		return secretKey.getEncoded();
	}

	/**
	 * 计算 HMAC-SHA-256。
	 *
	 * @return 32 字节认证码
	 */
	public static byte[] hmac(byte[] data, byte[] key) throws Exception {
		checkKey(key);
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		Mac mac = Mac.getInstance(ALGORITHM);
		mac.init(new SecretKeySpec(key, ALGORITHM));
		return mac.doFinal(data);
	}

	/**
	 * 计算 HMAC 并转成小写十六进制，方便打印对照。
	 */
	public static String hmacHex(byte[] data, byte[] key) throws Exception {
		return BytesToHex.fromBytesToHex(hmac(data, key));
	}

	/**
	 * 用同一把密钥重算 HMAC，再用 {@link MessageDigest#isEqual} 比较。
	 * <p>
	 * {@code isEqual} 会扫完整段字节再返回，避免简单 {@code Arrays.equals}
	 * 在第一个不同字节就返回带来的时序差异。这是教学级别的「尽量等时」比较，
	 * 不是完整的侧信道防护。
	 */
	public static boolean verify(byte[] data, byte[] key, byte[] expectedMac) throws Exception {
		if (expectedMac == null) {
			throw new IllegalArgumentException("expectedMac must not be null");
		}
		byte[] actual = hmac(data, key);
		return MessageDigest.isEqual(actual, expectedMac);
	}

	private static void checkKey(byte[] key) {
		if (key == null || key.length != KEY_SIZE_BITS / 8) {
			throw new IllegalArgumentException("HMAC-SHA-256 key must be " + (KEY_SIZE_BITS / 8) + " bytes");
		}
	}
}

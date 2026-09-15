package elven.encryption;

import java.security.MessageDigest;

/**
 * SHA-256 摘要（哈希）演示。
 * <p>
 * <b>哈希不是加密。</b>摘要是单向的：同一段输入永远得到同一段 32 字节输出，
 * 但不能从摘要还原原文，也没有密钥。任何人都能对同一段数据算出同一个值。
 * 它解决的是「内容有没有被改过」这类完整性问题，不能当保密手段。
 * 若还需要「只有持有密钥的人才能算出认证码」，请看 {@link HMACUtil}；
 * 若需要「用私钥证明是我签的」，请看 {@link RSAUtil} 的签名方法。
 * <p>
 * 这是教学示例，不是生产级密码学库。
 */
public class SHA256Util {

	public static final String ALGORITHM = "SHA-256";

	/** SHA-256 输出固定 32 字节（256 bit）。 */
	public static final int DIGEST_LENGTH_BYTES = 32;

	/**
	 * 计算 SHA-256 摘要。
	 *
	 * @return 32 字节摘要
	 */
	public static byte[] hash(byte[] data) throws Exception {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		MessageDigest md = MessageDigest.getInstance(ALGORITHM);
		return md.digest(data);
	}

	/**
	 * 计算 SHA-256 摘要并转成小写十六进制，方便打印对照。
	 */
	public static String hashHex(byte[] data) throws Exception {
		return BytesToHex.fromBytesToHex(hash(data));
	}
}

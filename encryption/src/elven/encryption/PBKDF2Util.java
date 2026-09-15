package elven.encryption;

import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 口令派生密钥演示：PBKDF2-HMAC-SHA256。
 * <p>
 * 人能记住的口令熵很低，不能直接当 AES 密钥用。PBKDF2 用「口令 + 盐 + 很多次 HMAC」
 * 慢慢算出一段伪随机字节，再交给 {@link AESUtil} 这类对称算法去加密。
 * <p>
 * <b>它做什么 / 不做什么：</b>
 * <ul>
 *   <li>做：把口令变成固定长度的密钥材料（password → key）</li>
 *   <li>不做：它<b>不是</b> AES，也不替代 AES。派生出密钥之后，仍然要用 AES-GCM 等算法加密明文</li>
 *   <li>不做：不是「把口令 SHA-256 一下存进数据库」的完整口令存储方案。若只存哈希，至少还要存盐、迭代次数、算法名</li>
 * </ul>
 * <p>
 * <b>盐（salt）必须和密文一起保存。</b>解密时要用同一份盐才能重新派生出同一把密钥。
 * 盐不是密钥、也不用保密；但每个口令 / 每次加密都应使用新的随机盐，不要假设「可以省略盐」
 * 或「全库共用一个盐」。
 * <p>
 * 演示默认 {@value #ITERATION_COUNT} 次迭代，在普通机器上通常不到 1 秒。
 * 真实系统请按当时的 <a href="https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html">OWASP 口令存储建议</a>
 * 把迭代次数再提高（或改用 Argon2id 等内存硬化算法）。这是教学示例，不是生产级口令库。
 */
public class PBKDF2Util {

	/** JDK 标准算法名。PRF 是 HMAC-SHA256。 */
	public static final String ALGORITHM = "PBKDF2WithHmacSHA256";

	/** 盐长度：16 字节（128 bit）。教学下限；再短会削弱「同口令不同盐 → 不同密钥」的效果。 */
	public static final int SALT_LENGTH_BYTES = 16;

	/**
	 * 演示用迭代次数。故意选「能在云虚拟机上约 1 秒内跑完」的数量，方便看 demo。
	 * 生产环境应显著更高，并随硬件变快而调大；请查 OWASP 当年推荐值，不要照抄这个数字。
	 */
	public static final int ITERATION_COUNT = 100_000;

	/** 默认输出长度：128 bit，正好喂给 {@link AESUtil} 的 AES-128。 */
	public static final int DEFAULT_KEY_LENGTH_BITS = AESUtil.KEY_SIZE_BITS;

	/**
	 * 生成一份新的随机盐。每次派生都应调用一次，不要复用上一份盐。
	 */
	public static byte[] generateSalt() {
		byte[] salt = new byte[SALT_LENGTH_BYTES];
		new SecureRandom().nextBytes(salt);
		return salt;
	}

	/**
	 * 从口令 + 盐派生密钥，默认长度 {@link #DEFAULT_KEY_LENGTH_BITS}（AES-128）。
	 */
	public static byte[] deriveKey(String password, byte[] salt) throws Exception {
		return deriveKey(password, salt, DEFAULT_KEY_LENGTH_BITS);
	}

	/**
	 * 从口令 + 盐派生指定比特长度的密钥。
	 *
	 * @param password    口令；内部会转成 {@code char[]} 再交给 {@code PBEKeySpec}
	 * @param salt        随机盐，解密时必须原样提供；长度至少 {@link #SALT_LENGTH_BYTES}
	 * @param keyLenBits  输出密钥比特数，例如 128 对应 16 字节 AES 密钥
	 * @return 原始密钥字节，可直接交给 {@link AESUtil#encryptAES(byte[], byte[])}
	 */
	public static byte[] deriveKey(String password, byte[] salt, int keyLenBits) throws Exception {
		if (password == null) {
			throw new IllegalArgumentException("password must not be null");
		}
		return deriveKey(password.toCharArray(), salt, keyLenBits);
	}

	/**
	 * {@code char[]} 版本，演示结束后可 {@code Arrays.fill} 清掉口令。
	 */
	public static byte[] deriveKey(char[] password, byte[] salt, int keyLenBits) throws Exception {
		if (password == null || password.length == 0) {
			throw new IllegalArgumentException("password must not be empty");
		}
		if (salt == null || salt.length < SALT_LENGTH_BYTES) {
			throw new IllegalArgumentException("salt must be at least " + SALT_LENGTH_BYTES + " bytes");
		}
		if (keyLenBits <= 0 || keyLenBits % 8 != 0) {
			throw new IllegalArgumentException("keyLenBits must be a positive multiple of 8");
		}

		PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATION_COUNT, keyLenBits);
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
			return factory.generateSecret(spec).getEncoded();
		} finally {
			spec.clearPassword();
		}
	}
}

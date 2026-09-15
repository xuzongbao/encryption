package elven.test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import elven.encryption.AESUtil;
import elven.encryption.BytesToHex;
import elven.encryption.PBKDF2Util;

/**
 * PBKDF2 口令派生密钥演示：口令 → PBKDF2 → AES-GCM 加解密。
 * <p>
 * 要点：盐必须和密文一起保存；解密时用同一口令 + 同一份盐重新派生密钥。
 * 口令错了，AES-GCM 会因认证失败而解不开（这是预期行为）。
 */
public class testPBKDF2 {

	public static final String DATA = "hi, welcome to my git area!";

	/** 演示口令。真实系统里口令来自用户输入，不要写进源码。 */
	public static final String PASSWORD = "correct horse battery staple";

	public static void main(String[] args) throws Exception {
		System.out.println("算法: " + PBKDF2Util.ALGORITHM);
		System.out.println("盐长度: " + PBKDF2Util.SALT_LENGTH_BYTES + " 字节");
		System.out.println("迭代次数: " + PBKDF2Util.ITERATION_COUNT
				+ "（演示用，生产请按 OWASP 再提高）");
		System.out.println("输出密钥: " + PBKDF2Util.DEFAULT_KEY_LENGTH_BITS + " bit，交给 AES-GCM");
		System.out.println();

		byte[] salt = PBKDF2Util.generateSalt();
		System.out.println("随机盐: " + BytesToHex.fromBytesToHex(salt));
		System.out.println("盐不是密钥，但解密时必须带着它；请和密文一起保存。");

		long started = System.nanoTime();
		byte[] aesKey = PBKDF2Util.deriveKey(PASSWORD, salt, PBKDF2Util.DEFAULT_KEY_LENGTH_BITS);
		long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
		System.out.println("PBKDF2 派生 AES 密钥: " + BytesToHex.fromBytesToHex(aesKey));
		System.out.println("本次派生耗时: " + elapsedMs + " ms");

		// 对照：对口令做一次 SHA-256 又快又没有盐/迭代参数。不能当密钥派生，也不能当口令存储。
		byte[] naiveHash = MessageDigest.getInstance("SHA-256")
				.digest(PASSWORD.getBytes(StandardCharsets.UTF_8));
		System.out.println("对照 SHA-256(口令)（无盐、无迭代，不要这样用）: "
				+ BytesToHex.fromBytesToHex(naiveHash));
		System.out.println();

		byte[] packed = AESUtil.encryptAES(DATA.getBytes(StandardCharsets.UTF_8), aesKey);
		System.out.println(DATA + " AES-GCM 加密: " + BytesToHex.fromBytesToHex(packed));
		System.out.println("密文线格式仍是 AESUtil 的 IV || 密文+tag；盐要另外保存。");

		// 解密方只有口令和盐，没有「密钥文件」。必须重新派生。
		byte[] derivedAgain = PBKDF2Util.deriveKey(PASSWORD, salt);
		System.out.println("同一口令 + 同一盐，密钥是否相同: " + Arrays.equals(aesKey, derivedAgain));

		byte[] plain = AESUtil.decryptAES(packed, derivedAgain);
		System.out.println(DATA + " AES-GCM 解密: " + new String(plain, StandardCharsets.UTF_8));
		System.out.println();

		try {
			byte[] wrongKey = PBKDF2Util.deriveKey("wrong password", salt);
			AESUtil.decryptAES(packed, wrongKey);
			System.out.println("错误：错误口令竟然解密成功了");
		} catch (Exception e) {
			System.out.println("错误口令无法解密（AES-GCM 认证失败）: " + e.getClass().getSimpleName());
		}

		try {
			byte[] otherSalt = PBKDF2Util.generateSalt();
			byte[] keyWithWrongSalt = PBKDF2Util.deriveKey(PASSWORD, otherSalt);
			AESUtil.decryptAES(packed, keyWithWrongSalt);
			System.out.println("错误：丢了原来的盐竟然还能解密");
		} catch (Exception e) {
			System.out.println("盐不对也无法解密（必须保存加密时用的那份盐）: "
					+ e.getClass().getSimpleName());
		}
	}
}

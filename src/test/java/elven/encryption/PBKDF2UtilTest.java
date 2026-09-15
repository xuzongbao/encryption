package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import javax.crypto.AEADBadTagException;

import org.junit.jupiter.api.Test;

/**
 * 同一口令 + 同一盐 → 同一把 AES 密钥；口令错了，AES-GCM 解不开。
 */
class PBKDF2UtilTest {

	private static final String PASSWORD = "correct horse battery staple";

	@Test
	void samePasswordAndSaltDeriveSameKey() throws Exception {
		byte[] salt = PBKDF2Util.generateSalt();
		assertEquals(PBKDF2Util.SALT_LENGTH_BYTES, salt.length);

		byte[] first = PBKDF2Util.deriveKey(PASSWORD, salt);
		byte[] second = PBKDF2Util.deriveKey(PASSWORD, salt);
		assertEquals(PBKDF2Util.DEFAULT_KEY_LENGTH_BITS / 8, first.length);
		assertArrayEquals(first, second);
	}

	@Test
	void differentSaltProducesDifferentKey() throws Exception {
		byte[] key1 = PBKDF2Util.deriveKey(PASSWORD, PBKDF2Util.generateSalt());
		byte[] key2 = PBKDF2Util.deriveKey(PASSWORD, PBKDF2Util.generateSalt());
		assertFalse(Arrays.equals(key1, key2));
	}

	@Test
	void wrongPasswordCannotDecryptAes() throws Exception {
		byte[] salt = PBKDF2Util.generateSalt();
		byte[] key = PBKDF2Util.deriveKey(PASSWORD, salt);
		byte[] packed = AESUtil.encryptAES(TestData.BYTES, key);

		byte[] wrongKey = PBKDF2Util.deriveKey("wrong password", salt);
		assertThrows(AEADBadTagException.class, () -> AESUtil.decryptAES(packed, wrongKey));
	}

	@Test
	void emptyPasswordRejected() {
		byte[] salt = PBKDF2Util.generateSalt();
		assertThrows(IllegalArgumentException.class, () -> PBKDF2Util.deriveKey("", salt));
	}
}

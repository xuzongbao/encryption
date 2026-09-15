package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

/**
 * DES 仅教材对照：56 bit + ECB，不安全。这里只做一次 round-trip。
 */
class DESUtilTest {

	@Test
	void encryptDecryptRoundTrip() throws Exception {
		byte[] key = DESUtil.initKey();
		byte[] cipher = DESUtil.encryptDES(TestData.BYTES, key);
		assertArrayEquals(TestData.BYTES, DESUtil.decryptDES(cipher, key));
	}
}

package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * SHA-256 是确定性哈希：同样输入永远同样输出，但不能还原原文。
 */
class SHA256UtilTest {

	@Test
	void digestIsDeterministicAnd32Bytes() throws Exception {
		byte[] first = SHA256Util.hash(TestData.BYTES);
		byte[] second = SHA256Util.hash(TestData.BYTES);
		assertEquals(SHA256Util.DIGEST_LENGTH_BYTES, first.length);
		assertArrayEquals(first, second);
	}

	@Test
	void hashHexIsLowercaseAndMatchesBytes() throws Exception {
		byte[] digest = SHA256Util.hash(TestData.BYTES);
		String hex = SHA256Util.hashHex(TestData.BYTES);
		assertEquals(64, hex.length());
		assertEquals(hex, hex.toLowerCase());
		assertEquals(BytesToHex.fromBytesToHex(digest), hex);
	}

	@Test
	void knownVectorEmptyString() throws Exception {
		// FIPS 180-2：空输入的 SHA-256
		assertEquals(
				"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
				SHA256Util.hashHex(new byte[0]));
	}

	@Test
	void differentInputDifferentHash() throws Exception {
		assertNotEquals(SHA256Util.hashHex(TestData.BYTES), SHA256Util.hashHex(TestData.flip(TestData.BYTES, 0)));
	}

	@Test
	void nullInputRejected() {
		assertThrows(IllegalArgumentException.class, () -> SHA256Util.hash(null));
	}
}

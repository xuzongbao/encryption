package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Base64 是编码，不是加密：任何人都能解码。这里只核对 round-trip 和非法输入。
 */
class Base64UtilTest {

	@Test
	void roundTripUtf8Text() {
		String encoded = Base64Util.base64Encrypt(TestData.BYTES);
		assertEquals(TestData.TEXT, Base64Util.base64Decrypt(encoded));
	}

	@Test
	void knownVectorHello() {
		// "hello" 的标准 Base64，方便对照教材 / 在线计算器
		assertEquals("aGVsbG8=", Base64Util.base64Encrypt("hello".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		assertEquals("hello", Base64Util.base64Decrypt("aGVsbG8="));
	}

	@Test
	void nullInputRejected() {
		assertThrows(IllegalArgumentException.class, () -> Base64Util.base64Encrypt(null));
		assertThrows(IllegalArgumentException.class, () -> Base64Util.base64Decrypt(null));
	}

	@Test
	void invalidBase64Rejected() {
		assertThrows(IllegalArgumentException.class, () -> Base64Util.base64Decrypt("@@@not-base64@@@"));
	}
}

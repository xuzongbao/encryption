package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * HMAC 能发现篡改，但不加密。verify 用 MessageDigest.isEqual 比较。
 */
class HMACUtilTest {

	@Test
	void verifyAcceptsUntamperedMac() throws Exception {
		byte[] key = HMACUtil.initKey();
		byte[] mac = HMACUtil.hmac(TestData.BYTES, key);
		assertEquals(HMACUtil.MAC_LENGTH_BYTES, mac.length);
		assertTrue(HMACUtil.verify(TestData.BYTES, key, mac));
	}

	@Test
	void verifyRejectsTamperedData() throws Exception {
		byte[] key = HMACUtil.initKey();
		byte[] mac = HMACUtil.hmac(TestData.BYTES, key);
		assertFalse(HMACUtil.verify(TestData.flip(TestData.BYTES, 0), key, mac));
	}

	@Test
	void verifyRejectsTamperedMac() throws Exception {
		byte[] key = HMACUtil.initKey();
		byte[] mac = HMACUtil.hmac(TestData.BYTES, key);
		assertFalse(HMACUtil.verify(TestData.BYTES, key, TestData.flip(mac, 0)));
	}

	@Test
	void wrongKeyLengthRejected() {
		assertThrows(IllegalArgumentException.class, () -> HMACUtil.hmac(TestData.BYTES, new byte[8]));
	}
}

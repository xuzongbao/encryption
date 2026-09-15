package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

/**
 * 3DES 是遗留算法：CBC + 前置 IV。只做一次 round-trip，新系统请看 AES。
 */
class DESedeTest {

	@Test
	void encryptDecryptRoundTrip() throws Exception {
		byte[] key = DESede.initKey();
		byte[] packed = DESede.encrypt3DES(TestData.BYTES, key);
		assertArrayEquals(TestData.BYTES, DESede.decrypt3DES(packed, key));
	}
}

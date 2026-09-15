package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import javax.crypto.AEADBadTagException;

import org.junit.jupiter.api.Test;

/**
 * AES-GCM：能 round-trip；改密文或 tag 会认证失败（AEADBadTagException）。
 */
class AESUtilTest {

	@Test
	void encryptDecryptRoundTrip() throws Exception {
		byte[] key = AESUtil.initKey();
		assertEquals(AESUtil.KEY_SIZE_BITS / 8, key.length);

		byte[] packed = AESUtil.encryptAES(TestData.BYTES, key);
		byte[] plain = AESUtil.decryptAES(packed, key);
		assertArrayEquals(TestData.BYTES, plain);
	}

	@Test
	void packedCiphertextStartsWithRandomIv() throws Exception {
		byte[] key = AESUtil.initKey();
		byte[] first = AESUtil.encryptAES(TestData.BYTES, key);
		byte[] second = AESUtil.encryptAES(TestData.BYTES, key);
		// 每次加密换新 IV，所以整段 packed 密文通常不同；两边都能解开
		assertFalse(Arrays.equals(first, second));
		assertArrayEquals(TestData.BYTES, AESUtil.decryptAES(first, key));
		assertArrayEquals(TestData.BYTES, AESUtil.decryptAES(second, key));
	}

	@Test
	void tamperedCiphertextFailsAuthentication() throws Exception {
		byte[] key = AESUtil.initKey();
		byte[] packed = AESUtil.encryptAES(TestData.BYTES, key);
		byte[] tampered = TestData.flip(packed, packed.length - 1); // 翻 tag 的最后一个字节
		assertThrows(AEADBadTagException.class, () -> AESUtil.decryptAES(tampered, key));
	}

	@Test
	void wrongKeyFailsAuthentication() throws Exception {
		byte[] key = AESUtil.initKey();
		byte[] otherKey = AESUtil.initKey();
		byte[] packed = AESUtil.encryptAES(TestData.BYTES, key);
		assertThrows(AEADBadTagException.class, () -> AESUtil.decryptAES(packed, otherKey));
	}

	@Test
	void shortCiphertextRejected() {
		byte[] key = new byte[AESUtil.KEY_SIZE_BITS / 8];
		assertThrows(IllegalArgumentException.class, () -> AESUtil.decryptAES(new byte[8], key));
	}
}

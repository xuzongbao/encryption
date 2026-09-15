package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * 经典 DH：乙方必须复用甲方 p、g。双方应算出同一把 AES-128 密钥。
 * 第一次生成 2048 bit 参数可能要几秒，这是预期行为。
 */
class DHUtilTest {

	@Test
	void bothSidesDeriveSameSecretAndAesRoundTrip() throws Exception {
		Map<String, Object> alice = DHUtil.initKey();
		byte[] alicePub = DHUtil.getPublicKey(alice);
		byte[] alicePri = DHUtil.getPrivateKey(alice);

		Map<String, Object> bob = DHUtil.initKey(alicePub);
		byte[] bobPub = DHUtil.getPublicKey(bob);
		byte[] bobPri = DHUtil.getPrivateKey(bob);

		byte[] secretAlice = DHUtil.getSecretKey(bobPub, alicePri);
		byte[] secretBob = DHUtil.getSecretKey(alicePub, bobPri);
		assertEquals(DHUtil.AES_KEY_LENGTH_BYTES, secretAlice.length);
		assertArrayEquals(secretAlice, secretBob);

		byte[] packed = AESUtil.encryptAES(TestData.BYTES, secretAlice);
		assertArrayEquals(TestData.BYTES, AESUtil.decryptAES(packed, secretBob));
	}
}

package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * ECDH on secp256r1：双方独立 initKey()，应得到同一把 AES 密钥；ECDSA 可对照验签。
 */
class ECDHUtilTest {

	@Test
	void bothSidesDeriveSameSecretAndAesRoundTrip() throws Exception {
		Map<String, Object> alice = ECDHUtil.initKey();
		byte[] alicePub = ECDHUtil.getPublicKey(alice);
		byte[] alicePri = ECDHUtil.getPrivateKey(alice);

		Map<String, Object> bob = ECDHUtil.initKey();
		byte[] bobPub = ECDHUtil.getPublicKey(bob);
		byte[] bobPri = ECDHUtil.getPrivateKey(bob);

		byte[] secretAlice = ECDHUtil.getSecretKey(bobPub, alicePri);
		byte[] secretBob = ECDHUtil.getSecretKey(alicePub, bobPri);
		assertEquals(ECDHUtil.AES_KEY_LENGTH_BYTES, secretAlice.length);
		assertArrayEquals(secretAlice, secretBob);

		byte[] packed = AESUtil.encryptAES(TestData.BYTES, secretAlice);
		assertArrayEquals(TestData.BYTES, AESUtil.decryptAES(packed, secretBob));
	}

	@Test
	void ecdsaVerifyTrueAndFalse() throws Exception {
		Map<String, Object> keys = ECDHUtil.initKey();
		byte[] publicKey = ECDHUtil.getPublicKey(keys);
		byte[] privateKey = ECDHUtil.getPrivateKey(keys);

		byte[] signature = ECDHUtil.sign(TestData.BYTES, privateKey);
		assertTrue(ECDHUtil.verify(TestData.BYTES, signature, publicKey));
		assertFalse(ECDHUtil.verify(TestData.flip(TestData.BYTES, 0), signature, publicKey));
	}
}

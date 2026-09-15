package elven.encryption;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * RSA-OAEP 加解密 + RSASSA-PSS 签名。生成 2048 bit 密钥较慢，整类共用一对。
 */
class RSAUtilTest {

	private static RSAPublicKey publicKey;
	private static RSAPrivateKey privateKey;

	@BeforeAll
	static void generateKeyPair() throws Exception {
		Map<String, Object> keyMap = RSAUtil.initKey();
		publicKey = RSAUtil.getpublicKey(keyMap);
		privateKey = RSAUtil.getPrivateKey(keyMap);
	}

	@Test
	void encryptDecryptRoundTrip() throws Exception {
		byte[] cipher = RSAUtil.encrypt(TestData.BYTES, publicKey);
		byte[] plain = RSAUtil.decrypt(cipher, privateKey);
		assertArrayEquals(TestData.BYTES, plain);
	}

	@Test
	void tamperedCiphertextFailsDecrypt() throws Exception {
		byte[] cipher = RSAUtil.encrypt(TestData.BYTES, publicKey);
		byte[] tampered = TestData.flip(cipher, 0);
		assertThrows(Exception.class, () -> RSAUtil.decrypt(tampered, privateKey));
	}

	@Test
	void signVerifyAcceptsUntamperedData() throws Exception {
		byte[] signature = RSAUtil.sign(TestData.BYTES, privateKey);
		assertTrue(RSAUtil.verify(TestData.BYTES, signature, publicKey));
	}

	@Test
	void verifyRejectsTamperedData() throws Exception {
		byte[] signature = RSAUtil.sign(TestData.BYTES, privateKey);
		assertFalse(RSAUtil.verify(TestData.flip(TestData.BYTES, 0), signature, publicKey));
	}

	@Test
	void verifyRejectsTamperedSignature() throws Exception {
		byte[] signature = RSAUtil.sign(TestData.BYTES, privateKey);
		assertFalse(RSAUtil.verify(TestData.BYTES, TestData.flip(signature, 0), publicKey));
	}
}

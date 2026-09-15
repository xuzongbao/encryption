package elven.test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import elven.encryption.AESUtil;
import elven.encryption.BytesToHex;
import elven.encryption.ECDHUtil;

/**
 * ECDH 密钥协商演示：双方在 secp256r1 上算出同一把 AES 密钥，再加密示例明文。
 * 错误的对方公钥会得到不同的秘密；用那把错密钥解密 AES-GCM 会失败。
 * 顺带用同一对 EC 密钥做一次很小的 ECDSA 签名对照。
 */
public class testECDH {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) throws Exception {
		System.out.println("曲线: " + ECDHUtil.CURVE_NAME + "（NIST P-256）");
		System.out.println("密钥协商: ECDH；派生: SHA-256 截断 "
				+ ECDHUtil.AES_KEY_LENGTH_BYTES + " 字节 → " + ECDHUtil.SECRET_ALGORITHM + "-128");
		System.out.println("教学 KDF，不是 HKDF。双方都直接 initKey()，不必像经典 DH 那样复用 p、g。");
		System.out.println();

		Map<String, Object> keyMap1 = ECDHUtil.initKey();
		byte[] publicKey1 = ECDHUtil.getPublicKey(keyMap1);
		byte[] privateKey1 = ECDHUtil.getPrivateKey(keyMap1);
		System.out.println("ECDH 甲方公钥(" + publicKey1.length + " 字节): "
				+ BytesToHex.fromBytesToHex(publicKey1));
		System.out.println("ECDH 甲方私钥(" + privateKey1.length + " 字节): "
				+ BytesToHex.fromBytesToHex(privateKey1));

		Map<String, Object> keyMap2 = ECDHUtil.initKey();
		byte[] publicKey2 = ECDHUtil.getPublicKey(keyMap2);
		byte[] privateKey2 = ECDHUtil.getPrivateKey(keyMap2);
		System.out.println("ECDH 乙方公钥(" + publicKey2.length + " 字节): "
				+ BytesToHex.fromBytesToHex(publicKey2));
		System.out.println("ECDH 乙方私钥(" + privateKey2.length + " 字节): "
				+ BytesToHex.fromBytesToHex(privateKey2));

		byte[] secretKey1 = ECDHUtil.getSecretKey(publicKey2, privateKey1);
		System.out.println("ECDH 甲方 本地 AES 密钥: " + BytesToHex.fromBytesToHex(secretKey1));

		byte[] secretKey2 = ECDHUtil.getSecretKey(publicKey1, privateKey2);
		System.out.println("ECDH 乙方 本地 AES 密钥: " + BytesToHex.fromBytesToHex(secretKey2));

		System.out.println("双方派生密钥是否相同: " + Arrays.equals(secretKey1, secretKey2));

		byte[] encrypted = AESUtil.encryptAES(DATA.getBytes(StandardCharsets.UTF_8), secretKey1);
		System.out.println(DATA + " 用甲方密钥 AES-GCM 加密: " + BytesToHex.fromBytesToHex(encrypted));
		byte[] plain = AESUtil.decryptAES(encrypted, secretKey2);
		System.out.println(DATA + " 用乙方密钥 AES-GCM 解密: "
				+ new String(plain, StandardCharsets.UTF_8));
		System.out.println();

		Map<String, Object> keyMap3 = ECDHUtil.initKey();
		byte[] publicKey3 = ECDHUtil.getPublicKey(keyMap3);
		byte[] mismatched = ECDHUtil.getSecretKey(publicKey3, privateKey1);
		System.out.println("甲方误用第三方公钥时，派生密钥是否仍与乙方相同: "
				+ Arrays.equals(secretKey1, mismatched));
		System.out.println("错误对方公钥得到的 AES 密钥: " + BytesToHex.fromBytesToHex(mismatched));

		try {
			AESUtil.decryptAES(encrypted, mismatched);
			System.out.println("错误：用错误共享密钥竟然解密成功了");
		} catch (Exception e) {
			System.out.println("用错误共享密钥无法解密（AES-GCM 认证失败）: "
					+ e.getClass().getSimpleName());
		}
		System.out.println();

		byte[] data = DATA.getBytes(StandardCharsets.UTF_8);
		byte[] signature = ECDHUtil.sign(data, privateKey1);
		System.out.println("签名算法: " + ECDHUtil.SIGNATURE_ALGORITHM + "（教学附加，生产请与 ECDH 分钥）");
		System.out.println(DATA + " ECDSA 签名: " + BytesToHex.fromBytesToHex(signature));
		System.out.println("乙方用甲方公钥验签(原文未改): "
				+ ECDHUtil.verify(data, signature, publicKey1));

		byte[] tampered = data.clone();
		tampered[0] = (byte) (tampered[0] ^ 0x01);
		System.out.println("乙方用甲方公钥验签(翻转第 1 字节): "
				+ ECDHUtil.verify(tampered, signature, publicKey1));
	}
}

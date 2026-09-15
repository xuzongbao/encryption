package elven.test;

import java.nio.charset.StandardCharsets;

import elven.encryption.BytesToHex;
import elven.encryption.HMACUtil;

/**
 * HMAC-SHA-256 演示：正确密钥能通过校验，改一个字节就会失败。
 */
public class testHMAC {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) throws Exception {
		System.out.println("算法: " + HMACUtil.ALGORITHM + "（带密钥的认证码，不加密）");

		byte[] key = HMACUtil.initKey();
		System.out.println("HMAC 密钥 : " + BytesToHex.fromBytesToHex(key));

		byte[] data = DATA.getBytes(StandardCharsets.UTF_8);
		byte[] mac = HMACUtil.hmac(data, key);
		System.out.println(DATA + " HMAC-SHA-256 : " + BytesToHex.fromBytesToHex(mac));
		System.out.println("原文未改时校验: " + HMACUtil.verify(data, key, mac));

		byte[] tampered = data.clone();
		tampered[0] = (byte) (tampered[0] ^ 0x01);
		System.out.println("把原文第 1 个字节翻转后校验: " + HMACUtil.verify(tampered, key, mac));
		System.out.println("HMAC 能发现篡改，但原文本身仍是明文；要同时保密请看 AES-GCM。");
	}
}

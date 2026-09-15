package elven.test;

import java.nio.charset.StandardCharsets;

import elven.encryption.BytesToHex;
import elven.encryption.DESede;

/**
 * 3DES-CBC 演示。3DES 已过时，仅作教材对照。密文线格式：8 字节 IV || 密文。
 */
public class testDESede {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) throws Exception {
		System.out.println("警告: 3DES 是遗留算法，新系统请用 AES。变换: " + DESede.TRANSFORMATION);

		byte[] key = DESede.initKey();
		System.out.println("3DES 密钥 : " + BytesToHex.fromBytesToHex(key));
		byte[] encrypt = DESede.encrypt3DES(DATA.getBytes(StandardCharsets.UTF_8), key);
		System.out.println(DATA + " 3DES 加密 : " + BytesToHex.fromBytesToHex(encrypt));
		System.out.println("线格式: 前 " + DESede.IV_LENGTH_BYTES + " 字节是 IV");

		byte[] plain = DESede.decrypt3DES(encrypt, key);
		System.out.println(DATA + " 3DES 解密: " + new String(plain, StandardCharsets.UTF_8));
	}
}

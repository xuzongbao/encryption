package elven.test;

import java.nio.charset.StandardCharsets;

import elven.encryption.BytesToHex;
import elven.encryption.DESUtil;

/**
 * DES-ECB 演示。DES 已不安全，仅作教材对照。
 */
public class testDES {

	public static final String DATA = "hi, welcome to my git area!";

	public static void main(String[] args) throws Exception {
		System.out.println("警告: DES 是遗留算法，不要用于真实数据。变换: " + DESUtil.TRANSFORMATION);

		byte[] desKey = DESUtil.initKey();
		System.out.println("DES Key : " + BytesToHex.fromBytesToHex(desKey));
		byte[] desReult = DESUtil.encryptDES(DATA.getBytes(StandardCharsets.UTF_8), desKey);
		System.out.println(DATA + " DES 加密 =====>>>>>>> " + BytesToHex.fromBytesToHex(desReult));
		byte[] plain = DESUtil.decryptDES(desReult, desKey);
		System.out.println(DATA + " DES 解密 =====>>>>>>> " + new String(plain, StandardCharsets.UTF_8));
	}
}

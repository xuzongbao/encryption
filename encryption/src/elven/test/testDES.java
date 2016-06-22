package elven.test;

import elven.encryption.BytesToHex;
import elven.encryption.DESUtil;

public class testDES {

	//待加密原文
	public static final String DATA = "hi, welcome to my git area!";
	
	public static void main(String[] args) throws Exception {
		byte[] desKey = DESUtil.initKey();
		System.out.println("DES Key : " + BytesToHex.fromBytesToHex(desKey));
		byte[] desReult = DESUtil.encryptDES(DATA.getBytes(), desKey);
		System.out.println(DATA + "DES 加密 =====>>>>>>> " + BytesToHex.fromBytesToHex(desReult));
		byte[] plain = DESUtil.decryptDES(desReult, desKey);
		System.out.println(DATA + "DES 解密 =====>>>>>>> " + new String(plain));
	}
}

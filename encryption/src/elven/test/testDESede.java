package elven.test;

import elven.encryption.BytesToHex;
import elven.encryption.DESede;

public class testDESede {

	//待加密原文
	public static final String DATA = "hi, welcome to my git area!";
	
	public static void main(String[] args) throws Exception {
		//获取密钥
		byte[] key = DESede.initKey();
		System.out.println("3DES 密钥 : " + BytesToHex.fromBytesToHex(key));
		//加密
		byte[] encrypt = DESede.encrypt3DES(DATA.getBytes(), key);
		System.out.println(DATA + " 3DES 加密 : " + BytesToHex.fromBytesToHex(encrypt));
		byte[] plain = DESede.decrypt3DES(encrypt, key);
		System.out.println(DATA + " 3DES 解密: " + new String(plain));
	}
}

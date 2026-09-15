package elven.encryption;

/**
 * 把字节数组转成小写十六进制，方便在控制台查看密钥和密文。
 * 这不是加密，只是打印辅助。
 */
public class BytesToHex {

	public static String fromBytesToHex(byte[] resultBytes) {
		if (resultBytes == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder(resultBytes.length * 2);
		for (int i = 0; i < resultBytes.length; i++) {
			String hex = Integer.toHexString(0xFF & resultBytes[i]);
			if (hex.length() == 1) {
				builder.append('0');
			}
			builder.append(hex);
		}
		return builder.toString();
	}
}

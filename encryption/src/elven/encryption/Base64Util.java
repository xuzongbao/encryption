package elven.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 编解码演示。
 * <p>
 * Base64 <b>不是加密</b>，只是把二进制变成可打印字符串。任何人都能解码。
 * Java 8 起请使用 {@link java.util.Base64}，不要再用已删除的 {@code sun.misc.BASE64Encoder}
 * 或本仓库曾经捆绑的 {@code Decoder.BASE64Encoder} JAR。
 * <p>
 * 使用标准 Base64（RFC 4648），不含 URL 变体，也不按 76 字符折行
 * （旧 {@code sun.misc} 编码器会折行，短字符串结果可能因此不同）。
 */
public class Base64Util {

	/**
	 * 把字节编码为 Base64 字符串。
	 *
	 * @param data 原始字节，不能为 null
	 * @return 标准 Base64 文本
	 */
	public static String base64Encrypt(byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		return Base64.getEncoder().encodeToString(data);
	}

	/**
	 * 把 Base64 字符串解码为 UTF-8 文本。
	 * <p>
	 * 演示用：解码后按 UTF-8 解释成字符串。若原文不是 UTF-8 文本，请自行对
	 * {@link Base64#getDecoder()} 的字节结果处理。
	 *
	 * @param data Base64 文本
	 * @return UTF-8 字符串
	 */
	public static String base64Decrypt(String data) {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		byte[] resultBytes = Base64.getDecoder().decode(data);
		return new String(resultBytes, StandardCharsets.UTF_8);
	}
}

package elven.encryption;

import java.nio.charset.StandardCharsets;

/**
 * JUnit 测试共用的示例明文。和 {@code elven.test} 演示里的句子保持一致，方便对照。
 */
final class TestData {

	static final String TEXT = "hi, welcome to my git area!";

	static final byte[] BYTES = TEXT.getBytes(StandardCharsets.UTF_8);

	/** 翻转指定下标的一个比特，用来构造「被篡改」的副本。 */
	static byte[] flip(byte[] src, int index) {
		byte[] copy = src.clone();
		copy[index] ^= 0x01;
		return copy;
	}

	private TestData() {
	}
}

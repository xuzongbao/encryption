package elven.encryption;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

public class DHUtil {

	public static final String PUBLIC_KEY = "DHPublicKey";
	public static final String PRIVATE_KEY = "DHPrivateKey";
	
	/**
	 * 甲方初始化并返回密钥对
	 */
	public static Map<String, Object> initKey() throws Exception{
		//实例化密钥对生成器
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
		//初始化密钥对生成器  默认是1024  512-1024 & 64的倍数
		keyPairGenerator.initialize(1024);
		//生成密钥对
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		//得到甲方公钥
		DHPublicKey publicKey = (DHPublicKey) keyPair.getPublic();
		//得到甲方私钥
		DHPrivateKey peivateKey = (DHPrivateKey) keyPair.getPrivate();
		//将公钥和私钥封装到Map中，方便之后使用
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, peivateKey);
		return keyMap;
	}
	
	/**
	 * 乙方根据甲方公钥初始化并返回密钥对
	 */
	public static Map<String, Object> initKey(byte[] key) throws Exception{
		//将甲方公钥从字节数组转换为publicKey
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
		//实例化密钥工厂
		KeyFactory keyFactory = KeyFactory.getInstance("DH");
		//产生甲方公钥pubKey
		DHPublicKey dhPublicKey = (DHPublicKey) keyFactory.generatePublic(keySpec);
		//剖析甲方公钥，得到其参数
		DHParameterSpec dhParameterSpec = dhPublicKey.getParams();
		//实例化密钥对生成器
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
		//用甲方公钥初始化密钥对生成器
		keyPairGenerator.initialize(dhParameterSpec);
		//产生密钥对
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		//得到乙方公钥
		DHPublicKey publicKey = (DHPublicKey) keyPair.getPublic();
		//得到乙方私钥
		DHPrivateKey privateKey = (DHPrivateKey) keyPair.getPrivate();
		//将公钥和私钥封装到Map中，方便以后使用
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		
		return keyMap;
	}
	
	/**
	 * 根据对方的公钥和自己的私钥生成本地密钥
	 */
	public static byte[] getSecretKey(byte[] publicKey, byte[] privateKey) throws Exception{
		//实例化密钥工厂
		KeyFactory keyFactory = KeyFactory.getInstance("DH");
		//将公钥从字节数组转换为publicKey
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
		PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
		//将私钥从字节数组转换为privateKey
		PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(privateKey);
		PrivateKey priKey = keyFactory.generatePrivate(priKeySpec);
		//准备根据以上公钥和私钥生成本地密钥SecretKey
		//先实例化KeyAgreement
		KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
		//用自己的私钥初始化keyAgreement
		keyAgreement.init(priKey);
		//结合对方的公钥进行运算
		keyAgreement.doPhase(pubKey, true);
		//开始生成本地密钥secretKey   密钥算法为对称密码算法
		SecretKey secretKey = keyAgreement.generateSecret("DES");  //DES、3DES、AES
		return secretKey.getEncoded();
	}
	
	/**
	 * 从Map中取得公钥
	 */
	public static byte[] getPublicKey(Map<String, Object> keyMap){
		DHPublicKey key = (DHPublicKey) keyMap.get(PUBLIC_KEY);
		return key.getEncoded();
	}
	
	/**
	 * 从Map中取得私钥
	 */
	public static byte[] getPrivateKey(Map<String, Object> keyMap){
		DHPrivateKey key = (DHPrivateKey) keyMap.get(PRIVATE_KEY);
		return key.getEncoded();
	}
	
}

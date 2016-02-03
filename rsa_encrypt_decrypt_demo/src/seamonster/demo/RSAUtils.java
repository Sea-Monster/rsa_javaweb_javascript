/**
 * 
 */
package seamonster.demo;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JCERSAPrivateKey;
import org.bouncycastle.jce.provider.JCERSAPublicKey;


/**
 * @author Dovahkiin
 *
 */
public class RSAUtils {
	public static KeyPair KEY_PAIR = null;
	private static BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();

	/**
	 * 生成公钥私钥
	 */
	public static KeyPair generateKeyPair() throws Exception {
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", bouncyCastleProvider);
			final int KEY_SIZE = 1024;// 没什么好说的了，这个值关系到块加密的大小，可以更改，但是不要太大，否则效率会低
			// final int KEY_SIZE = 4096;
			keyPairGen.initialize(KEY_SIZE, new SecureRandom());
			KeyPair keyPair = keyPairGen.generateKeyPair();

			System.out.println(keyPair.getPrivate());
			System.out.println(keyPair.getPublic());
			KEY_PAIR = keyPair;
			return keyPair;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * 用私钥加密
	 */
	public static String encryptPrivate(String data) throws Exception {
		return encrypt(KEY_PAIR.getPrivate(), data);
	}

	/**
	 * 用私钥解密
	 */
	public static String decryptPrivate(String data) throws Exception {
		return decrypt(KEY_PAIR.getPrivate(), data);
	}

	private static String encrypt(Key pk, String data) throws Exception {

		System.out.println("需要加密的：" + data);
		String encodeStr = URLEncoder.encode(data, "UTF-8");
		System.out.println("Encode后的：" + encodeStr);
		String encodeStr2 = encodeStr.replace("+", "%20");
		System.out.println("+替换为%20：" + encodeStr2);
		byte[] b1 = encodeStr2.getBytes();
		byte[] b2 = encrypt(pk, b1);
		String encryptedString = byteToString(b2);
		System.out.println("加密后的：" + encryptedString);
		return encryptedString;
	}

	private static String decrypt(Key pk, String data) throws Exception {
		System.out.println("需要解密的：" + data);
		byte[] b1 = hexStringToBytes(data);
		byte[] b2 = decrypt(pk, b1);
		String decryptedString = new String(b2);
		System.out.println("解密后的：" + decryptedString);
		String reverseDecryptedString = new StringBuffer(decryptedString).reverse().toString();
		System.out.println("解密后再倒序：" + reverseDecryptedString);
		String resultString = URLDecoder.decode(reverseDecryptedString, "UTF-8");
		System.out.println("最后URLDecoder一下：" + resultString);
		return resultString;
	}

	/**
	 * 返回公钥指数e
	 */
	public static String getPublicExponent() {
		BigInteger b = ((JCERSAPublicKey) KEY_PAIR.getPublic()).getPublicExponent();
		return b.toString(16);
	}

	/**
	 * 返回模值n
	 */
	public static String getModulus() {
		BigInteger b = ((JCERSAPublicKey) KEY_PAIR.getPublic()).getModulus();
		return b.toString(16);
	}

	/**
	 * 返回私钥指数d
	 * 
	 * @return
	 */
	public static String getPrivateExponent() {
		BigInteger b = ((JCERSAPrivateKey) KEY_PAIR.getPrivate()).getPrivateExponent();
		return b.toString(16);
	}

	/**
	 * 加密
	 * 
	 * @param key
	 *            加密的密钥 (也可以是公钥)
	 * @param data
	 *            待加密的明文数据
	 * @return 加密后的数据
	 */
	private static byte[] encrypt(Key pk, byte[] data) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance("RSA", bouncyCastleProvider);
			cipher.init(Cipher.ENCRYPT_MODE, pk);
			int blockSize = cipher.getBlockSize();// 获得加密块大小，如：加密前数据为128个byte，而key_size=1024
			// 加密块大小为127
			// byte,加密后为128个byte;因此共有2个加密块，第一个127
			// byte第二个为1个byte
			int outputSize = cipher.getOutputSize(data.length);// 获得加密块加密后块大小
			int leavedSize = data.length % blockSize;
			int blocksSize = leavedSize != 0 ? data.length / blockSize + 1 : data.length / blockSize;
			byte[] raw = new byte[outputSize * blocksSize];
			int i = 0;
			while (data.length - i * blockSize > 0) {
				if (data.length - i * blockSize > blockSize)
					cipher.doFinal(data, i * blockSize, blockSize, raw, i * outputSize);
				else
					cipher.doFinal(data, i * blockSize, data.length - i * blockSize, raw, i * outputSize);
				// 这里面doUpdate方法不可用，查看源代码后发现每次doUpdate后并没有什么实际动作除了把byte[]放到
				// ByteArrayOutputStream中，而最后doFinal的时候才将所有的byte[]进行加密，可是到了此时加密块大小很可能已经超出了
				// OutputSize所以只好用dofinal方法。
				i++;
			}
			return raw;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * 解密
	 * 
	 * @param key
	 *            解密的密钥 (也可以是公钥)
	 * @param raw
	 *            已经加密的数据
	 * @return 解密后的明文
	 */
	private static byte[] decrypt(Key pk, byte[] raw) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance("RSA", bouncyCastleProvider);
			cipher.init(Cipher.DECRYPT_MODE, pk);
			int blockSize = cipher.getBlockSize();
			ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
			int j = 0;
			while (raw.length - j * blockSize > 0) {
				bout.write(cipher.doFinal(raw, j * blockSize, blockSize));
				j++;
			}
			return bout.toByteArray();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	private static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/** * Convert char to byte * @param c char * @return byte */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * 把byte数组变换为16進数的字符串。
	 *
	 * @param bytes
	 *            byte数组
	 * @return 16進数的字符串
	 */
	private static String byteToString(byte[] bytes) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			int d = bytes[i];
			if (d < 0) {
				d += 256;
			}
			if (d < 16) {
				buf.append("0");
			}
			buf.append(Integer.toString(d, 16));
		}
		return buf.toString();
	}

	/**
	 * 用公钥加密
	 */
	public static String encryptPublic(String data) throws Exception {
		return encrypt(KEY_PAIR.getPublic(), data);
	}

	/**
	 * 用公钥解密
	 */
	public static String decryptPublic(String data) throws Exception {
		return decrypt(KEY_PAIR.getPublic(), data);
	}

	
}

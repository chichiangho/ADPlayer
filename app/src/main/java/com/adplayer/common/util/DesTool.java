package com.adplayer.common.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Des加解密工具类
 * 
 * @author xieke
 *
 */
public class DesTool {

	/**
	 * 3des加密
	 * 
	 * @param key
	 *            24个字节
	 * @param src
	 * @return
	 */
	public static byte[] encode(byte[] key, byte[] src) {
		if (key == null || key.length != 24) {
			throw new RuntimeException("key不合法!");
		}
		try {
			// 生成密钥
			SecretKey deskey = new SecretKeySpec(key, "DESede");
			// 加密
			Cipher c1 = Cipher.getInstance("DESede");
			c1.init(Cipher.ENCRYPT_MODE, deskey);

			return c1.doFinal(src);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 3des解密
	 * 
	 * @param key
	 *            24个字节
	 * @param src
	 * @return
	 */
	public static byte[] decode(byte[] key, byte[] src) {
		if (key == null || key.length != 24) {
			throw new RuntimeException("key不合法!");
		}
		try {
			// 生成密钥
			SecretKey deskey = new SecretKeySpec(key, "DESede");
			// 解密
			Cipher c1 = Cipher.getInstance("DESede");
			c1.init(Cipher.DECRYPT_MODE, deskey);
			return c1.doFinal(src);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

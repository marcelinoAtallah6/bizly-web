package com.um.common;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Base64;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

	@Value("${keyStore.path}")
	private String keyStorePath;

	@Value("${keyStore.password}")
	private String keyStorePassword;

	@Value("${keyStore.alias}")
	private String keyStoreAlias;

	public String decryptPassword(String encryptedPassword) throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		InputStream is = getClass().getClassLoader().getResourceAsStream(keyStorePath);
		keyStore.load(is, keyStorePassword.toCharArray());
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyStoreAlias, keyStorePassword.toCharArray());

		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
		return new String(decryptedBytes);
	}
}
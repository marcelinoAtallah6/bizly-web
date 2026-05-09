package com.auth.common;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

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

		String normalized = encryptedPassword.trim().replace(' ', '+');
		byte[] encryptedBytes = Base64.getDecoder().decode(normalized);

		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
				PSource.PSpecified.DEFAULT);
		cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);

		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
		return new String(decryptedBytes, StandardCharsets.UTF_8);
	}

}
package com.auth.config.security;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtConfig {

	@Value("${keyStore.path}")
	private String keyStorePath;

	@Value("${keyStore.password}")
	private String keyStorePassword;

	@Value("${keyStore.alias}")
	private String keyStoreAlias;

	@Bean
	public JwtEncoder jwtEncoder() throws Exception {

		KeyStore keyStore = KeyStore.getInstance("JKS");

		InputStream is = new ClassPathResource(keyStorePath).getInputStream();
		keyStore.load(is, keyStorePassword.toCharArray());

		RSAPublicKey publicKey = (RSAPublicKey) keyStore.getCertificate(keyStoreAlias).getPublicKey();

		RSAPrivateKey privateKey = (RSAPrivateKey) keyStore.getKey(keyStoreAlias, keyStorePassword.toCharArray());

		RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyStoreAlias).build();

		JWKSource<SecurityContext> jwkSource = (selector, context) -> selector.select(new JWKSet(rsaKey));

		return new NimbusJwtEncoder(jwkSource);
	}
}
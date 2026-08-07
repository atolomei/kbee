package com.novamens.signature;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Map;

import com.novamens.dom.Domain;
import com.novamens.security.User;
import com.novamens.service.SystemService;

public interface SystemSignatureService extends SystemService {
	public KeyPair createKeys() throws SignatureException;
	public Certificate createCertificate(User user, KeyPair keys, Map<String, String> dn) throws SignatureException;
	public Certificate createCertificate(Domain domain, KeyPair keys) throws SignatureException;
	public Certificate getRootCACertificate()throws SignatureException;
	public String sign(String data, PrivateKey key) throws SignatureException;
	public boolean verify(String message, String sign, Certificate certificate) throws SignatureException;
}

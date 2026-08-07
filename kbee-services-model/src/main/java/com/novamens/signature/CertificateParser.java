package com.novamens.signature;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public abstract class CertificateParser {
	
	public abstract String write(Certificate certificate) throws IOException;
	public abstract Certificate read(String stream) throws IOException, CertificateException;
	
	public abstract String writePrivateKey(PrivateKey key) throws IOException;
	public abstract String writePlainKey(PrivateKey key) throws IOException;
	public abstract PrivateKey readPrivateKey(String stream) throws IOException, CertificateException;
	public abstract PrivateKey readPlainKey(String stream) throws IOException, CertificateException;

	
	public abstract byte[] writePfx(Certificate cACertificate, Certificate certificate, PrivateKey key, String password) throws IOException;

	public static CertificateParser Get() {
		return (CertificateParser)ServiceLocator.getService(BeansService.class).getBean("CertificateParser");
	}
}
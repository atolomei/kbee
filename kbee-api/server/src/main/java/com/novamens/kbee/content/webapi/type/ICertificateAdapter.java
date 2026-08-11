package com.novamens.kbee.content.webapi.type;

import java.io.IOException;
import java.security.cert.Certificate;

import com.novamens.signature.CertificateParser;

import kbee.api.model.ICertificate;

public class ICertificateAdapter implements Adapter<Certificate, ICertificate> {
	
	
	public ICertificateAdapter() {
	}
	
	public ICertificate adapt(Certificate certificate) {
		try {
			ICertificate icertificate = new ICertificate();
			
			String pem = CertificateParser.Get().write(certificate);
			icertificate.setData(pem);
			
			return icertificate;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

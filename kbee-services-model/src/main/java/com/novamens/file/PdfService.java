package com.novamens.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.novamens.service.SystemService;
import com.novamens.signature.SignatureException;

public interface PdfService extends SystemService {
	public File convertHtml(InputStream input, PdfInfo info) throws IOException;
	public void getStream(InputStream input, OutputStream output) throws IOException;
	public void sign(File input, Certificate caCertificate, Certificate certificate, PrivateKey key, OutputStream output, String signaturestream) throws SignatureException;
	public void getSigned(File input, OutputStream output, String signaturestream) throws IOException;
	public void sign(InputStream input, Certificate caCertificate, Certificate certificate, PrivateKey key, OutputStream output) throws SignatureException;
	public File getHtml(String id, InputStream input) throws IOException;
}

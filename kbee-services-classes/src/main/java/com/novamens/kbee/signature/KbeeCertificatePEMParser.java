package com.novamens.kbee.signature;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCS12PfxPduBuilder;
import org.bouncycastle.pkcs.PKCS12SafeBag;
import org.bouncycastle.pkcs.PKCS12SafeBagBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12PBEOutputEncryptorBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEOutputEncryptorBuilder;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.encryption.EncryptionSystemService;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.CertificateParser;


public class KbeeCertificatePEMParser extends CertificateParser {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeCertificatePEMParser.class.getName());

	
	public String write(Certificate certificate) throws IOException {
		StringWriter sWrt = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(sWrt);
		pemWriter.writeObject(certificate);
		pemWriter.close();
		String string = sWrt.toString();
		return string;
	}
	
	public byte[] writePfx(Certificate caCertificate, Certificate certificate, PrivateKey key, String password) throws IOException {
		try {
			X509Certificate x509 = (X509Certificate)certificate;

			
		      JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
		      
		      Certificate root = getKbeeDomain().getCertificate();
		      PKCS12SafeBagBuilder rootCertBagBuilder = new JcaPKCS12SafeBagBuilder((X509Certificate)root);
		      rootCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("CA Certificate"));

		      PKCS12SafeBagBuilder taCertBagBuilder = new JcaPKCS12SafeBagBuilder((X509Certificate)caCertificate);
		      taCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("SubCA Certificate"));

		      PKCS12SafeBagBuilder eeCertBagBuilder = new JcaPKCS12SafeBagBuilder(x509);
		      eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("USER Certificate"));
		      eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(x509.getPublicKey()));

		      PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(key, new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, new CBCBlockCipher(new DESedeEngine())).build(password.toCharArray()));

		      keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("Kbee Bag"));
		      keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(x509.getPublicKey()));

		      //
		      // construct the actual key store
		      //
		      PKCS12PfxPduBuilder pfxPduBuilder = new PKCS12PfxPduBuilder();

		      PKCS12SafeBag[] certs = new PKCS12SafeBag[3];

		      certs[0] = eeCertBagBuilder.build();
		      //certs[1] = caCertBagBuilder.build();
		      certs[1] = taCertBagBuilder.build();
		      certs[2] = rootCertBagBuilder.build();

//		      pfxPduBuilder.addEncryptedData(new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, new CBCBlockCipher(new RC2Engine())).build(password.toCharArray()), certs);

				OutputEncryptor dataEncryptor = new JcePKCSPBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC).build(password.toCharArray());
				pfxPduBuilder.addEncryptedData(dataEncryptor, certs);
		      pfxPduBuilder.addData(keyBagBuilder.build());

		      byte pfx[]= pfxPduBuilder.build(new BcPKCS12MacCalculatorBuilder(), password.toCharArray()).getEncoded(ASN1Encoding.DER);
			
			
			
			
//			File outputFile = new File ("c:\\temp\\cert.pfx");
//			try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
//			    outputStream.write(pfx);
//			    outputStream.close();
//			}
			return pfx;
			
		
		}
		catch (Exception e) {
			logger.error(e);
			throw new IOException(e);
		}
	}

	
	public Certificate read(String pem) throws IOException, CertificateException {
		PEMParser parser = new PEMParser(new StringReader(pem));
		X509CertificateHolder certHolder = (X509CertificateHolder)parser.readObject();
		if (certHolder==null) throw new CertificateException("invalid certificate");
		Certificate certificate = new JcaX509CertificateConverter().getCertificate(certHolder);
		return certificate;
	}
	
	public String writePrivateKey(PrivateKey key) throws IOException {
		
		StringWriter sWrt = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(sWrt);
		pemWriter.writeObject(key);
		pemWriter.close();
		
		String string = sWrt.toString();
		
		string = encrypt(string);
		
		return string;
	}
	
	public String writePlainKey(PrivateKey key) throws IOException {
		
		StringWriter sWrt = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(sWrt);
		pemWriter.writeObject(key);
		pemWriter.close();
		
		String string = sWrt.toString();
		
		return string;
	}
	
	public PrivateKey readPrivateKey(String string) throws IOException, CertificateException {
		
		//String pem = string;
		String pem = decrypt(string);
		
		PEMParser parser = new PEMParser(new StringReader(pem));
		//PrivateKeyInfo info = (PrivateKeyInfo)parser.readObject();
		//PrivateKey key = new JcaPEMKeyConverter().getPrivateKey(info);
		PEMKeyPair pemKeyPair = (PEMKeyPair)parser.readObject();
		PrivateKey key = new JcaPEMKeyConverter().getPrivateKey(pemKeyPair.getPrivateKeyInfo());
		
		return key;
	}
	
	public PrivateKey readPlainKey(String pem) throws IOException, CertificateException {
		PEMParser parser = new PEMParser(new StringReader(pem));
		Object keyobject = parser.readObject();
		if (keyobject==null) throw new CertificateException("invalid key");
		PrivateKeyInfo info = keyobject instanceof PrivateKeyInfo ?
			(PrivateKeyInfo)keyobject :
			((PEMKeyPair)keyobject).getPrivateKeyInfo();		
		//PEMKeyPair pemKeyPair = (PEMKeyPair)parser.readObject();
		PrivateKey key = new JcaPEMKeyConverter().getPrivateKey(info);
		return key;
	}
	
	private String encrypt(String input) throws IOException {
		String encrypted = ServiceLocator.getService(EncryptionSystemService.class).encrypt(input);
		return encrypted;
	}
	
	private String decrypt(String input) throws IOException {
		try {
			String decrypted = ServiceLocator.getService(EncryptionSystemService.class).decrypt(input);
			return decrypted;
		}
		catch (Exception e) {
			logger.error(e);
			throw new IOException(e);
		}
	}
	
	private Domain getKbeeDomain() {
		return getContentDao().findDomainByName("kbee");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
package com.novamens.kbee.signature;


import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.DomainService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.SignatureException;
import com.novamens.signature.SystemSignatureService;

public class BouncyCastleSignatureService implements SystemSignatureService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BouncyCastleSignatureService.class.getName());

	
	public BouncyCastleSignatureService() {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public KeyPair createKeys() throws SignatureException {
		try {
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
	        //KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
	        keyGen.initialize(512);
	        //keyGen.initialize(2048);
	        KeyPair pair = keyGen.genKeyPair();
		        
	        return pair;
		}
		catch (NoSuchAlgorithmException e) {
			throw new SignatureException(e);
		}
	}
	
	@Override
	public Certificate getRootCACertificate() throws SignatureException  {
		return getKbeeDomain().getCertificate();
	}
	
	public PrivateKey getRootCAKey() throws IOException  {
		return getKbeeDomain().getPrivateKey();
	}
	
	public String sign(String data, PrivateKey key) throws SignatureException {
		try {
			Security.addProvider(new BouncyCastleProvider());
			//Signature signature = Signature.getInstance("SHA256withDSA");
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(key);
			signature.update(data.getBytes(StandardCharsets.UTF_8));
			byte[] signedbytes = signature.sign();
					
			String signed = Base64.getEncoder().encodeToString(signedbytes);
			 
			return signed;
		}
		catch (java.security.SignatureException | NoSuchAlgorithmException |  InvalidKeyException e) {
			throw new SignatureException(e);
		}
	}
	
	public boolean verify(String message, String signed, Certificate certificate)  throws SignatureException   {
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify( certificate.getPublicKey() );
			sig.update( message.getBytes( (StandardCharsets.UTF_8))) ;
			final byte[] bytes = Base64.getDecoder().decode( signed);
			return sig.verify( bytes );
		}
		catch (Exception e) {
			throw new SignatureException(e);
		}
	}
	
	public Certificate createCertificate(User user, KeyPair keys, Map<String, String> dn) throws SignatureException {
		try {
			//Security.addProvider(new BouncyCastleProvider());
			
	        X500NameBuilder issuerbuilder = new X500NameBuilder();
	        issuerbuilder.addRDN(BCStyle.CN, ((KbeeUser)user).getDomain().getOrganization());
			X500Name issuer = issuerbuilder.build();
		    
	        X500NameBuilder subjectbuilder = new X500NameBuilder();
	        subjectbuilder.addRDN(BCStyle.CN, user.getFirstLastName());
	        // Organization
	        if (dn.get("O")!=null) subjectbuilder.addRDN(BCStyle.O, dn.get("O"));
	        // Title
	        if (dn.get("T")!=null) subjectbuilder.addRDN(BCStyle.T, dn.get("T"));
	        X500Name subjectDN = subjectbuilder.build();

			SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keys.getPublic().getEncoded());
			Date start = new Date();
			Date until = Date.from(LocalDate.now().plus(365, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.UTC));
			   
			X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer,
				new BigInteger(10, new SecureRandom()), //Choose something better for real use???
				start,
				until,
				subjectDN,
				subPubKeyInfo
			);
			
			Certificate caCertificate = ((KbeeUser)user).getDomain().getService(DomainService.class).getCertificate();

		    SubjectPublicKeyInfo caPublicKeyInfo = SubjectPublicKeyInfo.getInstance(caCertificate.getPublicKey().getEncoded());
		    DigestCalculator digCalc = new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));
		    AuthorityKeyIdentifier aki =  new X509ExtensionUtils(digCalc).createAuthorityKeyIdentifier(caPublicKeyInfo);
		    builder.addExtension(Extension.authorityKeyIdentifier, false, aki);
			
			ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").setProvider(new BouncyCastleProvider()).build(keys.getPrivate());
			//ContentSigner signer = new JcaContentSignerBuilder("SHA256WithDSA").setProvider(new BouncyCastleProvider()).build(keys.getPrivate());
			final X509CertificateHolder holder = builder.build(signer);
			Certificate certificate = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(holder);
			return certificate;
		}
		catch (OperatorCreationException | CertificateException  | CertIOException e) {
			logger.error(e);
			throw new SignatureException(e);
		}
	}
	
	public Certificate createCertificate(Domain domain, KeyPair keys) throws SignatureException {
		try {
			Security.addProvider(new BouncyCastleProvider());
			
			Certificate caCertificate =  getRootCACertificate();
			
	        //X500NameBuilder issuerbuilder = new X500NameBuilder();
	        //issuerbuilder.addRDN(BCStyle.CN, "KBEE");
	    	//X500Name issuer = issuerbuilder.build();
	        X500Name issuer = new JcaX509CertificateHolder((X509Certificate)caCertificate).getSubject();
		    
	        X500NameBuilder subjectbuilder = new X500NameBuilder();
	        subjectbuilder.addRDN(BCStyle.CN, domain.getDisplayName());
	        subjectbuilder.addRDN(BCStyle.T, domain.getDisplayName());
	        subjectbuilder.addRDN(BCStyle.O, domain.getOrganization());
	        X500Name subjectDN = subjectbuilder.build();
	       
			SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keys.getPublic().getEncoded());
			Date start = new Date();
			Date until = Date.from(LocalDate.now().plus(365, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.UTC));
			   
			X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer,
				new BigInteger(10, new SecureRandom()), //Choose something better for real use
				start,
				until,
				subjectDN,
				subPubKeyInfo
			);
			
		    builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
		    KeyUsage ku = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign);
		    builder.addExtension(Extension.keyUsage, false, ku);
		    
//			Certificate caCertificate =  getRootCACertificate();
			
		    SubjectPublicKeyInfo caPublicKeyInfo = SubjectPublicKeyInfo.getInstance(caCertificate.getPublicKey().getEncoded());
		    DigestCalculator digCalc = new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));
		    AuthorityKeyIdentifier aki =  new X509ExtensionUtils(digCalc).createAuthorityKeyIdentifier(caPublicKeyInfo);
		    builder.addExtension(Extension.authorityKeyIdentifier, false, aki);

			
			ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(getRootCAKey());
			//ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keys.getPrivate());
			//ContentSigner signer = new JcaContentSignerBuilder("SHA256WithDSA").setProvider(new BouncyCastleProvider()).build(keys.getPrivate());
			final X509CertificateHolder holder = builder.build(signer);
			Certificate certificate = new JcaX509CertificateConverter().getCertificate(holder);
						
			//byte pfx[] = CertificateParser.Get().writePfx(caCertificate, certificate, keys.getPrivate(), "alejo");
			
			return certificate;
		}
		catch (OperatorCreationException | CertificateException | IOException e)  {
			throw new SignatureException(e);
		}
	}
	
	public Certificate createKbeeCertificate(KeyPair keys) throws SignatureException {
		try {
			Security.addProvider(new BouncyCastleProvider());
			
			//KeyPair keys=createKeys();
			
	        X500NameBuilder issuerbuilder = new X500NameBuilder();
	        issuerbuilder.addRDN(BCStyle.CN, "KBEE");
	    	X500Name issuer = issuerbuilder.build();
		    
	        X500NameBuilder subjectbuilder = new X500NameBuilder();
	        subjectbuilder.addRDN(BCStyle.CN, "KBEE");
	        //subjectbuilder.addRDN(BCStyle.O, "Digital Signature");
	        X500Name subjectDN = subjectbuilder.build();

			SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keys.getPublic().getEncoded());
			Date start = new Date();
			Date until = Date.from(LocalDate.now().plus(365, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.UTC));
			   
			X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer,
				new BigInteger(10, new SecureRandom()), //Choose something better for real use
				start,
				until,
				subjectDN,
				subPubKeyInfo
			);
			
		    builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
		    KeyUsage ku = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign);
		    builder.addExtension(Extension.keyUsage, false, ku);
			
			ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keys.getPrivate());
			//ContentSigner signer = new JcaContentSignerBuilder("SHA256WithDSA").setProvider(new BouncyCastleProvider()).build(keys.getPrivate());
			final X509CertificateHolder holder = builder.build(signer);
			Certificate certificate = new JcaX509CertificateConverter().getCertificate(holder);
			return certificate;
		}
		catch (OperatorCreationException | CertificateException | CertIOException e)  {
			throw new SignatureException(e);
		}
	}
	
	private Domain getKbeeDomain() {
		return getContentDao().findDomainByName("kbee");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

package test.com.novamens.kbee.signature;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


import java.security.InvalidKeyException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;


import com.messagebird.MessageBirdClient;
import com.messagebird.MessageBirdService;
import com.messagebird.MessageBirdServiceImpl;
import com.messagebird.exceptions.GeneralException;
import com.messagebird.exceptions.UnauthorizedException;
import com.messagebird.objects.MessageResponse;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.jupiter.api.Test;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class SignTest {
	


	@Test
	public void test() {
		try {
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
	        keyGen.initialize(512);
	        KeyPair pair = keyGen.genKeyPair();
	        byte[] publicKey = pair.getPublic().getEncoded();
	        
	        generateV3Certificate(pair);
	        // System.out.println("x.509 certificate is successfully generated!");
        
		}
		catch (Exception e) {
			
		}
	}
	
	  public static X509Certificate generateV3Certificate(KeyPair pair) throws InvalidKeyException,  NoSuchProviderException, SignatureException, OperatorCreationException, CertificateException {
		  Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		    X500Name name = new X500Name("cn=Annoying Wrapper"); //cn=user@domain
		    SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pair.getPublic().getEncoded());
		    final Date start = new Date();
		    final Date until = Date.from(LocalDate.now().plus(365, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.UTC));
		    
		    final X509v3CertificateBuilder builder = new X509v3CertificateBuilder(name,
		            new BigInteger(10, new SecureRandom()), //Choose something better for real use
		            start,
		            until,
		            name,
		            subPubKeyInfo
		    );
		    ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").setProvider(new BouncyCastleProvider()).build(pair.getPrivate());
		    final X509CertificateHolder holder = builder.build(signer);

		    Certificate cert = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(holder);
		    return (X509Certificate)cert;
  }

}

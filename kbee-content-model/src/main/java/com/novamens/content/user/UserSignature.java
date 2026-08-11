package com.novamens.content.user;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.novamens.dom.DomainObject;
import com.novamens.security.User;

public interface UserSignature extends com.novamens.dom.Object, DomainObject {
	public SignatureType getType();
	public UserDevice getDevice();
	public User getUser();
	public UserProfile getUserProfile();
	public Certificate getCertificate();
	public PrivateKey getPrivateKey() throws IOException;
}

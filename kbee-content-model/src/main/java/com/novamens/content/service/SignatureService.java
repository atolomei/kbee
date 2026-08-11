package com.novamens.content.service;

import java.security.cert.Certificate;

import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserSignature;
import com.novamens.service.ObjectService;
import com.novamens.signature.SignatureException;

public interface SignatureService extends ObjectService {
	public UserSignature updateSignature(UserDevice device) throws SignatureException;
	public UserSignature updateSignature(UserDevice device, Certificate cetertificate, KBFile handwriteImage) throws SignatureException;
	public boolean verify(UserDevice device, String data, String signedData) throws SignatureException;
	public void delete(UserSignature signature);
}
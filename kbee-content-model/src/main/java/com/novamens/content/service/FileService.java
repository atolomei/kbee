package com.novamens.content.service;

import java.io.IOException;

import com.novamens.content.resource.KBFile;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.user.UserSignature;
import com.novamens.signature.SignatureException;

public interface FileService extends ResourceService {
	
	public KBFSResourceService getKBFSService();
	
	public KBFile setVersion(KBFile version);
	
	public KBFile sign(UserSignature signature, String stream) throws SignatureException;
	public KBFile getSigned(UserSignature signature, String signaturestream) throws IOException;
}
package com.novamens.content.service;

import java.security.cert.Certificate;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.PersonSet;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserSignature;
import com.novamens.content.user.externalLogin.ExternalPlatformId;
import com.novamens.security.TokenSubmission;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.signature.SignatureException;

public interface PersonService extends ObjectService {
	
	public void registerDevice(UserDevice userDevice);
	public void updateDevice(UserDevice device);
	public void delete(UserDevice device);
	
	public TokenSubmission sendToken(Content content);
	
	public UserSignature updateSignature(UserDevice device, Certificate certificate, KBFile handwriteImage) throws SignatureException;
	public UserSignature updateSignature(UserDevice device) throws SignatureException;
	public void delete(UserSignature signature);
	
	public User createUser();
	public User createUser(List<Role> roles, List<ExternalPlatformId> platforms);
	public User updateUser(List<Role> roles, List<ExternalPlatformId> platforms);
	public User updateUser();
	
	public void setUserFrom(Person person);
	public User getUser();
	
	public String getUserName();
	
	public String getIdentityType();
	public String getIdentityDocument();
	public String getOrganization();
	
	public void addTo(PersonSet dataset);
	public void linkToUser(User user);
	
	public PersonMember getUserMember();
} 
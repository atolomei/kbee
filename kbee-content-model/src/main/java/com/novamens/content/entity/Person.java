package com.novamens.content.entity;

import java.time.LocalDate;

import com.novamens.content.resource.KBFile;


public interface Person extends Entity {
	
	public String getFirstName();
	public void setFirstName(String name);
	
	public String getLastName();
	public void setLastName(String surname);
	
	public String getLastFirstName();
	
	public String getEmail();					
	public void setEmail(String email);

	public String getAddress();
	public void setAddress(String surname);
	
	public String getPhone();					
	public void setPhone(String phone);
	
	public String getDescription();					
	public void setDescription(String desc);
	
	public LocalDate getBirthDate();
	public void setBirthDate(LocalDate date);
	
	public KBFile getPhoto();
	public void setPhoto(KBFile file);

	public String getFirstLastName();
	
	public String getWorkPosition();
	public void setWorkPosition(String pos);
	String getBusinessTitle();
	
	
	public boolean isPhotoDomainLogo();
	public void  setPhotoDomainLogo(boolean b);

	boolean isEmailValidated();
	public void setEmailValidated(boolean b);
	public void setIsEmailValidated(boolean b);
	boolean isDefaultPhoto();
	void setDefaultPhoto(boolean b);
	
}

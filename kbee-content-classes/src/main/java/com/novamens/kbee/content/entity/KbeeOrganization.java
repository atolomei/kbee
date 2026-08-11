package com.novamens.kbee.content.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.novamens.content.entity.Organization;


@Entity
@Table(name = "ORGANIZATION")
public class KbeeOrganization extends KbeeEntity implements Organization {
	
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "WEBSITE")
	private String website;
	
	@Column(name = "EMAIL")
	private String email;
	
	@Column(name = "ADDRESS")
	private String address;
	
	@Column(name = "PHONE")
	private String phone;
	
	public String getName()	{
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDisplayName() {
		return getName();
	}
	
	public String getWebsite() {
		return website; 
	}
	
	public String getEmail()    	{return email;	 }
	public String getAddress()		{return address; }
	public String getPhone()		{return phone;	 }
					
	public void setWebsite(String url)		{this.website=url;		}
	public void setEmail(String email) 		{this.email=email;		}
	public void setAddress(String address)	{this.address=address;	}
	public void setPhone(String phone)		{this.phone=phone;		}
}

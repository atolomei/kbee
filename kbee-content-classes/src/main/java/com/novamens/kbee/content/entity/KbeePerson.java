package com.novamens.kbee.content.entity;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.entity.Person;
import com.novamens.content.entity.Profile;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.resource.KBFileImpl;

import com.novamens.kbee.security.KbeeUser;

/**
 *  Person -> Entity  
 *  Person -> userProfile -> User
 */
@Entity
@Table(name = "PERSON")
@PrimaryKeyJoinColumn(name="ENTITY_ID")
public class KbeePerson extends KbeeEntity implements Person, Indexable {

	@Column(name = "emailvalidated")
	private boolean emailvalidated;

	@Column(name = "email")
	private String email;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "phone")
	private String phone;
	
	@Column(name = "workposition")
	private String workPosition;
	
	@Column(name = "website")
	private String website;
	
	@Column(name = "FIRSTNAME")
	private String firstName;
	
	@Column(name = "LASTNAME")
	private String lastName;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@Column(name = "BIRTHDATE")
	private LocalDate birthDate;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KBFileImpl.class, cascade=CascadeType.REMOVE)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "photo")
	private KBFile photo;
	
	@Column(name = "photo_domain_logo")
	private boolean photoDomainLogo = false;
	
	@Column(name = "isdefaultphoto")
	private boolean isdefaultphoto;

	transient String wpos=null;
	
	
	public KbeePerson() {
	}
	
	
	@Override
	public void  setDefaultPhoto(boolean b) {
		this.isdefaultphoto=b;
	}
	
	@Override
	public boolean isDefaultPhoto() {
		return this.isdefaultphoto;
	}
	
	
	
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String name) {
		this.firstName=name;
		for (Profile profile : getProfiles()) {
			if (profile instanceof UserProfile) {
				if (((UserProfile)profile).getUser()!=null)
					((KbeeUser)((UserProfile)profile).getUser()).setFirstName(name);
			}
		}
	}
	
	public String getLastName() {
		return lastName; 
	}
	
	/**
	 * 
	 * 
	 */
	public void setLastName(String name) {
		this.lastName=name;
		for (Profile profile : getProfiles()) {
			if (profile instanceof UserProfile) {
				if (((UserProfile)profile).getUser()!=null)
					((KbeeUser)((UserProfile)profile).getUser()).setLastName(name);
			}
		}
	}

	@Override
	public void setState(ObjectState state)	{
		super.setState(state);
		for (Profile profile : getProfiles()) {
			if (profile instanceof UserProfile) {
				if (((UserProfile)profile).getUser()!=null)
					((KbeeUser)((UserProfile)profile).getUser()).setState(state);
			}
		}
	}
	
	
	@Override
	public String getDisplayName() {
		if (getLastName()!=null && getLastName().length()>0)
			return getLastName() +  ((getFirstName()!=null && getFirstName().length()>0) ? (", "+getFirstName()):"");
		else
			return getFirstName();
		
	}
	
	
	public void setWorkPosition(String pos)	{
		this.workPosition= pos; 
	}
	
	@Override
	public String getBusinessTitle()	{
		return getWorkPosition(); 
	}
	
	@Override
	public String getWorkPosition()	{
		
		
		
		if (this.workPosition!=null && this.workPosition.length()>0) {
			return this.workPosition;
		}
		if (wpos!=null)
			return wpos;
		
		UserProfile profile = this.getProfile(UserProfile.class);
		
		List<UserRole> list = profile!=null ? profile.getRoles() : null;
		if (list!=null && !list.isEmpty())
			wpos=list.get(0).getRole().getName();
		return wpos;
	}
	
	
	public void setEmailValidated(boolean b) {
		this.emailvalidated=b;
	}
	
	
	public void setIsEmailValidated(boolean b) {
		this.emailvalidated=b;
	}
	
	@Override
	public boolean isEmailValidated() {
		return this.emailvalidated;
	}
	
	@Override
	public boolean isPhotoDomainLogo() {
		return photoDomainLogo;
	}

	@Override
	public void setPhotoDomainLogo(boolean photoDomainLogo) {
		this.photoDomainLogo = photoDomainLogo;
	}


	@Override
	public String getDescription()	{
		return description; 
	}
	
	public void setDescription(String description)	{
		this.description=description;
	}

	public String getAddress()	{
		return address; 
	}
	
	public void setAddress(String address)	{
		this.address=address;
	}
	
	@Override
	public String getPhone()	{
		return phone;	 
	}
	
	public void setPhone(String phone) {
		this.phone=phone;
	}
	
		public String getWebsite()	{
		return website; 
	}
	
	public void setWebsite(String website) { 
		this.website=website;
	}
	
	@Override
	public String getEmail() {
		return email;	 
	}
	
	public void setEmail(String email) {
		this.email=email;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}
	
	@Override
	public LocalDate getBirthDate(){
		return birthDate;
	}
	
	public void setPhoto(KBFile photo) {
		if (this.photo!=null && !this.photo.getId().equals(photo.getId())) {
			((KBFileImpl)photo).setVersion(this.photo.getVersion()+1);
			((KBFileImpl)photo).setPreviousVersion(this.photo);
			((KBFileImpl)photo).setOId(this.photo.getOId());
		}
		this.photo = photo;	
	}
	
	@Override
	public KBFile getPhoto(){
			return photo;
	}
	
	@Override
	public String getLastFirstName() {
		StringBuilder title = new StringBuilder();
		if (getLastName()!=null) 
			title.append(getLastName());
		if ( (getFirstName()!=null) && (getFirstName().length()>0)) {
			if (title.length()>0)
				title.append(", ");
			title.append(getFirstName());
		}		
		return title.toString();
	}

	@Override
	public String getName() {
		return getDisplayName();
	}

	@Override
	public String getFirstLastName() {
		StringBuilder title = new StringBuilder();
	
		if (getFirstName()!=null) 
			title.append(getFirstName());
		
		if (getLastName()!=null) {
			if (title.length()>0)
				title.append(" ");
			title.append(getLastName());
		}		
		return title.toString();

	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Id -> " + (getId()!=null?getId().toString():"null"));
		str.append(" | Name -> " +  getFirstLastName());
		str.append(" | Email -> " +  (getEmail()!=null? getEmail():"null"));
		try {
			UserProfile profile = this.getProfile(UserProfile.class);
			String un = profile!=null ? profile.getUser().getUserName() : null;
			str.append(" | User -> " +  (un!=null? un:"null"));
		} 
		catch (Exception e) {
			str.append(" | User -> " +  e.getClass().getName());
		}
		return str.toString();
	}

}

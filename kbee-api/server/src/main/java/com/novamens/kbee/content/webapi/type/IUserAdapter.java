package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.security.acl.Group;

import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiAttributeProxy;
import kbee.api.model.IAttributeValues;
import kbee.api.model.ApiResource;
import kbee.api.model.ApiUser;
import kbee.api.model.IUserRole;

public class IUserAdapter implements Adapter<Person, ApiUser> {

	boolean includeall = false;	
	
	public IUserAdapter() {
	}
	
	public IUserAdapter(boolean all) {
		includeall = all;
	}
	
	public ApiUser adapt(Person person) {
		ApiUser iuser = new ApiUser();
		
		iuser.setId(String.valueOf(person.getId()));
		iuser.setFirstName(person.getFirstName());
		iuser.setLastName(person.getLastName());
		iuser.setEmail(person.getEmail());
		iuser.setWorkPosition(person.getWorkPosition());
		iuser.setDomain(person.getDomain().getName());
		if (includeall) iuser.setPassword(null);
		
		if (person.getPhoto()!=null) {
			ApiResource photo = new ApiResource();
			photo.setId(String.valueOf(person.getPhoto().getId()));
			photo.setHRef(UriHelper.getUri(person.getPhoto()));
			photo.setName(person.getPhoto().getName());
			photo.setRel("file");
			iuser.setPhoto(photo);
		}
		
		iuser.setLastModifiedDate(person.getLastModifiedOffsetDateTime());
		
		UserProfile userprofile = person.getProfile(UserProfile.class);
		if (userprofile!=null && userprofile.getUser()!=null) {
			User user = userprofile.getUser();
			iuser.setName(user.getName());
			iuser.setLocale(user.getLocale());
			iuser.setEnabled(user.isEnabled());
			iuser.setTimeZone(user.getTimeZone());
			if (includeall) iuser.setPassword(((KbeeUser)user).getPassword());
			for (Group group : user.getGroups()) {
				ApiProxy proxy = new ApiProxy();
				proxy.setHRef(UriHelper.getUri(group));
				proxy.setName(group.getName());
				proxy.setRel("group");
				iuser.addGroup(proxy);
			}
			for (UserRole userRole : userprofile.getRoles()) {
				IUserRole iuserRole = new IUserRole();
				ApiProxy roleproxy = new ApiProxy();
				roleproxy.setHRef(UriHelper.getUri(userRole.getRole()));
				roleproxy.setId(String.valueOf(((KbeeAbstractRole)userRole.getRole()).getId()));
				roleproxy.setName(userRole.getRole().getName());
				roleproxy.setRel("role");
				iuserRole.setRole(roleproxy);
				if (userRole.getEntity()!=null) {
					ApiProxy entityproxy = new ApiProxy();
					entityproxy.setHRef(UriHelper.getUri(userRole.getEntity()));
					entityproxy.setId(String.valueOf(userRole.getEntity().getId()));
					entityproxy.setName(userRole.getEntity().getDisplayName());
					entityproxy.setRel("entity");
					iuserRole.setEntity(entityproxy);
				}
				iuser.setRole(iuserRole);
			}
		}
		
		List<IAttributeValues> values = new ArrayList<IAttributeValues>();
		
		if (!(person instanceof PersonMember)) {
			return iuser;
		}
		
		for (Classification classification : ((PersonMember)person).getClassification()) {
			Classifier classifier = classification.getClassifier();
			
			ApiAttributeProxy attribute = new ApiAttributeProxy();
			attribute.setHRef(UriHelper.getUri(classifier));		
			attribute.setRel("classifier");
			attribute.setName(classifier.getName());
			
			DataSetMember member = classification.getDataSetMember();
			
			ApiValue value = new ApiValue();
			value.setId(String.valueOf(member.getId()));
			value.setHRef(UriHelper.getUri(member));
			value.setValue(member.getDisplayName());
			
			values.add(new IAttributeValues(attribute, value));
		}
		
		iuser.setAttributes(values);
		
		if (person instanceof PersonMember)
			person = ((KbeePersonMember)person).getPerson();
		iuser.setPerson(new ApiProxy(String.valueOf(person.getId()), person.getDisplayName(), UriHelper.getUri(person), "person"));
		
		return iuser;	
	}
}

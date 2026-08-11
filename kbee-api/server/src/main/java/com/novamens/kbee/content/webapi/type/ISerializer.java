package com.novamens.kbee.content.webapi.type;

import java.util.List;

import org.apache.commons.lang.SerializationException;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.PersonMember;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiObject;
import kbee.api.service.ApiSerializer;

public class ISerializer implements ApiSerializer {

	public ApiObject serialize(Object object) {
		if (object instanceof Classifier) {
			return (new IClassifierAdapter()).adapt((Classifier)object);
		}
		if (object instanceof DataSet) {
			return (new IDataSetAdapter()).adapt((DataSet)object);
		}
		if (object instanceof KbeeIDoc) {
			return (new IDocAdapter(false)).adapt((KbeeIDoc)object);
		}
		if (object instanceof DataSetMember) {
			return (new IValueAdapter()).adapt((DataSetMember)object);
		}
		if (object instanceof Person) {
			return (new IPersonAdapter()).adapt((Person)object);
		}
		if (object instanceof ContentTemplate) {
			return (new ITemplateAdapter()).adapt((ContentTemplate)object);
		}
		if (object instanceof User) {
			PersonMember person = getPerson((User)object);
			return (new IUserAdapter(true)).adapt(person);
		}
		if (object instanceof KBFileImpl) {
			return (new IResourceAdapter()).adapt((KBFileImpl)object);
		}
		if (object instanceof KbeeAttribute) {
			return (new IModelAttributeAdapter()).adapt((KbeeAttribute)object);
		}

		throw new SerializationException("serialization adapter not found");
	}
	
	private PersonMember getPerson(User user) {
		UserProfile profile = getContentDao().findUserProfileByUser(user);
		Person person = profile.getPerson();
		PersonMember personMember = null;
		List<DataSetMember> members = getContentDao().findMembersByEntity(person);
		for (DataSetMember member : members) {
			if (DataSetType.USER.equals(member.getDataSet().getDataSetType())) {
				personMember = (PersonMember)member;
				break;
			}
		}
		return personMember;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

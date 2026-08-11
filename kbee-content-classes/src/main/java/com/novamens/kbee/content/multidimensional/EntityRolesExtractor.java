package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.entity.Person;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.PersonMember;
import com.novamens.content.security.EntityRole;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.kbee.content.dao.Proxy;

public class EntityRolesExtractor implements Extractor {
	private Classifier classifier;
	
	public EntityRolesExtractor() {
	}
	
	public EntityRolesExtractor(Classifier classifier) {
		setClassifier(classifier);
	}
	
	public Object extract(Object object) throws IndexerException  {
		List<String> members = new ArrayList<String>();
		Classificable classificable = (Classificable)object;
		if (classificable instanceof PersonMember) {
			Person person = ((PersonMember)classificable).getPerson();
			UserProfile userProfile = person.getProfile(UserProfile.class);
			if (userProfile!=null)
			for (UserRole userRole : userProfile.getRoles()) {
				if (userRole.getEntity()!=null && userRole.getEntity().getDataSet().equals(getClassifier().getDataSet())) {
					EntityRole role = (EntityRole)Proxy.Unproxy(userRole.getRole()); 
					if (role.getClassifier().equals(getClassifier())) {
						String id = String.valueOf(userRole.getEntity().getId());
						if (!members.contains(id))
						members.add(id);
					}
				}
			}
		}
		return members;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	public Classifier getClassifier() {
		return classifier;
	}
}

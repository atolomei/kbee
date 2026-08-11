package com.novamens.kbee.content.multidimensional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class ClassificationExtractor implements Extractor {
	private Classifier classifier;
	private boolean hierarchical = false;
	
	public ClassificationExtractor() {
	}
	
	public ClassificationExtractor(Classifier classifier) {
		setClassifier(classifier);
	}
	
	public Object extract(Object object) throws IndexerException  {
		
		if (!(object instanceof Content)) {
			if (object instanceof Classificable) {
				return extractclassification(object);
			}
		}
		
		Assert.isInstanceOf(Content.class, object);
		
		List<String> members = new ArrayList<String>();
		
		Content content = (Content)object;
		for (Classification classification : content.getClassification()) {
			Classifier classifier = classification!=null ? classification.getClassifier() : null;
			if (classifier!=null && classifier.equals(getClassifier())) {
				if (classifier.getDataSetType().equals(DataSetType.DATE)) {
					if (classification.getDateValue()!=null) {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("yyyy/MM/dd", Locale.getDefault());
						String member = formatter.format(classification.getDateValue());
						members.add(member);
					}
				}
				else {
					DataSetMember member = classification.getDataSetMember();
					if (hierarchical)
						members.addAll(getPaths(member));
					else
						members.add(String.valueOf(member.getId()));
				}
			}
		}
		
		return members;
	}
	
	
	public Object extractclassification(Object object) throws IndexerException  {
		Assert.isInstanceOf(Classificable.class, object);
		List<String> members = new ArrayList<String>();
		Classificable classificable = (Classificable)object;
		if (classificable.getClassification()!=null) {
			for (Classification classification : classificable.getClassification()) {
				Classifier classifier = classification!=null ? classification.getClassifier() : null;
				if (classifier!=null && classifier.equals(getClassifier())) {
					if (classifier.getDataSetType().equals(DataSetType.DATE)) {
						if (classification.getDateValue()!=null) {
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("yyyy/MM", Locale.getDefault());
							String member = formatter.format(classification.getDateValue());
							members.add(member);
						}
					}
					else {
						DataSetMember member = classification.getDataSetMember();
						if (member!=null) {
							if (hierarchical)
								members.addAll(getPaths(member));
							else
								members.add(String.valueOf(member.getId()));
						}
					}
				}
			}
		}
//		if (classificable instanceof PersonMember) {
//			Person person = ((PersonMember)classificable).getPerson();
//			UserProfile userProfile = person.getProfile(UserProfile.class);
//			if (userProfile!=null)
//			for (UserRole userRole : userProfile.getRoles()) {
//				if (userRole.getEntity()!=null && userRole.getEntity().getDataSet().equals(getClassifier().getDataSet())) {
//					EntityRole role = (EntityRole)Proxy.Unproxy(userRole.getRole()); 
//					if (role.getClassifier().equals(getClassifier())) {
//						String id = String.valueOf(userRole.getEntity().getId());
//						if (!members.contains(id))
//						members.add(id);
//					}
//				}
//			}
//		}
		return members;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
		hierarchical = classifier.getDataSet()!=null ? classifier.getDataSet().isHierachical() : false;
	}

	public Classifier getClassifier() {
		return classifier;
	}
	
	private List<String> getPaths(DataSetMember member) {
		return getPaths(member, member);
	}
	
	private List<String> getPaths(DataSetMember member, DataSetMember child) {
		List<String> paths = new ArrayList<>();
		if (member.getParents().isEmpty()) {
			paths.add(String.valueOf(member.getId()));
		}
		else {
			for (DataSetMember parent : member.getParents()) {
				if (!parent.equals(child)) {
					for (String path : getPaths(parent, child)) {
						path = path + "/" +
					String.valueOf(member.getId());
						paths.add(path);
					}
				}
			}
		}
		return paths;
	}
}

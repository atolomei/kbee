package kbee.importer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.model.KbeeValueMember;

import kbee.api.model.ApiValue;
import kbee.api.model.ApiAttributeProxy;
import kbee.api.model.IAttributeValues;
import kbee.api.model.ApiClassificable;
import kbee.api.model.ApiClassifier;
import kbee.api.model.IModelAttribute;
import kbee.api.service.ApiService;

public class ClassificablesImporter extends Importer {

	public ClassificablesImporter(ApiService server, LocalMatcher matcher) {
		super(server, matcher);
	}
	
	protected void syncClassifiers(ApiClassificable remote, Classificable local, List<Classifier> classifiers) {
		if (remote.getAttributes()!=null)
		for (IAttributeValues values : remote.getAttributes()) {
			Classifier classifier = getClassifier(values.getAttribute());
			if (classifier!=null) {
				List<DataSetMember> members = new ArrayList<DataSetMember>();
				if (classifier.getDataSetType().equals(DataSetType.DATE)) {
					for (ApiValue remotevalue : values.getValues()) {
						try {
 							remotevalue.setDomain(remote.getDomain());
							DataSetMember member = new KbeeValueMember(classifier.getDataSet());
							DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
							LocalDateTime datetime = LocalDateTime.parse(remotevalue.getDisplayName(), dateformat);
							OffsetDateTime localvalue = OffsetDateTime.of(datetime, OffsetDateTime.now().getOffset());
							if (localvalue!=null) {
								member.setDateValue(localvalue);
								members.add(member);
							}
						} 
						catch (Exception e) {
							logger.error(e.getMessage());
						}
					}
				}
				else {
					for (ApiValue remotevalue : values.getValues()) {
						remotevalue.setDomain(remote.getDomain());
						DataSetMember localvalue = getLocal(KbeeDataSetMember.class, remotevalue);
						if (localvalue!=null) {
							members.add(localvalue);
						}
					}
				}
				if (!members.isEmpty()) {
					local.setClassification(classifier, members);
					update(local);
				}	
			}
		}
	}
	
	protected void syncAttributes(ApiClassificable remote, Classificable local, List<AttributeTemplate> attributes) {
		if (remote.getAttributes()!=null)
		for (IAttributeValues values : remote.getAttributes()) {
			Attribute attribute = getAttribute(values.getAttribute());
			if (attribute!=null) {
				List<String> stringvalues = new ArrayList<String>();
				for (ApiValue remotevalue : values.getValues()) {
					stringvalues.add(remotevalue.getDisplayName());
				}
				local.setAttributeValues(attribute, stringvalues);
			}
		}
	}
	
	private Classifier getClassifier(ApiAttributeProxy attribute) {
		if ("classifier".equals(attribute.getRel())) {
			ApiClassifier remoteclassifier = getServer().getClassifier(attribute.getId());
			if (remoteclassifier!=null) {
				Classifier local = getLocal(KbeeClassifier.class, remoteclassifier);
				return local;
			}
		}
		return null;
	}
	
	private Attribute getAttribute(ApiAttributeProxy attribute) {
		if ("attribute".equals(attribute.getRel())) {
			IModelAttribute remoteattribute = getServer().getAttribute(attribute.getId());
			if (remoteattribute!=null) {
				Attribute local = getLocal(KbeeAttribute.class, remoteattribute);
				return local;
			}
		}
		return null;
	} 

}

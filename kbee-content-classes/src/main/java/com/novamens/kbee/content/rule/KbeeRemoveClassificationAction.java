package com.novamens.kbee.content.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.rule.RemoveClassificationAction;
import com.novamens.content.service.ContentService;

public class KbeeRemoveClassificationAction extends KbeeClassificationAction implements RemoveClassificationAction {
	private static final long serialVersionUID = 1L;

	@Transactional(propagation = Propagation.REQUIRED)
	public Object execute(Content content) {
		if (classified(content)) {
			content.setClassification(getClassifier(), getValues(content));
			List<String> parts = new ArrayList<String>();
			getValues().forEach(value -> parts.add(value.getDisplayName()));
			content.getService(ContentService.class).update(parts);
		}
		return content;
	}
	
	private List<DataSetMember> getValues(Content content) {
		List<DataSetMember> values = new ArrayList<DataSetMember>();
		List<Classification> classifications = content.getClassification(getClassifier());
		classifications.forEach(classification -> values.add(classification.getDataSetMember()));
		for (DataSetMember value : getValues()) {
			if (values.contains(value)) values.remove(value);
		}
		return values;
	}
}
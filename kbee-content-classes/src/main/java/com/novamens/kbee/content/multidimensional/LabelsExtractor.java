package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.service.LabelsService;
import com.novamens.content.user.UserLabel;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class LabelsExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		List<String> members = new ArrayList<String>();
		Content content = (Content)object;
		for (UserLabel label : content.getService(LabelsService.class).getUserLabels()) {
			try {
				members.add(String.valueOf(label.getId()));
			} catch (org.hibernate.ObjectNotFoundException e) {
				// warning ?
			}
		}
		return members;
	}
}

package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.base.PublishingStatus;
import com.novamens.dom.Versionable;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class PublishingStatusExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		List<String> members = new ArrayList<String>();
		
		Content content = (Content)object;
		
		if (content.isArchived()) {
			members.add(PublishingStatus.ARCHIVED.name());
		}
		else
		if (content.isLocked()) {
			members.add(PublishingStatus.LOCKED.name());
		}
		else 
		if (content.isRecycled()) {
			members.add(PublishingStatus.DELETED.name());
		}
		else 
		if (content.getWorkspace()!=null) {
			if (content instanceof Versionable<?>) {
				if (((Versionable<?>)content).getPreviousVersion()!=null) {
					members.add(PublishingStatus.WORKINGCOPY.name());
				}
				else {
					members.add(PublishingStatus.DRAFT.name());
				}
			}
		}
		else {
			if (content.isHeadVersion()) {
				members.add(PublishingStatus.PUBLISHED.name());
			}
			else {
				members.add(PublishingStatus.VERSION.name());
			}
		}
		
		return members;
	}
}

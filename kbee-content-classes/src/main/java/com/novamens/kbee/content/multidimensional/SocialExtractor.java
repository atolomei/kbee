package com.novamens.kbee.content.multidimensional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.social.Comment;
import com.novamens.content.social.SocialService;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;

public class SocialExtractor implements Extractor {

	public Object extract(Object object) throws IndexerException  {
		Assert.isInstanceOf(Content.class, object);
		SocialService service = ((Content)object).getService(SocialService.class);
		List<String> members = new ArrayList<String>();
		for (Comment comment : service.getComments()) {
			members.add(comment.getText());
		}
		return members;
	}
}

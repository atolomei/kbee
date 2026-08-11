package com.novamens.kbee.content.text.template;

import java.util.List;

import org.w3c.dom.Element;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentId;
import com.novamens.content.text.AncordResolver;
import com.novamens.content.text.ImageResolver;
import com.novamens.content.text.template.Include;
import com.novamens.content.text.template.IncludeResolver;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

public class ContentIncludeResolver implements IncludeResolver {
	
	public ContentIncludeResolver() {
	}
	
	public String getInclude(Include include) {
		if (include.getName().indexOf(":")<=0)
			return "-";
		String includename = include.getName().substring(include.getName().indexOf(":")+1);
		List<Content> contents = getContentDao().findContentsByTitle(includename, getDomain());
		if (contents.size()!=1) 
			return "-";
		Content content = contents.get(0);
		if (content instanceof OrganizationalText) {
			OrganizationalText template = (OrganizationalText)content;
			final String templateUri = new ContentId(template).toString();
			
			String text = template.getText().getText(new AncordResolver() {
				@Override
				public Element resolve(Element ancord) {
					return ancord;
				}
			}, new ImageResolver() {
				@Override
				public Element resolve(Element image) {
					String src = image.getAttribute("src");
					src = "/resource/content/"+ templateUri +"/" + src;
					image.setAttribute("src", src);
					return image;
				}
			});
			
			text = getBody(text);
			
			return text;
		}
		return "-";
	}
	
	private String getBody(String text) {
		String bodytag = "<BODY xmlns=\"http://www.w3.org/1999/xhtml\">";
		int i1 = text.indexOf(bodytag);
		if (i1>0) {
			int i2 = text.indexOf("</BODY>");
			if (i2>0) {
				text = text.substring(i1+bodytag.length(), i2);
			}
		}
		return text;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}

package com.novamens.content.web.admin.markup;



import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

public interface XLink {

	public AbstractLink getLink(String id);
	public IModel<String> getLabel();
	public void setIsNewTab(boolean nt);
	public boolean isNewTab();
	
	
}

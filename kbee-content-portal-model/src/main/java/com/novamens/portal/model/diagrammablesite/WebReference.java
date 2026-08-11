package com.novamens.portal.model.diagrammablesite;

import org.apache.wicket.markup.html.WebPage;

import com.novamens.content.base.Content;

public interface WebReference {

	public static final int REFERENCE_NULL = -1;
	public static final int REFERENCE_URL = 0;
	public static final int REFERENCE_PAGE = 1;
	public static final int REFERENCE_CONTENT = 2;
	
	public int getReferenceType();
	public WebPage getReferencePage();
	public Object getReference();
	
	public Content getContentReference();
	public DiagrammablePage getPageReference();
	public String getUrlReference();
	public String getReferenceAsString();
	public String getReferenceTypeStr();
	
	void setReferenceType(int type);
	void setContent(Content content);
	void setPage(DiagrammablePage page);
	void setUrl(String url);
	
}

package com.novamens.content.communication;

import java.util.List;

import com.novamens.content.base.TextContainer;
import com.novamens.content.resource.KBFile;
import com.novamens.content.text.Text;

public interface OrganizationalText extends TextContainer, Communication {
	
	public static final String CLASS_CODE ="tx";
	
	public static final String OTEXT_TYPE 			= "otext";
	
	public static final String ARTICLE_TYPE 		= "arti";
	public static final String INTERNAL_COMM_TYPE 	= "intcom";
	public static final String INTERVIEW_TYPE		= "inter";
	public static final String LETTER_TYPE 			= "letter";
	public static final String MEDIA_COVERAGE_TYPE 	= "mecov";
	public static final String NEWS_TYPE 			= "news";
	public static final String OPINION_TYPE			= "opin";
	public static final String PRESS_RELEASE_TYPE 	= "press";
	public static final String PUBLICATIONS_TYPE    = "publi";
	
	public Text getText();
	public void setText(String text);
	
	public String getSummary();
	public void setSummary(String summary);
	
	public String getMedia();
	public void setMedia(String media);
	
	public List<KBFile> getFiles();
}

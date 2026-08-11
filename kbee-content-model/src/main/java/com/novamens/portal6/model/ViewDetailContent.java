package com.novamens.portal6.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.base.DisplayMode;
import com.novamens.content.base.Resource;
import com.novamens.content.model.Classificable;
import com.novamens.content.text.Text;
import com.novamens.dom.Indexable;
import com.novamens.dom.Versionable;
import com.novamens.security.User;


/***
 *
 */
public interface ViewDetailContent extends ViewDetail, Versionable<PortalObject>, Classificable, Indexable {
	
	public String getTitle();
	public String getSubtitle();
	
	public Text getAbstract();
	public Text getText();
	
	public void setContent(Content content);
	public Content getContent();
	
	public void addRelated(Content content);
	public void setRelated(List<Content> related);
	public List<Content> getRelated();
	
	public boolean isResources();
	
	public String getGlyphIcon();

	public Map<String, String> getGeneralInfo();
	
	public TitleMode getTitleMode();
	public void setTitleMode(TitleMode tm);
	
	public boolean isAbstract();
	public boolean isViewer();
	
	public void setViewer(boolean v);
	public void setAbstract(boolean a);
	public void setResources(boolean b);
	
	public void setDisplayMode(DisplayMode tm);
	public DisplayMode getDisplayMode();
	
	public BodyTemplateType getBodyTemplateType();
	public void setBodyTemplateType(BodyTemplateType bodyTemplateType);
	
	public List<Resource> getResources();
	public List<Resource> getEnabledResources();
	public String getResourcesID();
	
	public Map<Long, Boolean> getMapEnabled();
	
	public void setDisabled(Resource object);
	public void setEnabled(Resource object);
	
	public OffsetDateTime getPublicationDate();
	public User getPublisher();
	
	public String toString();
	
	public String getContentType();
	
	// public Site getContentHomeSite();
	// public String getUrl();
	
	public boolean isDocument();
	
	boolean isTool();
	boolean isVideo();
	boolean isAudio();
	boolean isText();
	boolean isAd();
	boolean isActivity();
	
	
	
}
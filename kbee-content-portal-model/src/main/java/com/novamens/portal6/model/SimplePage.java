package com.novamens.portal6.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.dom.Object;
import com.novamens.dom.ObjectState;

import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageType;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;


public class SimplePage implements Page {

	@Override
	public String getClassKey() {
		return "page";
	}

	Site site;
	String page_key;
	String relative_url;
	

	public void setUrl( String url) {
		this.relative_url=url;
	}
	
	public String getUrl() {
		return this.relative_url;
	}
	
	public SimplePage(Site site, String page_key) {
		this.site=site;
		 this.page_key= page_key;
	}
	
	public String getKey() {
		return page_key;
	}
	//@Override
	//public void setOId(Long id) {
	//}

	@Override
	public Long getOId() {
		return null;
	}

	
	public void setName(String name) {
	}

	
	public void setTitle(String title) {
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public Site getSite() {
		return null;
	}

	@Override
	public PortalObject getParent() {
		return null;
	}

	
	public void setParent(PortalObject parent) {
	}

	@Override
	public String getMetadataAsString() {
		return null;
	}

	@Override
	public String getLanguage() {
		return null;
	}

	@Override
	public boolean isPublished() {
		return false;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public Map<String, String> getGeneralInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(Serializable id) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setState(ObjectState enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public ObjectState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCreationOffsetDateTimeColloquial() {
		return null;
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultAudit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLastModifiedUser(User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public User getLastModifiedUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		// TODO Auto-generated method stub

	}

	@Override
	public OffsetDateTime getCreationOffsetDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		// TODO Auto-generated method stub

	}

	@Override
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Domain getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDomain(Domain domain) {
		// TODO Auto-generated method stub

	}

	@Override
	public PortalObject getPreviousVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPreviousVersion(Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isHeadVersion() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHeadVersion(boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVersion(int version) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNextVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isSiteSection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAdminPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHome() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	@Override
	public String getRelativeUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PageType getPageType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Content getContent() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public Page clone() {
		return null;
	}

	@Override
	public void onAfterClone() {
	}

	@Override
	public String getContentShortenedId() {
		return null;
	}

	@Override
	public Json getCustomValuesJson() {
		return null;
	}

	@Override
	public void add(PageSection page_section) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<PageSection> getPageSections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSubtitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKey(String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String> getSpecificInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUsageInfoKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUsageInfoKey(String i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOrder(int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaults() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String treeString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IPTab> getTabs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isContentPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIsContentPage(boolean iscontent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRegularPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIsRegularPage(boolean isregular) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSiteSection(boolean is_sitesection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBuildable(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBuildable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCss(String css) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCss() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSubtitle(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDescription(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPayloadEditor() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPayloadEditor(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDataProviderInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	



}

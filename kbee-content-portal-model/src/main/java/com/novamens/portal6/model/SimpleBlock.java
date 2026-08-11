package com.novamens.portal6.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.dom.Object;
import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;

public class SimpleBlock implements Block {

	@Override
	public String getClassKey() {
		return "block";
	}

	
	
	public int getPtab() {
		return 0;
	}

	@Override
	public Long getOId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Site getSite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PortalObject getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setParent(PortalObject parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getMetadataAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPublished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDisplayName() {
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
	public String getLastModifiedOffsetDateTimeColloquial() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCreationOffsetDateTimeColloquial() {
		// TODO Auto-generated method stub
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
	public String getSubtitle() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, String> getGeneralInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getSpecificInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Json getCustomValuesJson() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AreaSection getAreaSection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAreaSection(AreaSection section) {
		// TODO Auto-generated method stub

	}

	 
	

	@Override
	public void onAfterClone() {
		// TODO Auto-generated method stub

	}

	@Override
	public Block clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String css) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKey(String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUsageInfoKey(String i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUsageInfoKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setOrder(int order) {
		// TODO Auto-generated method stub
		
	}

	

	

	@Override
	public String treeString() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public int getPTab() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public void setPTab(int ptab) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public List<IPTab> getTabs() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void setHeader(boolean b) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public boolean isHeader() {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void setOId(Long oid) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void setDefaults() {
		// TODO Auto-generated method stub
		
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

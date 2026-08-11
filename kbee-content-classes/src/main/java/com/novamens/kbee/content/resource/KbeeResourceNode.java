package com.novamens.kbee.content.resource;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ObjectService;
import com.novamens.service.ServiceNotFoundException;

public class KbeeResourceNode implements ResourceNode {
	private Resource resource;
	private ResourceFolder folder;
	private boolean isIndex = false;
	
	public KbeeResourceNode(Resource resource, ResourceFolder folder) {
		setResource(resource);
		setFolder(folder);
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public ResourceFolder getFolder() {
		return folder;
	}
	
	public void setFolder(ResourceFolder folder) {
		this.folder = folder;
	}
	
	public boolean isIndex() {
		return isIndex;
	}

	public void setIndex(boolean isIndex) {
		this.isIndex = isIndex;
	}

	public Serializable getId() {
		return getResource().getId();
	}
	
	public String getName() {
		return getResource().getName();
	}
	
	public String getDescription() {
		return getResource().getDescription();
	}
	
	public void setDomain(Domain domain) {
		getResource().setDomain(domain);
	}
	
	public String getDisplayName() {
		return getResource().getDisplayName() == null 
			? getResource().getTitle() 
			: getResource().getDisplayName();
	}
	
	public String getTitle() {
		return getResource().getTitle();
	}
	
	public String getPath() {
		return getResource().getPath();
	}
	
	public int getVersion() {
		return getResource().getVersion();
	}
	
	public long getSize() {
		return getResource().getSize();
	}
	
	public String getUrl() {
		return getResource().getUrl();
	}
	
	public void setDefaultAudit() {
		getResource().setDefaultAudit();
	}
	
	public void setLastModifiedUser(User user) {
		getResource().setLastModifiedUser(user);
	}
	
	public User getLastModifiedUser() {
		return getResource().getLastModifiedUser();
	}
	
	public void setCreationOffsetDateTime(OffsetDateTime date) {
		getResource().setCreationOffsetDateTime(date);
	}
	
	public void setId(Serializable id) {
		getResource().setId(id);
	}
	
	public OffsetDateTime getCreationOffsetDateTime() {
		return getResource().getCreationOffsetDateTime();
	}
	
	public <T extends ObjectService> T getService(Class<T> service) throws ServiceNotFoundException {
		return getResource().getService(service);
	}
	
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		getResource().setLastModifiedOffsetDateTime(date);
	}
	
	public void setState(ObjectState enabled) {
		getResource().setState(enabled);
	}
	
	public OffsetDateTime getLastModifiedOffsetDateTime() {
		return getResource().getLastModifiedOffsetDateTime();
	}
	
	public String getCreationOffsetDateTimeColloquial() {
		return getResource().getCreationOffsetDateTimeColloquial();
	}
	
	public ObjectState getState() {
		return getResource().getState();
	}
	
	public Domain getDomain() {
		return getResource().getDomain();
	}
	
	public boolean isPublicArea() {
		return getResource().isPublicArea();
	}
	
	public boolean isBinaryFile() throws IOException {
		return getResource().isBinaryFile();
	}
	
	public ResourceTag getGroup() {
		return getResource().getGroup();
	}
	
	public void setTitle(String title) {
		getResource().setTitle(title);
	}
	
	public void setDescription(String des) {
		getResource().setDescription(des);
	}
	
	public Resource getPreviousVersion() {
		return getResource().getPreviousVersion();
	}
	
	public String getMetadataAsString() {
		return getResource().getMetadataAsString();
	}
	
	public String getMetadataAsString(DateTimeFormatter df) {
		return getResource().getMetadataAsString(df);
	}
	
	public String getGlyphIcon() {
		return getResource().getGlyphIcon();
	}
	
	public String getLastModifiedOffsetDateTimeColloquial() {
		return getResource().getLastModifiedOffsetDateTimeColloquial();
	}
	
	public String getLastModifiedOffsetDateTimeColloquial(String classago) {
		return getResource().getLastModifiedOffsetDateTimeColloquial(classago);
	}
	
	public String getFontAwesomeFreeIcon() {
		return getResource().getFontAwesomeFreeIcon();
	}
	
	public boolean isInPortalVersion() {
		return getResource().isInPortalVersion();
	}
	
	public void setInPortalVersion(boolean b) {
		getResource().setInPortalVersion(b);
	}
	
	public Long getOId() {
		return getResource().getOId();
	}
	
	public AuditSet getAuditSet() {
		return getResource().getAuditSet();
	}
	
	@Override
	public OffsetDateTime getUploadOffsetDateTime() {
		return getResource().getUploadOffsetDateTime();
	}
	
	@Override
	public User getUploadUser() {
		return getResource().getUploadUser();
	}
	@Override
	public int getWidth() {
		return getResource().getWidth();
	}
	
	@Override
	public int getHeight() {
		return getResource().getHeight();
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Resource)) return false;
		return getResource().getId().equals(((Resource)object).getId());
	}
}
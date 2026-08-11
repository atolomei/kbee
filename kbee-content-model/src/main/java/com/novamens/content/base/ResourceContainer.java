package com.novamens.content.base;

import java.io.Serializable;
import java.util.List;

import com.novamens.content.resource.KBFile;
import com.novamens.security.Identifiable;

public interface ResourceContainer extends Identifiable  {
	
	public Serializable getId();
	public Serializable getOId();
	
	public String getTitle();
	
	public List<KBFile> getFiles();
	public List<KBFile> getFiles(String tag);
	
	public void addFile(KBFile file);
	public void addFile(KBFile file, ResourceTag tag);
	public void removeFile(KBFile file);
	public void restoreFile(KBFile file);
	public void setFiles(List<KBFile> files);
	public boolean contains(KBFile file);
	public KBFile getFirstFile();

	public Resource getResource(String name);
	public Resource getResource(ResourceURI uri);
	public ResourceURI getURI(Resource resource);
	
	public void addResource(Resource resource);
	public void addResource(Resource resource, ResourceTag tag);
	public void addResource(Resource resource, ResourceFolder folder, ResourceTag tag);
	
	public List<Resource> getResources();
	public List<Resource> getResources(String tag);
	
	public void setResources(List<Resource> files);
	public void setResources(List<Resource> files, ResourceTag tag);
	public void setResourceNodes(List<ResourceNode> files, ResourceTag tag);
	
	public List<Resource> getPortalEnabledResources();
	
	public void setTag(Resource resource, ResourceTag tag);
	public void setFolder(Resource resource, ResourceFolder folder);
	
	public ResourceTag getTag(Resource resource);

	@Deprecated
	public void setPrivate(Resource resource);
	@Deprecated
	public void setPublic(Resource resource);
	@Deprecated
	public boolean isPublic(Resource resource);
	@Deprecated
	public void addFile(KBFile file, boolean publicarea);
	@Deprecated
	public List<Resource> getResources(boolean publicArea);
	@Deprecated
	public ResourceFolder getFolder(Resource resource);
	@Deprecated
	public void addFile(KBFile file, ResourceTag tag, boolean publicarea);

}
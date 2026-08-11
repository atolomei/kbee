package com.novamens.kbee.content.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import com.novamens.content.base.ContentResource;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.ResourceURI;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.content.model.KbeeContentResource;
import com.novamens.kbee.content.resource.AbstractResource;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.util.KbeeRuntimeException;

import io.odilon.util.FileNameNormalizer;

@MappedSuperclass
public class KbeeResourceContainer extends KbeeContent implements ResourceContainer {
	
	/**
	 * <p>ContentResources are deleted with the Content<p> 
	 */
	@OneToMany(
		mappedBy = "content", 
		cascade = CascadeType.ALL, 
		orphanRemoval = true,
		targetEntity = KbeeContentResource.class
	)
	@OrderColumn(name="position", updatable=false)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
	private List<ContentResource> resources = new ArrayList<ContentResource>();
	
	public KbeeResourceContainer(ContentTemplate template) {
		 super(template);
	}
	
	public KbeeResourceContainer() {
	}

	public Resource getResource(String name) {
		
		for (ContentResource contentresourse : resources) {
			if (contentresourse!=null && contentresourse.getResource().getName().toLowerCase().equals(name.toLowerCase()))
				return contentresourse.getResource();
		}
		
		for (ContentResource contentresourse : resources) {
			if (contentresourse!=null && contentresourse.getResource().getVersion()>1) {
				Resource resource = contentresourse.getResource().getPreviousVersion();
				while (resource!=null) {
					if (resource.getName().equals(name)) {
						return resource;
					}
					else {
						resource = resource.getPreviousVersion();
					}
				}
				
			}
		}
		return null;
	}
	
	@Override
	public Resource getResource(ResourceURI uri) { 
		 
		ResourceFolder folder = null;
		for (String fragment : uri.getPath()) {
			for (ContentResource contentresourse : resources) {
				if (contentresourse!=null && 
					contentresourse.getResource() instanceof ResourceFolder && 
					((folder==null)|| (folder!=null && contentresourse.getFolder()!=null && contentresourse.getFolder().equals(folder))) && 
					contentresourse.getResource().getName().toLowerCase().equals(fragment.toLowerCase())) {
					folder = (ResourceFolder)contentresourse.getResource();
					break;
				}	
			}
			if (folder==null) {
				return null;
			}
		}
		
		String name = uri.getName().toLowerCase();
		
		for (ContentResource contentresourse : resources) {
			if (contentresourse!=null && 
					((folder==null && contentresourse.getFolder()==null)|| (folder!=null && contentresourse.getFolder()!=null && contentresourse.getFolder().equals(folder))) && 
					contentresourse.getResource().getName().toLowerCase().equals(name))
				return contentresourse.getResource();
		}
		
		for (ContentResource contentresourse : resources) {
			if (contentresourse!=null && 
				((folder==null && contentresourse.getFolder()==null)|| (folder!=null && contentresourse.getFolder()!=null && contentresourse.getFolder().equals(folder))) && 
				contentresourse.getResource().getVersion()>1) {
				Resource resource = contentresourse.getResource().getPreviousVersion();
				while (resource!=null) {
					if (resource.getName().equals(uri.getName())) {
						return resource;
					}
					else {
						resource = resource.getPreviousVersion();
					}
				}
				
			}
		}
		return null;
	}
	
	@Override
	public ResourceURI getURI(Resource resource) {
		
		String path = "";
		
		Resource context = resource;
		while (context!=null) {
			boolean found = false;
			for (ContentResource contentresourse : resources) {
				if (contentresourse!=null && 
					context.equals(contentresourse.getResource())) {
					if (!"".equals(path)) path = "/"+path;
					path = context.getName() + path;
					context = contentresourse.getFolder();
					found = true;
					break;
				}
			}
			if (!found) {
				return null;
			}
		}	
		
		return new ResourceURI(path);
	}

	
	@Override
	public List<KBFile> getFiles() {
		List<KBFile> files = new ArrayList<KBFile>();
		for (ContentResource contentresource : this.resources) { 
			if (contentresource!=null && contentresource.getResource() instanceof KBFile)
				files.add((KBFile)contentresource.getResource());
		}
		return files;
	}
	
	private transient List<Resource> list_resources = null; 
	
	public List<Resource> getResources() {
		
		if (list_resources!=null)
			return list_resources;
		
		list_resources = new ArrayList<Resource>();
		
		for (ContentResource contentresource : this.resources) {
			if (contentresource!=null) {
				list_resources.add(contentresource.getResource());
			}	
		}
		return list_resources;
	}
	
	public List<ContentResource> getContentResources() {
		return this.resources;
	}
	
	@Override
	public void setFiles(List<KBFile> files) {
		List<Resource> resources = new ArrayList<Resource>();
		resources.addAll(files);
		setResources(resources, true);
		list_resources = null;
	}
	
	@Override
	public void setResources(List<Resource> resources) {
		
		List<ContentResource> contentresources = new ArrayList<ContentResource>(resources.size());
		
		list_resources = null;
		
		boolean found = false;
		
		while(!found) {
			found = true;
			for(ContentResource contentresource : this.resources) {
				found = false;
				for(Resource resource : resources) {
					if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
						found = true;
						break;
					}
				}
				if (!found) {
					this.resources.remove(contentresource);
					break;
				}
			}
		}
 
		int order = -1;
		for(Resource resource : resources) {
			order++;
			ContentResource contentresource = null;
			for (ContentResource existingcontentresource : this.resources) {
				if (existingcontentresource !=null && existingcontentresource.getResource().getId().equals(resource.getId())) {
					contentresource = existingcontentresource;
					break;
				}	
			}
			if (contentresource==null) {
				contentresource = new KbeeContentResource(this, resource);
				this.resources.add(contentresource);
			}
			((KbeeContentResource)contentresource).setOrder(order);
			contentresources.add(contentresource);
		}
	}
	
	private transient List<ContentResource> deleted = null;
	
	public void setResources(List<Resource> resources, ResourceTag tag) {
		
		List<ContentResource> tagresources = new ArrayList<ContentResource>(); 
				
		for(ContentResource cr : this.resources) {
			if (cr!=null && cr.getTag()!=null && tag!=null && cr.getTag().equals(tag)) {
				tagresources.add(cr);
			}
		}
				
		for(ContentResource contentresource : tagresources) {
			boolean found = false;
			for(Resource resource : resources) {
				if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
					found = true;
					break;
				}
			}
			if (!found && tag.isMultiple()) {
				if (deleted==null) deleted = new ArrayList<ContentResource>();
				this.resources.remove(contentresource);
				deleted.add(contentresource);
			}
		}
		
		for (Resource resource : resources) {
			ContentResource contentresource = null;
			for(ContentResource cr : this.resources) {
				if (cr!=null && cr.getResource().getId().equals(resource.getId())) {
					contentresource = cr;
					break;
				}
			}
			if (deleted!=null)
			for(ContentResource cr : deleted) {
				if (cr!=null && cr.getResource().getId().equals(resource.getId())) {
					contentresource = cr;
					this.resources.add(cr);
					break;
				}
			}
			if (contentresource!=null) {
				if (tag!=null && !tag.isMultiple()) {
					ContentResource tagresource = tagresources.isEmpty() ? null : tagresources.get(0); 
					if (tagresource!=null && !tagresource.getResource().getId().equals(resource.getId())) {
						((AbstractResource)resource).setPreviousVersion(tagresource.getResource());
						((AbstractResource)resource).setVersion(tagresource.getResource().getVersion()+1);
						((AbstractResource)resource).setOId(tagresource.getResource().getOId());
						this.resources.remove(tagresource);
					}
				}
				((KbeeContentResource)contentresource).setTag(tag);
			}
			else {
				if (tag!=null && !tag.isMultiple()) {
					ContentResource tagresource = tagresources.isEmpty() ? null : tagresources.get(0); 
					if (tagresource!=null) {
						((AbstractResource)resource).setPreviousVersion(tagresource.getResource());
						((AbstractResource)resource).setVersion(tagresource.getResource().getVersion()+1);
						((AbstractResource)resource).setOId(tagresource.getResource().getOId());
						this.resources.remove(tagresource);
					}
				}
				contentresource = new KbeeContentResource(this, resource, tag, true);
				this.resources.add(contentresource);
			}
		}
 	}
	
	public void setResourceNodes(List<ResourceNode> resources, ResourceTag tag) {
		
		List<ContentResource> tagresources = new ArrayList<ContentResource>(); 
				
		for(ContentResource cr : this.resources) {
			if (cr!=null && cr.getTag()!=null && tag!=null && cr.getTag().equals(tag)) {
				tagresources.add(cr);
			}
		}
				
		for(ContentResource contentresource : tagresources) {
			boolean found = false;
			for(Resource resource : resources) {
				if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
					found = true;
					break;
				}
			}
			if (!found && tag.isMultiple()) {
				this.resources.remove(contentresource);
			}
		}
		
		for (ResourceNode node : resources) {
			ContentResource contentresource = null;
			Resource resource = node.getResource();
			ResourceFolder folder = node.getFolder();
			for(ContentResource cr : this.resources) {
				if (cr!=null && cr.getResource().getId().equals(resource.getId())) {
					contentresource = cr;
					break;
				}
			}
			if (contentresource!=null) {
				if (tag!=null && !tag.isMultiple()) {
					ContentResource tagresource = tagresources.isEmpty() ? null : tagresources.get(0); 
					if (tagresource!=null && !tagresource.getResource().getId().equals(resource.getId())) {
						((AbstractResource)resource).setPreviousVersion(tagresource.getResource());
						((AbstractResource)resource).setVersion(tagresource.getResource().getVersion()+1);
						((AbstractResource)resource).setOId(tagresource.getResource().getOId());
						this.resources.remove(tagresource);
					}
				}
				((KbeeContentResource)contentresource).setTag(tag);
				((KbeeContentResource)contentresource).setFolder(folder);
//				if (node.isIndex()) {
//					((KbeeContentResource)contentresource).setIndex(node.isIndex());
//				}
				((KbeeContentResource)contentresource).setIndex(node.isIndex());
			}
			else {
				
				if (tag!=null && !tag.isMultiple()) {
					ContentResource tagresource = tagresources.isEmpty() ? null : tagresources.get(0); 
					if (tagresource!=null) {
						((AbstractResource)resource).setPreviousVersion(tagresource.getResource());
						((AbstractResource)resource).setVersion(tagresource.getResource().getVersion()+1);
						((AbstractResource)resource).setOId(tagresource.getResource().getOId());
						this.resources.remove(tagresource);
					}
				}
				contentresource = new KbeeContentResource(this, resource, folder, tag);
				
				
		for(ContentResource cr : this.resources) {
			if (cr!=null && cr.getResource().getId().equals(resource.getId())) {
				System.out.print(isCommentsEnabled());
			}
		}
				
				
				((KbeeContentResource)contentresource).setIndex(node.isIndex());
				this.resources.add(contentresource);
			}
		}
 	}
	
	public ResourceFolder getFolder(Resource resource) {
		if (resource==null) return null;
		for (ContentResource contentresource : this.resources) {
			if (contentresource!=null && 
				contentresource.getResource()!=null &&
				contentresource.getResource().getId().equals(resource.getId())) {
				return contentresource.getFolder();
			}	
		}
		return null;
	}
	
	@Override
	public void addFile(KBFile file) {
		resources.add(new KbeeContentResource(this, file));
		list_resources = null;
	}
	
	@Override
	public void restoreFile(KBFile version) {
		KBFile file = null;
		ResourceTag tag = null;
		for (ContentResource contentresource : this.resources) { 
			if (contentresource!=null && contentresource.getResource() instanceof KBFile) {
				if (version.getOId().equals(((KBFile)contentresource.getResource()).getOId())) {
					file = (KBFile)contentresource.getResource();
					tag = contentresource.getTag();
					break;
				}
			}	
		}
		if (file==null) {
			throw new KbeeRuntimeException("file not found!");
		}
		boolean replace = false;
		KBFile v0 = file;
		KBFile v1 = (KBFile)unproxy(file.getPreviousVersion());
		while (v1!=null) {
			if (v1.equals(version)) {
				((KBFileImpl)v0).setPreviousVersion(v1.getPreviousVersion());
				//if (v0.equals(file)) {
				//	((KBFileImpl)file).setPreviousVersion(v1.getPreviousVersion());
				//}
				replace = true;
				break;
			}
			else {
				v0 = v1;
				v1 = (KBFile)unproxy(v1.getPreviousVersion());
			}
		}
		if (!replace) {
			throw new KbeeRuntimeException("no version!");
		}
		((KBFileImpl)v1).setPreviousVersion(file);
		((KBFileImpl)v1).setVersion(file.getVersion()+1);
		removeFile(file);
		addFile(v1, tag);
	}
	
	@Override
	public void addFile(KBFile file, ResourceTag tag) {
		resources.add(new KbeeContentResource(this, file, tag));
		list_resources = null;
	}
	
	@Override
	public void addResource(Resource resource) {
		resources.add(new KbeeContentResource(this, resource));
		list_resources = null;
	}
	
	@Override
	public void addResource(Resource resource, ResourceTag tag) {
		resources.add(new KbeeContentResource(this, resource, tag));
		list_resources = null;
	}
	
	public void addResource(Resource resource, ResourceFolder folder, ResourceTag tag) {
		// si no esta el folder=
		resources.add(new KbeeContentResource(this, resource, folder, tag));
		list_resources = null;
		
	}

	
	public void addResource(ContentResource resource) {
		resources.add(new KbeeContentResource(this, resource.getResource(), resource.getFolder(), resource.getTag(), resource.isIndex()));
		list_resources = null;
	}

	@Override
	public boolean contains(KBFile file) {
		for(ContentResource resource : this.resources) {
			if (resource.getResource().getId().equals(file.getId())) {
				return true;
			}
		}
		return false;
	}
	
	public void removeFile(KBFile file) {
		for(ContentResource resource : this.resources) {
			if (resource!=null && resource.getResource().getId().equals(file.getId())) {
				this.resources.remove(resource);
				list_resources = null;
				break;
			}
		}
	}
	
	public void removeResource(Resource res) {
		for(ContentResource resource : this.resources) {
			if (resource!=null && resource.getResource().getId().equals(res.getId())) {
				this.resources.remove(resource);
				list_resources = null;
				break;
			}
		}
	}
	
	@Override
	public KBFile getFirstFile() {
		if (!getFiles().isEmpty())
			return getFiles().get(0);
		return null;
	}

	@Override
	public List<KBFile> getFiles(String group_name) {
		List<KBFile> files = new ArrayList<KBFile>();
		for (ContentResource contentresource : resources) {
			if (contentresource!=null && contentresource.getResource() instanceof KBFile) {
 				ResourceTag rg = ((KBFile)contentresource.getResource()).getGroup();
				if (rg!=null && rg.getName().toLowerCase().equals(group_name.toLowerCase()))
					files.add((KBFile)contentresource.getResource());
			}
		}
		return files;
	}

	@Override
	public List<Resource> getResources(String tagname) {
		List<Resource> resources = new ArrayList<Resource>();
		for (ContentResource contentresource : this.resources) {
			if (contentresource!=null) {
 				ResourceTag tag = contentresource.getTag();
				if (tag!=null && tag.getName().toLowerCase().equals(tagname.toLowerCase()))
					resources.add(contentresource.getResource());
			}
		}
		return resources;
	}
	
	@Override
	public List<Resource> getPortalEnabledResources() {
		ArrayList<Resource> resources = new ArrayList<Resource>();
		for (ContentResource contentresource : this.resources) {
			if (contentresource!=null && contentresource.isPublicArea() && contentresource.getResource().isInPortalVersion()) {
				resources.add(contentresource.getResource());
			}	
		}
		return resources;
	}
	
	@Override
	public void setPublic(Resource resource) {
		for(ContentResource contentresource : this.resources) {
			if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
				((KbeeContentResource)contentresource).setPublic(true);
				break;
			}
		}
	}
	
	public void setTag(Resource resource, ResourceTag tag) {
		for(ContentResource contentresource : this.resources) {
			if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
				((KbeeContentResource)contentresource).setTag(tag);
				break;
			}
		}
	}
	
	public void setFolder(Resource resource, ResourceFolder folder) {
		boolean folderfound = false;
		if (folder!=null)
		for(ContentResource contentresource : this.resources) {
			if (contentresource!=null && contentresource.getResource().getId().equals(folder.getId())) {
				folderfound = true;
				break;
			}
		}
		if (folderfound || folder==null) {
			for(ContentResource contentresource : this.resources) {
				if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
					((KbeeContentResource)contentresource).setFolder(folder);
					break;
				}
			}
		}
	}

	
	@Override
	public ResourceTag getTag(Resource resource) {
		for(ContentResource contentresource : this.resources) {
			if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
				return ((KbeeContentResource)contentresource).getTag();
			}
		}
		return null;
	}
	
	@Deprecated
	public List<Resource> getResources(boolean publicarea) {
		List<Resource> resources = new ArrayList<Resource>();
		for (ContentResource contentresource : this.resources) {
			if (contentresource!=null && contentresource.isPublicArea()==publicarea)
				resources.add(contentresource.getResource());
		}
		return resources;
	}
	
	@Deprecated
	public void setResources(List<Resource> resources, boolean publicarea) {
		list_resources = null;
		List<ContentResource> otherarea = new ArrayList<ContentResource>();
		for(ContentResource contentresource : this.resources) {
			if (contentresource!=null && contentresource.isPublicArea()!=publicarea) {
				otherarea.add(contentresource);
			}
		}
		boolean found = false;
		while(!found) {
			found = true;
			for(ContentResource contentresource : this.resources) {
				if (contentresource!=null) {
					found = false;
					for(Resource resource : resources) {
						if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
							found = true;
							break;
						}
					}
					if (!found) {
						if (contentresource.isPublicArea()==publicarea) {
							this.resources.remove(contentresource);
							break;
						}
						else {
							found=true;
						}
					}
				}
			}
		}
		int order = publicarea ? -1 : otherarea.size()-1;
		for(Resource resource : resources) {
			order++;
			ContentResource contentresource = null;
			for (ContentResource existingcontentresource : this.resources) {
				if (existingcontentresource!=null && existingcontentresource.getResource().getId().equals(resource.getId())) {
					contentresource = existingcontentresource;
					break;
				}	
			}
			if (contentresource==null) {
				contentresource = new KbeeContentResource(this, resource, publicarea);
				this.resources.add(contentresource);
			}
			((KbeeContentResource)contentresource).setOrder(order);
		}
		
		if (publicarea) {
			for(ContentResource contentresource : otherarea) {
				((KbeeContentResource)contentresource).setOrder(order++);
			}
		}
		this.resources.removeIf(resource -> resource==null);
		Collections.sort(this.resources, new Comparator<ContentResource>() {
			@Override
			public int compare(ContentResource a, ContentResource b) {
				return a.getOrder() < b.getOrder() ? -1 : 1;
			}
		});
	}
	
	@Override
	@Deprecated
	public void addFile(KBFile file, boolean publicarea) {
		resources.add(new KbeeContentResource(this, file, publicarea));
		list_resources = null;
	}
	
	@Override
	@Deprecated
	public void addFile(KBFile file, ResourceTag tag, boolean publicarea) {
		resources.add(new KbeeContentResource(this, file, tag, publicarea));
		list_resources = null;
	}
	
 	@Override
	@Deprecated
	public void setPrivate(Resource resource) {
		for(ContentResource contentresource : this.resources) {
			if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
				((KbeeContentResource)contentresource).setPublic(false);
				break;
			}
		}
	}
	
	@Override
	@Deprecated
	public boolean isPublic(Resource resource) {
		for(ContentResource contentresource : this.resources) {
			if (contentresource!=null && contentresource.getResource().getId().equals(resource.getId())) {
				return ((KbeeContentResource)contentresource).isPublicArea();
			}
		}
		return false;
	}
	
    private Object unproxy(Object object) {
        try {
    		if (object instanceof HibernateProxy) {
    			HibernateProxy proxy = (HibernateProxy)object;
    			LazyInitializer initializer = proxy.getHibernateLazyInitializer();
    			object = initializer.getImplementation();
    		}
            return object;
        } 
        catch (Exception e) {
            return object;
        }
    }

}
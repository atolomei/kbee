package com.novamens.kbee.content.resource;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
 
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.content.base.Content;
import com.novamens.content.resource.KBFile;
import com.novamens.content.resource.HTMLText;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.security.User;


/**
 * 
 * THis class is no longer used
 *
 */
@Deprecated
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="resource")
@PrimaryKeyJoinColumn(name="resource_id")
@Table(name = "HTMLTEXT")
public class HTMLTextImpl extends AbstractResource implements HTMLText {

  	@Column(name = "htmltext")
  	private String text;
  	
  	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity=KbeeContent.class)
	@JoinTable(name = "contentresource",  
				joinColumns 		= @JoinColumn(name = "resource_id"), 
				inverseJoinColumns 	= @JoinColumn(name = "content_id")
			   )
	private List<Content> contents = null;

   	
  	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity=AbstractResource.class)
	@JoinTable(name = "ResourceFile",  
				joinColumns 		= @JoinColumn(name = "resource_id"), 
				inverseJoinColumns 	= @JoinColumn(name = "file_id")
			   )
  	private List<KBFile> files;
    	
  	public HTMLTextImpl() {
  		super();
  	}
  	
  	public HTMLTextImpl(String text) {
  		super();
  		setText(text);
  	}
   	
  	public void setText(String text) 	{this.text=text;}
	public String getText() 			{return text;}
	public List<KBFile> getFiles() 		{return files;}
	public void add(KBFile file) 		{files.add(file);}
	
	public void setDescription(String description)	{
	}
	
	public String getBaseName() {
		return getName();
	}
	
	@Override
	public String getPath() {
		return getName();
	}
	
	public String getUrl() {
		return null;
	}
	
	public String toString() {
		
		 StringBuilder str = new StringBuilder();
		 str.append(super.toString());

		 if (text!=null)
			 str.append("\ntext:\n"+text);
		 
		 if (files!=null) {
			 	for( KBFile file: files) {
			 		str.append("\n" + file.getName());
			 	}
		 }
		 return str.toString();
	}

	@Override
	public String getMetadataAsString() {
		return "not implemented";
	}

	@Override
	public String getGlyphIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial(String classago) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMetadataAsString(DateTimeFormatter df) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getFontAwesomeFreeIcon() {
		return  getResourceFAFreeByKey("file");
	}

	@Override
	public boolean isBinaryFile() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInPortalVersion() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setInPortalVersion(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OffsetDateTime getUploadOffsetDateTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUploadUser() {
		// TODO Auto-generated method stub
		return null;
	}


}

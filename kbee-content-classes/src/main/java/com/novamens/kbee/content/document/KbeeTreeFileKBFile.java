package com.novamens.kbee.content.document;


import java.io.IOException;
import java.time.OffsetDateTime;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Resource;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.content.resource.KBFile;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.util.FSUtils;

@Entity
@DiscriminatorValue(value=TreeFileKBFile.DISCRIMNATOR_CODE)
public class KbeeTreeFileKBFile extends KbeeTreeFile implements TreeFileKBFile {
	
	@SuppressWarnings("unused")				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeTreeFileKBFile.class.getName());
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KBFileImpl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "resource_id", nullable=true)
	private KBFile kbfile; 

	
	
	@Override
	public boolean isLeaf() {
		assert (getChildren()==null || getChildren().size()==0);
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass().equals(KbeeTreeFileKBFile.class))
			return ((Long) ((KbeeTreeFileKBFile) o).getId()).equals((Long) getId());
		return false;	
	}

	
	@Override
	public KBFile getFile() {
		return this.kbfile;
	}
	
	@Override
	public String getType() {
		return TreeFileKBFile.DISCRIMNATOR_CODE;
	}

	@Override
    public void setFile(KBFile file) {
    	this.kbfile=file;
    }

	@Override
	public boolean isDirectory() {
		return false;
	}
	
	@Override
	public String getGlyphIcon() {
		if (kbfile!=null) {
			if (kbfile.getName()!=null)
					return FSUtils.getGlyphIcon(kbfile.getName());
		}
			
		return "fad fa-file";
	}
	
	@Override
	public boolean isBinaryFile() throws IOException {
		return getFile()!=null && getFile().isBinaryFile();
	}
	
	@Override
	public String getTitle() {
	
		if (super.getTitle()!=null)
			return super.getTitle();
		
		if (getFile()!=null)
			return getFile().getTitle();
		
		if (getId()!=null)
			return this.getClass().getSimpleName()+" "+String.valueOf(getId());
		
		return this.getClass().getSimpleName();
		
	}

	@Override
	public String getName() {
		if (getFile()!=null)
			return getFile().getName();
		return super.getName();
	}
	
	@Override
	public long getSize() {
		if (getFile()!=null)
			return getFile().getSize();
		return 0;
	}	
	
	public Resource getPreviousVersion() {
		return getFile().getPreviousVersion();
	}
	
	public int getVersion() {
		return getFile().getVersion();
	}
	
	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	public String toHTMLString() {
		StringBuilder str = new StringBuilder();
		str.append("<div class=\"tree_file_kbfile\">\n"); // tf
		if (getFile()!=null) {
			str.append("<a href=\""+  getFile().getUrl() +"\"> <span class=\"" + getFile().getGlyphIcon() + "\"></span><span class=\"label\">");
				str.append(getFile().getName());
			str.append("</span>");
			str.append("<span class=\"ago\">(");
				str.append(ServiceLocator.getService(DateTimeService.class).formatFileSize(getFile().getSize(), getSessionUser().getLocale()));
			str.append(")</span></a>\n");
		}
		else {
			str.append("<span class=\"label\">");
			str.append("[null]");
			str.append("</span>\n");
		}
		str.append("</div>\n"); // tf
		
		return str.toString();
	}
	
	/**
     * Creates the root.
     *
     * @param bid the bid
     * @return the domain
     */
    public static TreeFileKBFile createRoot() {
        return new KbeeTreeFileKBFile();
    }

	@Override
	public OffsetDateTime getUploadOffsetDateTime() {
		 
		return  getFile().getUploadOffsetDateTime();
	}

	@Override
	public User getUploadUser() {
		return getFile().getUploadUser();
	}

	@Override
	public int getWidth() {
		return getFile().getWidth();
	}

	@Override
	public int getHeight() {
		return getFile().getHeight();
	}



}

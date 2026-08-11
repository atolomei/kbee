package com.novamens.kbee.content.resource;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.document.TreeFile;
import com.novamens.content.resource.TreeFileResource;
import com.novamens.kbee.content.document.KbeeTreeFile;
import com.novamens.util.KbeeRuntimeException;

@Entity
@PrimaryKeyJoinColumn(name="RESOURCE_ID")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="resource")
@Table(name = "KB_TREE_RESOURCE")
public class KbeeTreeFileResource extends AbstractResource implements TreeFileResource {
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeTreeFile.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "TREEFILE_ID", updatable=false)
	private KbeeTreeFile treefile;
	
	public KbeeTreeFileResource() {
	}
	
	public KbeeTreeFileResource(KbeeTreeFile treefile) {
		this.treefile = treefile;
		setName(treefile.getName());
		setDomain(treefile.getDomain());
		setTitle(treefile.getTitle());
		setSize(treefile.getSize());
		setLastModifiedUser(treefile.getLastModifiedUser());
		setLastModifiedOffsetDateTime(treefile.getLastModifiedOffsetDateTime());
	}
	
	public TreeFile getTreeFile() {
		return treefile;
	}
	
	@Override
	public String getMetadataAsString() {
		return getMetadataAsString(null); 
	}
	
	@Override
	public String getMetadataAsString(DateTimeFormatter df) {
		StringBuilder str = new StringBuilder(); 
		
		if (getLastModifiedUser()!=null)
			str.append(getLastModifiedUser().getFirstLastName());
		
		if (getLastModifiedOffsetDateTime()!=null) {
			if (df==null)
				str.append(". " + getLastModifiedOffsetDateTimeColloquial());
			else {
				str.append(". " + df.format(getLastModifiedOffsetDateTime()));
			}
		}

		return str.toString();
	}
	
	@Override
	public String getUrl() {
		return getTreeFile().getUrl();
	}
	
	@Override
	public String getPath() {
		return getTreeFile().getPath();
	}
	
	@Override
	public boolean isBinaryFile() throws IOException {
		return true;
	}
	
	@Override
	public String getGlyphIcon() {
		return getTreeFile().getGlyphIcon();
	}
	
	@Override
	public String getFontAwesomeFreeIcon() {
		return getResourceFAFreeByKey("link");
	}
	
	@Override
	public void setDescription(String des) {
		throw new KbeeRuntimeException("todo ");
		
	}
	
}

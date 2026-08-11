package com.novamens.content.base;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.dom.Indexable;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;


/**
 * <p>Resources are digital objects that are aggregated and managed by a {@link Content}
 * They can not be managed as autonomous objects by users of the applications.
 * <br />
 * <ul>
 * <li><b>{@link KBFile}</b> binary file stored on KBFS</li>
 * <li><b>{@link KBFileProxy}</b> binary file referenced in gateway mode. They are indexed by they are external to the server</li>
 * <li><b>{@link ExternalResource}</b> (a link or YouTubeVideo)</li>
 * <li><b>{@link TreeFile}</b>a Tree made of hierarchical directories and references to KBFile</li>
 * </ul>
 * </p>
 *
 */

public interface Resource extends com.novamens.dom.Object, Indexable, DomainObject  {
	
	public OffsetDateTime getUploadOffsetDateTime();
	public User getUploadUser();
	
	
	
	public String getName();
	public String getDescription();
	public String getTitle();
	public ObjectState getState();
	public Domain getDomain();

	/** whether it belongs to the public are or secured area */ 
	public boolean isPublicArea();
	
	/**  @return true if there is a binary file available */
	public boolean isBinaryFile() throws IOException;
	
	public ResourceTag getGroup();
	
	public void setTitle(String title);
	public void setDescription(String des);
	
	public long getSize();

	public String getUrl();
	
	/** relative to container */
	public String getPath();

	public int getVersion();
	public Resource getPreviousVersion();
	
	// get info
	public String getMetadataAsString();
	public String getMetadataAsString(DateTimeFormatter df);
	public String getGlyphIcon();
	public String getLastModifiedOffsetDateTimeColloquial();
	public String getLastModifiedOffsetDateTimeColloquial(String classago);
	public String getFontAwesomeFreeIcon();
	
	/** these fields are used by the Searcher Library. It should be replaced by the KbeePortal tools **/
	boolean isInPortalVersion();
	void setInPortalVersion(boolean b);
	Long getOId();
	
	public default AuditSet getAuditSet() {
		return AuditSet.RESOURCE;
	}
	public int getWidth();
	public int getHeight();



	
	
	
	
	
}
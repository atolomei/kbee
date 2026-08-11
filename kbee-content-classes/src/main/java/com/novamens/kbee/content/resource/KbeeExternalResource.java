package com.novamens.kbee.content.resource;

import java.io.IOException;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.novamens.content.resource.ExternalResource;
import com.novamens.security.User;

@Entity
@PrimaryKeyJoinColumn(name="RESOURCE_ID")
@Table(name = "EXTERNALRESOURCE")
public class KbeeExternalResource extends AbstractResource implements ExternalResource  {

	@Column(name = "URL")
	private String url;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@Column(name = "in_portal")
	private boolean in_portal = true;	
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setTitle(String title) {
		if (title!=null)
		super.setName(title.toLowerCase().trim());
		super.setTitle(title);
	}
	
	@Override
	public String getName()	{
		Integer value = getUrl()!=null ? getUrl().hashCode() : 0;
		return getUrl()!=null ? String.valueOf(Math.abs(value)) : super.getName(); 
	}


	@Override
	public String getDescription()	{
		return description; 
	}
	
	public void setDescription(String description)	{
		this.description=description;
	}

	public String getBaseName() {
		return getName();
	}
	
	@Override
	public String getPath() {
		return getName();
	}
	
	@Override
	public boolean isBinaryFile() throws IOException {
		return false;
	}

	@Override
	public String getGlyphIcon() {
		return "fal fa-external-link";
	}
	
	@Override
	public String getFontAwesomeFreeIcon() {
		return getResourceFAFreeByKey("link");
	}

//	@Override
//	public String getLastModifiedOffsetDateTimeColloquial() {
//		return getLastModifiedOffsetDateTimeColloquial("ago");
//	}
//	
//	@Override
//	public String getLastModifiedOffsetDateTimeColloquial(String classago) {
//		return getLastModifiedOffsetDateTimeColloquial(getLastModifiedOffsetDateTime(), classago);
//	}
//	
//	private String getLastModifiedOffsetDateTimeColloquial(OffsetDateTime date, String classago) {
//		if (date==null) 
//			return "";
//		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
//		User user = getSessionUser();
//		String zid = null;
//		if (user!=null)
//			zid=service.getMapZoneIds().get(user.getTimeZone());
//		if (zid==null)
//			zid=ZoneId.systemDefault().getId();
//		Locale locale = null;
//		if (user!=null)
//				locale=user.getLocale();
//		else
//			locale=Locale.getDefault();
//		return service.timeElapsed(date, ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
		
//	}

	@Override
	public boolean isInPortalVersion() {
		return this.in_portal;
	}
	
	@Override
	public void setInPortalVersion(boolean b) {
		this.in_portal=b;
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

package com.novamens.kbee.content.resource;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.novamens.content.base.ResourceFolder;

@Entity
@PrimaryKeyJoinColumn(name="RESOURCE_ID")
@Table(name = "KB_RESOURCE_FOLDER")
public class KbeeResourceFolder extends AbstractResource implements ResourceFolder  {

	@Column(name = "DESCRIPTION")
	private String description;
	
	public String getUrl() {
		return null;
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
	public void setTitle(String title) {
 		super.setTitle(title);
		//if (title!=null)
		//super.setName(title.toLowerCase());
	}
	
	@Override
	public boolean isBinaryFile() throws IOException {
		return false;
	}

	@Override
	public String getGlyphIcon() {
		return "fa-duotone fa-folder";
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
//		
//	}
}

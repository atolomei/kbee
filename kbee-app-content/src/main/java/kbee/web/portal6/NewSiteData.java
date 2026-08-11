package kbee.web.portal6;

import java.io.Serializable;

import org.apache.wicket.model.IDetachable;

import com.novamens.portal6.model.SiteTemplate;
import com.novamens.portal6.model.SiteType;

/**
 * Para Creacion de Sitio Externo
 * 
 */
public class NewSiteData implements IDetachable, Serializable {

	private static final long serialVersionUID = 1L;

	private boolean isExternal = false;
	
	private String title;
	private String subtitle;
	private String description;
	private SiteTemplate siteTemplate;

	private String url;
	private SiteType type;

	public String toString() {

		StringBuilder str = new StringBuilder();

		str.append(title + "| ");

		if (subtitle != null)
			str.append(subtitle + " | ");

		if (description != null)
			str.append(description + " | ");

		if (url != null)
			str.append(url + " | ");

		if (type != null)
			str.append(type.getLabel() + " | ");

		return str.toString();
	}

	public NewSiteData() {
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public SiteType getType() {
		return type;
	}

	public void setType(SiteType type) {
		this.type = type;
	}

	@Override
	public void detach() {
		// TODO Auto-generated method stub

	}

	public boolean isExternal() {
		return isExternal;
	}

	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}

	public SiteTemplate getSiteType() {
		return siteTemplate;
	}

	public void setSiteTemplate(SiteTemplate siteTemplate) {
		this.siteTemplate = siteTemplate;
	}

}

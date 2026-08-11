package com.novamens.kbee.portal.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.content.base.KbeeContent;


import com.novamens.portal.service.PortalUrlService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;

import com.novamens.kbee.url.UriHelper;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.portal6.model.ViewBKContent;
import com.novamens.portal6.model.ViewDetailContent;

@Entity
@Table(name = "PO_VIEWBKCONTENT")
@PrimaryKeyJoinColumn(name = "view_id")
@DynamicInsert
public class KbeeViewBKContent extends KbeeViewBK implements ViewBKContent {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeViewBKContent.class.getName());

	
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "content_id", nullable = true) // si borran el Content la View no se borra, queda apuntando a
														// null.
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "content")
	private Content content;

	@Column(name = "is_gallery")
	private boolean is_gallery;

	// if display resources in detail page
	@Column(name = "is_resources")
	private boolean is_resources;

	@Transient
	private String mds = null;

	@Transient
	private String summary = null;

	@Transient
	ViewDetailContent xv = null;

	public KbeeViewBKContent() {
		super();
	}

	@Override
	public KbeeViewBKContent clone() {
		KbeeViewBKContent clone = new KbeeViewBKContent();
		onClone(clone);
		return clone;
	}

	public void onClone(KbeeViewBKContent clone) {
		super.onClone((KbeeViewBK) clone);
		clone.setContent(this.getContent());
	}

	/***
	 * 
	 * This method is duplicated in class
	 * 
	 * {@link KbeeViewDetailContent}
	 * 
	 * we have to unify both into one service
	 * 
	 * 

	@Override
	public String getResponsePageAbsoluteLink() {

		if (getContent() == null)
			return null;

		try {
			Site site = getContentHomeSite();
			String siteuri;

			String base = PortalUriHelper.getInstance().getPortalURL(getContent().getDomain().getName());

			if (site != null)
				siteuri = site.getUrl();
			else
				siteuri = getHomeSite().getUrl();

			String id = getContent().getClassCode() + String.valueOf(getContent().getOId());
			String title = UriHelper.getInstance().getTitle(getContent());

			if (getContent().getContentTemplate().isVideo())
				return base + siteuri + "/" + id + "/player/" + title;
			else if (getContent().getContentTemplate().isMultimedia())
				return base + siteuri + "/" + id + "/viewer/" + title;
			else
				return base + siteuri + "/" + id + "/" + title;

		} catch (RuntimeException e) {
			logger.error(e);
			return null;
		}
	}
		 */
	

	public KbeeViewBKContent(Content content) {
		super();
		setContent(content);
		setLastModifiedUser(getSessionUser());
		setDomain(content.getDomain());
		setResources(true);
		if (content != null && (content.getContentTemplate().isImage() || content.getContentTemplate().isVideo()
				|| content.getContentTemplate().isAudio()))
			this.setOpenNewTab(true);
	}

	@Override
	public Object getObject() {
		return content;
	}

	@Override
	public void setContent(Content content) {
		this.content = content;
	}

	@Override
	public Content getContent() {
		return content;
	}

	@Override
	public String getViewType() {
		return KbeeViewBK.CONTENT_TYPE;
	}

	/**
	 * 
	 */
	// @Override
	// public WebPage getResponsePage() {
	//	 return getResponsePage(false);
	// }

	/*
	private WebPage getPageFromResource() {

		if (!(getContent() instanceof ResourceContainer))
			return null;

		List<com.novamens.content.base.Resource> list = ((ResourceContainer) (getContent())).getResources();

		Resource res = null;
		WebPage page = null;

		for (Resource xr : list) {
			if (xr instanceof ExternalResource) {
				res = xr;
				break;
			} else if (xr instanceof KBFile) {
				try {
					File fi = ((KBFile) xr).getFile();
					if (fi != null) {
						String ext = FilenameUtils.getExtension(fi.getName());
						if (ext != null && !ext.toLowerCase().equals("txt")) {
							res = xr;
							break;
						}
					}

				} catch (IOException e) {
					logger.error(e);
				}
			}
		}

		return page;
	}
	*/

	
	// TODO VER AT
	// getExternalResourceResponsePage()
	// getResponsePage( 
	
	
	/**
	 * 
	 * @return
	 
	@Override
	public WebPage getExternalResourceResponsePage() {

		if (getContent() == null)
			return null;

		if (!getContent().isHeadVersion())
			updateContent();

		if (!(getContent() instanceof ResourceContainer))
			return null;

		List<com.novamens.content.base.Resource> list = ((ResourceContainer) (getContent())).getResources();

		Resource res = null;
		WebPage page = null;

		for (Resource xr : list) {
			if (xr instanceof ExternalResource) {
				res = xr;
				break;
			}
		}

		if (res != null)
			page = new RedirectPage(ResourceUriHelper.getInstance().getHref(res));

		return page;
	}
*/
	
	/**
	 * 
	 
	@Override
	public WebPage getResponsePage(boolean from_resource) {

		if (getContent() == null)
			return null;

		if (!getContent().isHeadVersion())
			updateContent();

		if (from_resource)
			return getPageFromResource();

		ViewDetailContent view = getViewDetailContent();

		if (view == null) {
			PortalDiagrammableSiteService service = this.getBlock().getSite().getService(PortalDiagrammableSiteService.class);
			try {
				try {
					view = service.addViewDetailContent(getContent());
				} catch (ContentCreationException e) {
					logger.error(e);
					view = null;
					return null;
				}
			} catch (ContentMgmtException e) {
				logger.error(e);
				view = null;
				return null;
			}
		}

		// esto es para redirigir a enlance del recurso. TODO: Revisar
		//
		if (getContent().getDataSetMember(CONTENT_TYPE_CLASSIFIER) != null
				&& getContent().getDataSetMember(CONTENT_TYPE_CLASSIFIER).getStrValue() != null
				&& getContent().getDataSetMember(CONTENT_TYPE_CLASSIFIER).getStrValue().startsWith(VINCULO_INSTANCE)
				&& (getContent() instanceof ResourceContainer)
				&& (((ResourceContainer) getContent()).getResources().size() > 0)) {

			Resource file = ((ResourceContainer) getContent()).getResources().get(0);

			if (file != null)
				return new RedirectPage(file.getUrl());
			else
				return null;
		}

		// --------------------------------------------------
		// Audio o Video
		//
		else if (getContent().getContentTemplate().isVideo() || getContent().getContentTemplate().isAudio()) {

			KBPDetailVideoPage<ViewDetailContent> page;

			if (getContent() instanceof ResourceContainer) {
				List<KBFile> list = ((ResourceContainer) getContent()).getFiles();

				if (list.size() > 0) {

					PageParameters param = new PageParameters();

					Site site = getContentHomeSite();
					if (site == null)
						site = ServiceLocator.getService(PortalDirectoryService.class).getHomeSite(getDomain());

					param.set("site", site.getURI());
					param.set("id", getContent().getClassCode() + String.valueOf(getContent().getOId()));
					param.set("title", UriHelper.getInstance().getTitle(getContent()));

					IModel<Block> model_block = new ObjectModel<Block>((DiagrammableBlock) this.getBlock());

					ViewDetailContent xview = getPortalDao().findViewDetailByContent(getContent());
					IModel<ViewDetailContent> modex = new ObjectModel<ViewDetailContent>(xview);

					page = new KBPDetailVideoPage<ViewDetailContent>(modex, model_block);

				} else {

					logger.error("no video in ResourceContainer.");
					return new KBPErrorPage(new Model<String>("Error in Content"),
							new Model<String>("Content does not contains a Video."));
				}
			} else {
				logger.error("Content must be ResourceContainer.");
				return new KBPErrorPage(new Model<String>("Type Error"),
						new Model<String>("Content must be ResourceContainer"));
			}
			return page;
		}

		// Photo -------------------------------------
		//
		else if (getContent().getContentTemplate().isMultimedia() || getContent().getContentTemplate().isImage()) {
			ViewDetailContent xview = getPortalDao().findViewDetailByContent(getContent());
			KBPDetailVideoPage<ViewDetailContent> page = new KBPDetailVideoPage<ViewDetailContent>(
					new ObjectModel<ViewDetailContent>(xview),
					new ObjectModel<Block>((DiagrammableBlock) KbeeViewBKContent.this.getBlock()));
			return page;
		}

		// Textos -------------------------------------
		//
		else {
			IModel<Block> model_block = new ObjectModel<Block>( (DiagrammableBlock) this.getBlock());
			ViewDetailContent dview = getPortalDao().findViewDetailByContent(getContent());
			if (dview != null)
				return new KBPDetailTextPage<ViewDetailContent>(new ObjectModel<ViewDetailContent>(dview), model_block,
						this.isGalleryViewer(), this.isResources());
			else {
				logger.error("View is null for content " + content.getTitle());
				return null;
			}
		}
	}
*/
	
	@Override
	public String getTitle() {
		if (super.getTitle() != null)
			return super.getTitle();
		return getContent() != null ? getContent().getTitle() : "n/a";
	}

	 
	@Override
	public String getMetadataAsString() {

		if (this.mds != null)
			return this.mds;

		if (getContent() == null)
			return getViewType();

		StringBuilder str = new StringBuilder();

		str.append(getSummary());

		if (str.length() > 0)
			str.append(". ");

		str.append(getLastModifiedUser() != null ? getLastModifiedUser().getFirstLastName() + ". " : "");
		str.append(getLastModifiedOffsetDateTimeColloquial());

		this.mds = str.toString();

		return this.mds;

	}

	@Override
	public String getDescription() {
		if (super.getDescription() != null)
			return super.getDescription();
		if (getContent() != null && getContent().getMetadataAsString() != null)
			return getContent().getMetadataAsString();
		return null;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append("\n" + getViewType());
		str.append("\nContent: "
				+ ((getContent() != null && getContent().getTitle() != null) ? getContent().getTitle() : ""));
		return str.toString();
	}

	@Override
	public List<KBFile> getFiles(String group_name) {
		if (getContent() == null)
			return null;
		if (getContent() instanceof ResourceContainer)
			return ((ResourceContainer) getContent()).getFiles(group_name);
		return new ArrayList<KBFile>();
	}

	/**
	 * Para la View de un Content -> si la View tiene Files devuelve esos, sino
	 * devuelve los del Content
	 */
	@Override
	public List<KBFile> getFiles() {
		if (super.getFiles().size() > 0)
			return super.getFiles();
		return getFiles(null);
	}

	/**
	@Override
	public String getContentTypeAsString() {
		if (getContent() != null)
			return getContent().getContentTypeClassificationAsString();
		return super.getContentTypeAsString();
	}
	**/

	public void updateContent() {
		try {
			Content con = getContentDao().findContentByOId(getContent().getOId());
			if (con != null) {
				setContent(con);
				 SiteService service = this.getBlock().getSite().getService(SiteService.class);
				try {
					service.save();
				} catch (ContentMgmtException e) {
					logger.error(e);
				}
				// ----------
				// Notificar
				//
			}
		} catch (Exception e) {
			logger.error(e);
		}

	}

	@Override
	public boolean isResources() {
		return this.is_resources;
	}

	@Override
	public boolean isGalleryViewer() {
		return this.is_gallery;
	}

	@Override
	public void setResources(boolean b) {
		this.is_resources = b;
	}

	@Override
	public void setGalleryViewer(boolean b) {
		this.is_gallery = b;
	}

/**	private Site getContentHomeSite() {
		if (getContent() == null)
			return null;
		try {
			return ServiceLocator.getService(PortalUrlService.class).getContentHomeSite(getContent());
		} catch (Exception e) {
			logger.error(e);
			return null;
		}

	}
	*/

	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	private Classification getClassification(Classifier classifer) {

		if (getContent() == null)
			return null;

		for (Classification classification : getContent().getClassification()) {
			if (classification.getClassifier().equals(classifer)) {
				return classification;
			}
		}
		return null;
	}

	private List<Classifier> getCanonicalClassifiers() {

		if (getContent() == null)
			return new ArrayList<Classifier>();

		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (ClassifierTemplate template : getContent().getContentTemplate().getClassifiers()) {
			if (template.isMetadataSubtitle()) {
				classifiers.add(template.getClassifier());
			}

		}
		return classifiers;
	}

	/**
	 * 
	 * @return
	 */
	private String getSummary() {

		if (this.summary != null)
			return this.summary;

		int index = 0;
		StringBuffer sy = new StringBuffer();
		for (Classifier classifer : getCanonicalClassifiers()) {
			Classification classification = getClassification(classifer);
			if (classification != null && classification.getDataSetMember() != null) {
				if (index > 0)
					sy.append(" · ");
				sy.append(classification.getDataSetMember().getDisplayName());
				index++;
			}
		}

		this.summary = sy.toString();
		return this.summary;

	}

	public ViewDetailContent getViewDetailContent() {

		if (xv != null)
			return xv;

		//xv = getPortalDao().findViewDetailByContent(getContent());
		//return xv;
		return null;

	}

	

	

}

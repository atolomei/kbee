package kbee.web.searcher.panel;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.model.Classification;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.FSUtils;
import kbee.util.logging.Logger;
import kbee.web.page.InvisibleImage;
import kbee.web.resource.WebThumbnailReference;

/**
 * ViewDetailContent
 * 
 * Subtitle. Metadata Abstract
 * 
 * isVideo isAudio isPhotos isDocument isNew
 * 
 * publication date published by
 * 
 * [classifiers, attributes] Sitio de pertenencia
 */
		

@SuppressWarnings("serial")
public class SearcherContentViewPanel<T extends Content> extends KBPanel {
	private static final long serialVersionUID = 1L;

	private static Logger logger = kbee.util.logging.Logger.getLogger(SearcherContentViewPanel.class.getName());
	
	private boolean isImage = false;
	private boolean onlyTitle = false;

	private IModel<T> model;
	private T version = null;
	private String textquery;
	private List<String> list;
	private IModel<Site> siteModel;
	private Searcher searcher;
	private int index;
	private boolean expanded = false;
	private String target = "_blank";

	private boolean isValid;
	private String context;
	//private String user_lists = null;
	String subtitle = null;


	/**
	 * 
	 * @param id
	 * @param model
	 * @param site_model
	 * @param searcher
	 * @param textquery
	 * @param index
	 * @param isExpandedMode
	 */
	public SearcherContentViewPanel(String id, IModel<T> model, IModel<Site> site_model, Searcher searcher, String textquery, int index, boolean isExpandedMode) {
		super(id);
		this.model = model;
		this.searcher = searcher;
		this.index = index;
		this.expanded = isExpandedMode;
		this.textquery = textquery;
		this.siteModel = site_model;
		this.isValid = false;
		this.isValid = site_model.getObject().isDisplayValidVersion();
	}

	public IModel<T> getModel() {
		return model;
	}
	
	@SuppressWarnings("unchecked")
	public T getModelObject() {
		if (isValid) {
			if (version==null) {
				version = (T)model.getObject().getService(ContentService.class).getValidVersion();
			}
			return version;
		}
		else {
			return model.getObject();
		}
	}
	
	public boolean isDisplayValidVersion() {
		return this.isValid;
	}
	
	public boolean isDisplayHeadVersion() {
		return !this.isValid;
	}
	
	public IModel<Site> getSiteModel() {
		return siteModel;
	}

	public void setSiteModel(IModel<Site> siteModel) {
		this.siteModel = siteModel;
	}
	
	public int getIndex() {
		return index;
	}
	
	public Searcher getSearcher() {
		return searcher;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
	public String getContext() {
		return this.context;
	}
	
	public boolean isOnlyTitle() {
		return onlyTitle;
	}

	public void setOnlyTitle(boolean onlyTitle) {
		this.onlyTitle = onlyTitle;
	}
	
	public void setTarget(String t) {
		this.target=t;
	}
	
	public List<String> getSnippets() {
		return list;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		try {
			
			String displayMode = getUserPreference("displaymode");
			
			this.context = getServerUrl(); 
			
			WebMarkupContainer ic = new WebMarkupContainer("image-container") {
				public boolean isVisible() {
					return isImage();
				}
			};
			WebMarkupContainer link = new WebMarkupContainer("image-link");
			ic.add(link);
			if (this.getModelObject().getState().getId() != ObjectState.ENABLED.getId())
				link.add(new AttributeModifier("href", "/deleted"));
			else {
				String url = getUrl(getModelObject());
				link.add(new AttributeModifier("href", url));
			}
			if (getTarget()!=null)
				link.add(new AttributeModifier("target", getTarget()));
			add(ic);
			WebMarkupContainer gi = new WebMarkupContainer("glyphicon");
			gi.add(new AttributeModifier("class", this.model.getObject().getContentTemplate().getGlyphIcon() + "  centered-in-container"));
			link.add(gi);
			addImage(link, gi);

			String description = getModelObject().getAbstract()!=null ? getModelObject().getAbstract().asString() : null;
			WebMarkupContainer descriptionContainer = new WebMarkupContainer("description-container") {
				public boolean isVisible() {
					return displayMode!=null && displayMode.startsWith("comfortable") && description!=null;
				}
			};
			descriptionContainer.add((new Label("description", description)).setEscapeModelStrings(false));
			add(descriptionContainer);
			
			WebMarkupContainer expandedcontainer = new WebMarkupContainer("expanded-container") {
				@Override
				public boolean isVisible() {
					return expanded;
				}
			};
			expandedcontainer.add(getExpandedPanel());
			add(expandedcontainer);
		} 
		catch (Exception e) {
			logger.error(e);
			add(new InvisiblePanel("image-container"));
			add(new InvisiblePanel("description-container"));
			add(new InvisiblePanel("expanded-container"));
		}
	}
	
	
	public String getSubtitle() {
		
		if (subtitle!=null)
			return this.subtitle;
		
		if (getModel().getObject().getContentTemplate().getPortalsSubtitleRule()!=null && !"".equals(getModel().getObject().getContentTemplate().getPortalsSubtitleRule())) {
			try {
				KbeeTextTemplate template = new KbeeTextTemplate(getModel().getObject().getContentTemplate().getPortalsSubtitleRule());
				subtitle = template.process(getModel().getObject());
				return subtitle;
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		else {
			subtitle = getContentType();
		}
		return subtitle;
	}
	
	protected Panel getExpandedPanel() {
		IModel<T> model = new ObjectModel<T>(getModelObject());
		return new SearcherContentExpandedViewPanel<T>(model, textquery, getSiteModel());
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model != null)
			model.detach();
		version = null;
		if (this.siteModel!=null)
			this.siteModel.detach();
	}

	protected String getTarget() {
		return target;
	}
	
	protected boolean isImageResource(KBFile mod) {
		try {
			return FSUtils.isImage(mod.getFileName());
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	protected boolean isImage() {
		return this.isImage;
	}

	private String getUrl(T object) {
		if (object.getContentTemplate().isExternalReference()) {
			if (object instanceof ResourceContainer) {
				List<Resource> list = ((ResourceContainer) object).getResources();
				for(com.novamens.content.base.Resource res: list) 
					if (res instanceof ExternalResource)
						if ( ((ExternalResource) res).getUrl()!=null)
									return ((ExternalResource) res).getUrl();
			}
			return "#";
		}
		
		String url= ServiceLocator.getService(PortalUrlService.class).getRelativeDetailUrl(object, getSiteModel().getObject());
		return this.context + "/" + url;
	}


	private void addImage(WebMarkupContainer link, WebMarkupContainer gi) {
		
		if (this.getModelObject().getContentTemplate().isDocument() || 
			this.getModelObject().getContentTemplate().isAudio()	 || 
			this.getModelObject().getContentTemplate().isTool()) {
			
			link.add(new InvisibleImage("image"));
			return;
		}

		try {
			// Galeria, Video,
			//
			
			boolean isvideo = this.model.getObject().getContentTemplate().isVideo();
			Image im = null;
			if (this.model.getObject() instanceof ResourceContainer) {
				List<Resource> xfiles = ((ResourceContainer) this.model.getObject()).getResources();
				KBFile file = null;
				for (Resource fe : xfiles) {
					if (fe instanceof KBFile) {
						if (isImageResource((KBFile) fe)) {
							file = (KBFile) fe;
							break;
						}
					}
				}
				if (file != null) {
					org.apache.wicket.request.resource.ResourceReference imagereference;
					imagereference = new WebThumbnailReference(file, this.model.getObject(), ThumbnailSize.MINI);
					im = new Image("image", imagereference) {
						private static final long serialVersionUID = 1L;
						protected boolean shouldAddAntiCacheParameter() {
							return false;
						}
					};
				}
			}

			if (im != null) {
				gi.setVisible(isvideo);
				link.add(im);
			} 
			else {
				link.add(new InvisibleImage("image"));
 				gi.setVisible(true);
			}

			if (isvideo) {

				gi.add(new AttributeModifier("class", "fas fa-play centered-in-container"));
				gi.add(new AttributeModifier("style", "    font-size: 14px;" + "    color: white;" + "    z-index: 10;"
						+ "    float: left;" + "    background: initial;"));
			}

		} 
		catch (Exception e) {
			logger.error(e);
			link.addOrReplace(new InvisibleImage("image"));
			gi.setVisible(true);
		}
	}
	
	private String getUserPreference(String key) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user != null) {
			return user.getService(PreferencesService.class).getValue(getContext(), key);
		}	
		return null;
	}
	
	private String getContentType() {
		StringBuilder str = new StringBuilder();
		for (Classification clasi : getModel().getObject().getClassification()) {
			if (clasi.getClassifier().isContentType()) {
				if (str.length() > 0)
					str.append(", ");
				str.append(clasi.getStrValue());
			}
		}
		return str.toString();
	}
	
	private User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} 
		catch (Exception e) {
			return null;
		}
	}
}

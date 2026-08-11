package com.novamens.kbee.portal.model;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;



import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.novamens.content.base.Content;
import com.novamens.content.base.DisplayMode;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.resource.KBFile;
import com.novamens.content.text.Text;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.portal6.model.BodyTemplateType;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.TitleMode;
import com.novamens.portal6.model.ViewDetailContent;

//import com.novamens.portal.service.PortalDirectoryService;


import com.novamens.security.User;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.FSUtils;


/**
 * 
 * Vista de un contenido publicado en su sitio de pertenencia Un contenido puede
 * tener solo 1 Sitio de pertenencia.
 * 
 * Las Relaciones de publicación son parte de la Vista
 * 
 * Sitio Sección
 * 
 * Tipo de Contenido o Tipo de Vista ? Fecha de Publicación Publicado por
 * 
 * Otras facetas del contenido ?
 *
 * update contentclass set javaclass ='com.novamens.kbee.portal.model.KbeeViewDetailContent' where javaclass='com.novamens.kbee.portal.model.publish.KbeeViewDetailContent'
 * 
 * 
 */
@Entity
@PrimaryKeyJoinColumn(name = "po_id")
@Table(name = "PO_VIEWCONTENT")
public class KbeeViewDetailContent extends KbeePortalObject implements ViewDetailContent {

	//static final PackageResourceReference PLAYER = new PackageResourceReference(AbstractKbeePortalPage.class,	"player-small.png");
	//static final PackageResourceReference NO_IMAGE = new PackageResourceReference(AbstractKbeePortalPage.class,	"player-small.png");

	
	@Override
	public String getClassKey() {
		return "viewcontent";
	}

	public List<Classifier> getClassifiers() {
		if (getContent()!=null)
			return getContent().getClassifiers();
		return new ArrayList<Classifier>();
		
		
	}
	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeViewDetailContent.class.getName());
	
	
	
	@OneToOne(fetch = FetchType.EAGER, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "content_id")
	private Content content;

	// This is just an optimization to make some checks fast
	// basically when we need to find out if a Content has a View -for any of its
	// versions-
	@Column(name = "content_oid")
	private Long content_oid;
	

	// Related contents
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = KbeeContent.class)
	@JoinTable(name = "PO_VIEWCONTENTRELATION", joinColumns = {
			@JoinColumn(name = "VIEW_ID", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "TARGET_ID", nullable = false, updatable = false) })
	@OrderColumn(name = "POSITION")
	List<Content> related = new ArrayList<Content>();

	@Column(name = "isSearchable")
	private boolean isSearchable;

	//
	// TitleMode[ Basic, PhotoOntheRight ]
	//
	@Column(name = "TitleMode")
	private TitleMode titleMode = TitleMode.SIMPLE;

	@Column(name = "isAbstract")
	private boolean isAbstract;

	@Column(name = "isMetadata")
	private boolean isMetadata;

	@Column(name = "isViewer")
	private boolean isViewer;

	// --------------------------------------------------------------------------
	//
	// BodyTemplate FAQ | Text | Tool | Document | Activity | Audio/Video
	//
	@Column(name = "bodyTemplate")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.kbee.portal.model.BodyTemplateTypeUserType")
	private BodyTemplateType bodyTemplateType = BodyTemplateType.TEXT;

	// --------------------------------------------------------------------------
	//
	// DetailGeneralTemplate: [ Video ][ Text ][ Activity ]
	// @Column(name = "bodyTemplateType")
	// private int detailGeneralTemplate;

	@Column(name = "isResources")
	private boolean isResources;

	@Column(name = "resourcesmode")
	@Enumerated(EnumType.ORDINAL)
	@Type(type = "com.novamens.content.base.DisplayModeUserType")
	private DisplayMode resourcesmode;

	@Column(name = "resourcesIds")
	private String resourcesIds;

	@Transient
	private Map<String, String> map;

	@Transient
	private Map<Long, Boolean> enabled;

	@Transient
	private List<Resource> list = null;

	@Transient
	List<Classifier> classifiers = null;

	public KbeeViewDetailContent() {
	}

	public KbeeViewDetailContent(Content content) {
		setContent(content);
	}

	@Override
	public String getTitle() {
		if (super.getTitle() != null)
			return super.getTitle();
		if (getContent() != null)
			return getContent().getTitle();
		return null;
	}

	@Transient
	private String subtitle = null;

	/**
	 * 
	 * 
	 * 
	 */
	@Override
	public String getSubtitle() {

		if (subtitle != null)
			return subtitle;

		int index = 0;

		StringBuffer summary = new StringBuffer();

		for (Classifier classifier : getCanonicalClassifiers()) {
			for (Classification classification : getClassification(classifier)) {
				if (classification != null && classification.getDataSetMember() != null) {
					if (index > 0)
						summary.append(" · ");
					if (classifier.getDataSetType().equals(DataSetType.DATE)) {
						if (classification.getDataSetMember().getDateValue() != null) {
							// TODO Fix multi language
							//
							DateTimeFormatter dt = DateTimeFormatter.ofPattern("MM/dd/yy");
							String label = dt.format(classification.getDataSetMember().getDateValue());
							summary.append(label);
						}
					} else {
						summary.append(classification.getDataSetMember().getDisplayName());
					}
					index++;
				}
			}
		}
		for (AttributeTemplate template : getCanonicalAttributes()) {
			List<String> values = getAttributeValues(template.getAttribute());
			if (!values.isEmpty()) {
				if (index > 0)
					summary.append(" · ");
				summary.append(values.get(0));
				index++;
			}
		}

		subtitle = summary.toString();
		return subtitle;
	}

	@Override
	public Text getText() {
		if (getContent() != null && getContent() instanceof OrganizationalText)
			return ((OrganizationalText) getContent()).getText();
		return null;
	}

	@Override
	public Text getAbstract() {
		if (getContent() != null)
			return getContent().getAbstract();
		return null;
	}

	@Override
	public void setContent(Content content) {
		this.content = content;
		this.content_oid = Long.valueOf(content.getOId());
		if (content != null && !content.getContentTemplate().isAd())
			this.isSearchable = true;
		else
			this.isSearchable = false;
	}

	@Override
	public Content getContent() {
		return content;
	}

	@Override
	public void addRelated(Content content) {
		related.add(content);
	}

	@Override
	public void setRelated(List<Content> related) {
		this.related.clear();
		for (Content content : related)
			this.related.add(content);
	}

	@Override
	public List<Content> getRelated() {
		return related;
	}

	@Override
	public void setResources(boolean b) {
		this.isResources = b;
	}

	@Override
	public boolean isResources() {
		return this.isResources;
	}

	@Override
	public KbeeViewDetailContent clone() {
		throw new KbeeRuntimeException("Sorry. not implemented.");
	}

	@Override
	public String getMetadataAsString() {
		return getContent().getMetadataAsString();
	}

	
	@Override
	public TitleMode getTitleMode() {
		return this.titleMode;
	}

	@Override
	public boolean isViewer() {
		return this.isViewer;
	}

	@Override
	public boolean isAbstract() {
		return this.isAbstract;
	}

	@Override
	public void setTitleMode(TitleMode tm) {
		this.titleMode = tm;
	}

	@Override
	public void setViewer(boolean v) {
		this.isViewer = v;
	}

	@Override
	public void setAbstract(boolean a) {
		this.isAbstract = a;

	}

	/**
	 * Map<id del recurso,boolean true si esta publicado, falso sino>
	 * 
	 * @return
	 */
	@Override
	public Map<Long, Boolean> getMapEnabled() {

		if (this.enabled == null) {

			this.enabled = new HashMap<Long, Boolean>();

			if (getResources() != null) {
				if (this.resourcesIds != null) {
					Map<String, String> aux = new HashMap<String, String>();
					String arr[] = this.resourcesIds.split("#");
					for (String str : arr)
						aux.put(str, str);
					for (Resource res : getResources()) {
						if (aux.containsKey(res.getId().toString()))
							this.enabled.put((Long) res.getId(), Boolean.valueOf(true));
						else
							this.enabled.put((Long) res.getId(), Boolean.valueOf(false));
					}
				} else {
					for (Resource res : getResources()) {
						this.enabled.put((Long) res.getId(), Boolean.valueOf(false));
					}
				}
			}
		}

		return enabled;

	}

	@Override
	public BodyTemplateType getBodyTemplateType() {
		return bodyTemplateType;
	}

	@Override
	public void setBodyTemplateType(BodyTemplateType bodyTemplateType) {
		this.bodyTemplateType = bodyTemplateType;
	}

	@Override
	public List<Resource> getResources() {
		if (getContent() instanceof ResourceContainer) {
			return ((ResourceContainer) getContent()).getResources();
		}
		return null;
	}

	@Override
	public void setDisabled(Resource object) {
		getMapEnabled().put((Long) object.getId(), Boolean.valueOf(false));
		updateIDs();
	}

	@Override
	public void setEnabled(Resource object) {
		getMapEnabled().put((Long) object.getId(), Boolean.valueOf(true));
		updateIDs();
	}

	public void setResourcesID(String res) {
		this.resourcesIds = res;
	}

	@Override
	public String getResourcesID() {
		return this.resourcesIds;
	}

	@Override
	public List<Resource> getEnabledResources() {
		if (list == null) {
			synchronized (this) {
				list = new ArrayList<Resource>();
				for (Resource res : getResources()) {
					if (getMapEnabled().containsKey((Long) res.getId())) {
						if (getMapEnabled().get((Long) res.getId()).booleanValue())
							list.add(res);
					}
				}
			}
		}
		return list;
	}

	@Override
	public void setDisplayMode(DisplayMode tm) {
		this.resourcesmode = tm;
	}

	@Override
	public DisplayMode getDisplayMode() {
		return this.resourcesmode;
	}

	public void setEnabledAll() {
		if (getContent() != null) {
			if (getContent() instanceof ResourceContainer) {
				List<Resource> lr = ((ResourceContainer) getContent()).getResources();
				if (lr != null) {
					for (Resource res : lr) {
						getMapEnabled().put((Long) res.getId(), Boolean.valueOf(true));
					}
					updateIDs();
				}
			}
		}
	}

	public List<Classification> getClassification() {
		if (getContent() != null) {
			return getContent().getClassification();
		} else {
			return new ArrayList<Classification>();
		}
	}


	@Override
	public Map<String, List<String>> getAttributesAsMap() {
		try {
			return ((Classificable)getContent()).getAttributesAsMap();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	
	
	public List<Classification> getClassification(Classifier classifier) {
		if (getContent() != null && getContent() instanceof KbeeContent) {
			return ((KbeeContent) getContent()).getClassification(classifier);
		} else {
			return new ArrayList<Classification>();
		}
	}

	public void setClassification(Classifier classifier, DataSetMember member) {
	}
	
	public void setClassification(Classifier classifier, List<DataSetMember> members) {
	}

	public void setAttributeValues(Attribute name, List<String> values) {
	}

	public List<String> getAttributeValues(Attribute name) {
		if (getContent() != null && getContent() instanceof KbeeContent) {
			return ((KbeeContent) getContent()).getAttributeValues(name);
		} else {
			return new ArrayList<String>();
		}
	}

	public void setEnabledDocs() {
		if (getContent() != null) {
			if (getContent() instanceof ResourceContainer) {
				List<Resource> lr = ((ResourceContainer) getContent()).getResources();
				if (lr != null) {
					for (Resource res : lr) {

						if (res instanceof KBFile)

							try {
								if (!(((KBFile) res).isImage() || ((KBFile) res).isVideo() || ((KBFile) res).isAudio()))

									getMapEnabled().put((Long) res.getId(), Boolean.valueOf(true));

								else
									getMapEnabled().put((Long) res.getId(), Boolean.valueOf(false));
							} catch (Exception e) {
								logger.error(e);
							}
					}
					updateIDs();
				}
			}
		}
	}

	@Override
	public User getPublisher() {
		return getLastModifiedUser();
	}

	@Override
	public OffsetDateTime getPublicationDate() {
		return getLastModifiedOffsetDateTime();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(content.getTitle() + " | ");
		return str.toString();
	}

	@Override
	public Map<String, String> getGeneralInfo() {
		if (this.map != null)
			return this.map;

		this.map = new HashMap<String, String>();

		Locale locale = getSessionUser() != null ? getSessionUser().getLocale() : Locale.getDefault();
		ResourceBundle res = ResourceBundle.getBundle(KbeeViewDetailContent.this.getClass().getName(), locale);

		this.map.put(res.getString("title-mode"),
				this.getTitleMode() != null ? this.getTitleMode().getLabel(locale) : "");
		this.map.put(res.getString("abstract"), this.isAbstract() ? res.getString("yes") : res.getString("no"));
		this.map.put(res.getString("resources"), this.isResources() ? res.getString("yes") : res.getString("no"));
		this.map.put(res.getString("resources-templates"), this.getDisplayMode().getLabel(locale));
		this.map.put(res.getString("viewer"), this.isViewer() ? res.getString("yes") : res.getString("no"));
		this.map.put(res.getString("oid"), getOId() != null ? getOId().toString() : "-");
		this.map.put(res.getString("id"), getId() != null ? getId().toString() : "-");
		this.map.put(res.getString("status"), getState() != null ? getState().getLabel(locale) : "-");
		this.map.put(res.getString("modified"), getLastModifiedOffsetDateTimeColloquial());
		this.map.put(res.getString("user"), getLastModifiedUser().getFirstLastName());
		this.map.put(res.getString("bodytemplatetype"), this.getBodyTemplateType().getLabel(locale));

		if (getContent() != null) {
			this.map.put(res.getString("content-title"), getContent().getTitle());
			this.map.put(res.getString("content-oid"),
					getContent().getOId().toString() + " / " + String.valueOf(getContent().getVersion()));
		}
		return this.map;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	protected List<Classifier> getCanonicalClassifiers() {

		if (classifiers != null)
			return classifiers;

		classifiers = new ArrayList<Classifier>();

		if (getContent() != null) {
			for (ClassifierTemplate template : getContent().getContentTemplate().getClassifiers()) {

				if (template.isMetadataSubtitle() || template.getClassifier().isContentType()) {
					classifiers.add(template.getClassifier());
				}
			}
		}
		return classifiers;
	}

	@Transient
	List<AttributeTemplate> attributes = null;

	protected List<AttributeTemplate> getCanonicalAttributes() {

		if (attributes != null)
			return attributes;

		attributes = new ArrayList<AttributeTemplate>();

		if (getContent() != null) {
			for (AttributeTemplate template : getContent().getContentTemplate().getAttributes()) {
				if (template.isMetadataSubtitle()) {
					attributes.add(template);
				}
			}
		}
		return attributes;
	}

	private void updateIDs() {
		StringBuilder str = new StringBuilder();
		synchronized (this) {
			for (Entry<Long, Boolean> entry : getMapEnabled().entrySet()) {
				if (entry.getValue().booleanValue()) {
					if (str.length() > 0)
						str.append("#");
					str.append(entry.getKey().toString());
				}
			}
			this.resourcesIds = str.toString();
		}
	}

	/**
	 * Home Site for the Content
	 *
	@Override
	public Site getContentHomeSite() {

		try {
			Site site = ServiceLocator.getService(PortalDirectoryService.class).getContentHomeSite(getContent());
			if (site != null)
				return site;
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			return getGlobalHomeSite();
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
 * 
 */
	/**
	 * 
	 * 
	 * Intranet Home Site

	protected Site getGlobalHomeSite() {
		PortalDirectoryService service = ServiceLocator.getService(PortalDirectoryService.class);
		Site site = null;
		try {
			site = service.getGlobalHomeSite(getDomain());
			if (site == null)
				logger.error("Home Site is null. Please create HomeSite");
			return site;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	 */
	/**
	 * 
	 * http://localhost:8087/portal/home/tx2420/manejarse_a_mismo_relaciones_trabajo
	 * 
	 * 
	 * 

	@Override
	public String getUrl() {
		String site = getContentHomeSite().getURI();
		String id = getContent() != null ? (getContent().getClassCode() + String.valueOf(getContent().getOId())) : "";
		String title = getContent() != null ? (UriHelper.getInstance().getTitle(getContent())) : "";

		if (getContent() == null)
			return null;
		try {

			String base = PortalUriHelper.getInstance().getPortalURL(content.getDomain().getName());

			String ret;
			if (getContent().getContentTemplate().isVideo())			ret = base + site + "/" + id + "/player/" + title;
			else if (getContent().getContentTemplate().isAudio())		ret = base + site + "/" + id + "/player/" + title;
			else if (getContent().getContentTemplate().isMultimedia())	ret = base + site + "/" + id + "/player/" + title;
			else if (getContent().getContentTemplate().isImage())		ret = base + site + "/" + id + "/player/" + title;
			else
				ret = base + site + "/" + id + "/" + title;

			logger.debug("url: " + ret);

			return ret;

		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	*/
	

	public static BodyTemplateType getDefaultBodyTemplate(ContentTemplate clazz) {
		if (clazz.isAudio()) 			return BodyTemplateType.VIDEO;
		if (clazz.isImage())			return BodyTemplateType.VIDEO;
		if (clazz.isVideo())			return BodyTemplateType.VIDEO;
		if (clazz.isText())				return BodyTemplateType.TEXT;
		return BodyTemplateType.TEXT;
	}

	@Override
	public void addClassification(Classification clasi) {
		logger.warn("addClassification(Classification clasi) does nothing");
	}

	public void addClassification(Classifier c, DataSetMember dm) {
		logger.warn("addClassification(Classifier c, DataSetMember dm) does nothing");
	}
	
	@Override
	public void removeAllClassification(Classifier classifier) {
		logger.warn("removeAllClassification(Classifier classifier) does nothing");

	}

	@Override
	public void removeClassification(Classification c) {
		logger.warn("removeClassification(Classification c) does nothing");
	}

	public boolean isSearchable() {
		return isSearchable;
	}

	public void setSearchable(boolean isSearchable) {
		this.isSearchable = isSearchable;
	}

	@Override
	public String getContentType() {
		if (getContent() != null)
			return getContent().getContentTypeClassificationAsString();
		return null;
	}

	@Override
	public String getGlyphIcon() {
		return getContent() != null ? getContent().getContentTemplate().getGlyphIcon() : "fad fa-file";
	}

	@Override
	public boolean isDocument() {
		return getContent().getContentTemplate().isDocument();
	}

	@Override
	public boolean isVideo() {
		return getContent().getContentTemplate().isVideo();
	}

	@Override
	public boolean isText() {
		return getContent().getContentTemplate().isText();
	}

	@Override
	public boolean isTool() {
		return getContent().getContentTemplate().isTool();
	}

	@Override
	public boolean isAd() {
		return getContent().getContentTemplate().isAd();
	}

	@Override
	public boolean isActivity() {
		return getContent().getContentTemplate().isActivity();
	}

	@Override
	public boolean isAudio() {
		return getContent().getContentTemplate().isAudio();
	}

	protected boolean isVideoResource(KBFile mod) {
		try {
			return FSUtils.isVideo(mod.getFile().getName());
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	protected boolean isImageResource(KBFile mod) {
		try {
			return FSUtils.isImage(mod.getFile().getName());
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	@Override
	public Map<String, String> getSpecificInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PortalObject getParent() {
		return null;
	}

	@Override
	public String getDataProviderInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}

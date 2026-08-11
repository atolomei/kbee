package com.novamens.kbee.content.ad;

import java.io.Serializable;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.beans.BeansService;
import com.novamens.content.ad.Banner;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.resource.KBFile;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_ONLY, region="content")
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "BANNER")
public class KbeeBanner extends AbstractAd implements Serializable, Banner, ResourceContainer {
	private static final long serialVersionUID = -1L;

	@Column(name = "BANNERTEXT")
	private String bannertext;
	
	@Column(name = "LINK")
	private String link;
	
	@Column(name = "EXTERNAL")
	private boolean external;
	
	@Column(name = "GA")
	private String ga;
	
// 	@ManyToMany(fetch = FetchType.LAZY,  targetEntity = com.novamens.kbee.content.resource.KBFileImpl.class)
//	@JoinTable(name = "ContentResource",  
//				joinColumns 		= { @JoinColumn(name = "content_id") }, 
//				inverseJoinColumns 	= { @JoinColumn(name = "resource_id") }
//				)
//	private List<KBFile> files = new ArrayList<KBFile>();
	
 	
 	public KbeeBanner (ContentTemplate ct) {
 		super(ct);
 	}
 	
	public KbeeBanner () {
		super();
	}
	
	public KbeeBanner (Long id, String titulo, String texto, KBFile imagen, String link) {
		this (id,titulo, texto, imagen, link, false, null);
	}
	
	public KbeeBanner (Long id, String titulo, String texto, KBFile imagen, String link, String ga) {
		this (id,titulo, texto, imagen, link, false, ga);
	}
	
	public KbeeBanner (Long id, String titulo, String texto,  KBFile imagen, String link, boolean external) {
		this (id,titulo,texto, imagen, link,  false,  null);
	}
	
	public KbeeBanner (Long id, String titulo, String texto,  KBFile imagen, String link, boolean external, String ga) {
		setId(id);
		this.bannertext = texto;
		setTitle(titulo);
		this.link = link;
		this.ga = ga;
		this.external = external;
		addFile(imagen);
	}
	
	public String getBannerText() {
		return bannertext; 	
	}
	
	public void setBannerText(String text)	{ 
		this.bannertext=text;
	}
	
	public String getLink() { 
		return link; 				
	}
	
	public void setLink(String link) { 
		this.link=link;
	}
	
	public String getGA() {
		return ga; 					
	}
	
	public void setGA(String ga) { 
		this.ga=ga;
	}
	
	public boolean getExternal() {
		return external; 
	}
	
	public void setExternal(boolean external) { 
		this.external=external;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append( super.toString()+"\n");
		
		if (getBannerText()!=null)
			str.append("\ntext: " + getBannerText());
		
		if ( getLink()!=null)
			str.append("\nliddnk: " +  getLink());
		
		if (  getGA() !=null)
		str.append("\nga: "   + getGA());
		
		if (getExternal())
		str.append("\nexternal: YES");
		

		
		return str.toString();
	}
	
	@Override
	public Content clone() {
		KbeeBanner clone = new KbeeBanner();
		clone.setOId(getOId());
		clone.setName(getName());
		clone.setTitle(getTitle());
		clone.setContentTemplate(getContentTemplate());
		
		List<Classification> clonedclassification = new ArrayList<Classification>();
		for (Classification classification : getClassification()) {
			Classification cc = classification.clone();
			clonedclassification.add(cc);
		}	 
		
		clone.setClassification(clonedclassification);
		
		for (Resource resource: getResources()) 
			clone.addResource(resource); 

		clone.setLink(getLink());
		clone.setBannerText(getBannerText());
		clone.setExternal(getExternal());
		clone.setGA(getGA());

		return clone;
	}
	
	/**
	 * Files must be existing KBFiles, the map includes its id
	 *  
	 * @param map
	 * @return
	 * @throws KbeeRuntimeException
	 */
	static public Banner createFromMap(Map<String, String> map) throws KbeeRuntimeException {
				
		Banner banner = null;
		
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		
		if (map.get("domain_id")==null) 
			throw new KbeeRuntimeException("domain is null");
		
		if (map.get("name")==null)
			throw new KbeeRuntimeException("name is null");
		else if (dao.findContentByName(Banner.class, map.get("name"), map.get("domain_id"))!=null)
			throw new KbeeRuntimeException("Banner already exists");
		
		banner = new KbeeBanner();

		banner.setTitle(map.get("title"));
		banner.setBannerText(map.get("text"));
		banner.setLink(map.get("link"));
		banner.setGA("ga");
		
		if (map.get("external")!=null && map.get("external").toLowerCase().equals("yes"))
			banner.setExternal(true);
		
		//if (map.get("domain_id")!=null) 
		//	banner.setDomain((Domain) dao.findDomainById(map.get("domain_id")));
		
		//if (map.get("image1")!=null) {
		//	KBFile file =  (KBFile) dao.findDomainById(map.get("image1"));
		//	if (file!=null)
		//		banner.addFile(file);
		//	else
		//		throw new KbeeRuntimeException("KBFile not found");
		//}
			
		if (map.get("image2")!=null) {
				KBFile file2 =  (KBFile) dao.findResourceById(KBFile.class, map.get("image2"));
				if (file2!=null)
					banner.addFile(file2);
				else
					throw new KbeeRuntimeException("KBFile not found" + map.get("image2"));
			}
		
		if (map.get("classification")!=null) {
			String clasi[] = map.get("classification").split(";");
			
			for( String ci: clasi) {
				String d[] = ci.split(":");
				String classfier_name = d[0];
				
				Classifier classifier = (Classifier) dao.findModelObjectByName(Classifier.class, classfier_name, map.get("domain_id"));
				
				if (classifier!=null) {
					if (classifier.getDataSetType()==DataSetType.DATE) {
							TemporalAccessor x;
							
							throw new KbeeRuntimeException ("imcompleted sorry");
							
							//try {
								
								// date = new SimpleDateFormat("mm/dd/yyyy", Locale.ENGLISH).parse(d[1]);
								
								//x =  DateTimeFormatter.ofPattern("mm/dd/yyyy").parse(d[1]);
								// banner.addClassification(classifier, date);
							 
							//} catch (ParseException e) {
							//		throw new KbeeRuntimeException("Invalid date (mm/dd/yyyy): " + d[1]);
							//}
					}
					else {
						DataSetMember dm = (DataSetMember) dao.findModelObjectByName(DataSetMember.class, classifier.getDataSet(), d[1].trim());
						if (dm!=null) { 
							banner.addClassification(classifier, dm);
						}
						else
							throw new KbeeRuntimeException("DataSetMember does not exist: " + d[1]);
					}
				}
				else
					throw new KbeeRuntimeException("Classifier does not exist " + classfier_name);
			}
		}
		return banner;
	}

	public KBFile getImage1() {
 		if (getFiles().size()>0)
			return getFiles().get(0);
		
		return null;
	}

	public KBFile getImage2() {
		if (getFiles().size()>1)
			return getFiles().get(1);
		return null;
	}
}

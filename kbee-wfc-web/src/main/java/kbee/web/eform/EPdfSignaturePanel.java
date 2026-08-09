package kbee.web.eform;


import java.time.OffsetDateTime;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.document.IDoc;
import com.novamens.content.entity.Person;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.SignatureType;
import com.novamens.content.user.UserService;
import com.novamens.content.user.UserSignature;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.user.KbeeUserSignature;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.PropertiesFactory;
import kbee.web.console.MyBoxQuery;
import kbee.web.page.InvisibleImage;
import kbee.web.resource.SharedResourceThumbnailImage;
import kbee.web.resource.WebThumbnailReference;
import kbee.web.resource.WebThumbnailSharedReference;


/**
 * 
 * 
 * 
 * 
 *
 */
			
public class EPdfSignaturePanel extends Panel {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EPdfSignaturePanel.class.getName());
	static Boolean SIMULATE_SIGNATURE = "yes".equals(PropertiesFactory.getInstance("kbee").getProperties().getProperty("simulate-hand-written-signature", "no").trim());
	private Boolean hasHWSignature;
	
	/**
	 * 
	 * 
	 * 
	 */
	public EPdfSignaturePanel(UserSignature signature) {
		this("panel", signature);
	}

	
	/**
	 * 
	 * 
	 */
	public EPdfSignaturePanel(String id, UserSignature signature) {
		super(id);
		
		// ResourceReference reference = new PackageResourceReference(EPdfSignaturePanel.class, "digital-signature-icon.png");
		// String url = String.valueOf((RequestCycle.get().urlFor(reference, null)));
		// String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
		// WebMarkupContainer image = new WebMarkupContainer("image");
		// image.add(new AttributeModifier("src", absoluteUrl));
		// add(image);
		
		WebMarkupContainer data = new WebMarkupContainer("data-container");
		WebMarkupContainer image = new WebMarkupContainer("image-container");
		//WebMarkupContainer icon = new WebMarkupContainer("icon-container");
		

		// if (SignatureType.PHONE_APP.equals(signature.getType())) {
		if (((KbeeUserSignature)signature).getHandWriteImage()!=null) {
			data.add(new AttributeModifier("style","width: 100%;"));
			image.add(getImage(signature));
			//icon.setVisible(false);
		}
		else {
			image.add(new InvisibleImage("image"));
			image.setVisible(false);
		}
		add(image);
		//add(icon);
		//image.setVisible(false);
		
		Person person = signature.getUserProfile().getPerson();
		data.add(new Label("person-name", () -> person.getFirstLastName()));
		data.add(new Label("person-identityType", () -> getIdentityType(person)));
		data.add(new Label("person-identityDocument", () -> getIdentityDocument(person)));
		//add(new Label("person-organization", () -> getOrganization(person)));
		data.add(new Label("date", () -> format(OffsetDateTime.now())));
		add(data);
	}
	
	
	
	
	
	protected Boolean isHWSignature(UserSignature signature) {
		if (hasHWSignature==null)
			hasHWSignature = Boolean.valueOf(getSignatureImage(signature) !=null);
		return hasHWSignature;
	}
	protected KBFile getSignatureImage(UserSignature signature) {
		KBFile imagefile = ((KbeeUserSignature) signature).getHandWriteImage();
		/**
		if (imagefile==null)  {
			if (SIMULATE_SIGNATURE) {
				MyBoxQuery query  = new MyBoxQuery( ServiceLocator.getService(SecurityService.class).getSessionUser(), getQueryIndex());
				
				ResultSet kbfiles = query.execute();
				int index=0;
				while (kbfiles.hasNext() && index++<1000) {
					IDoc content = (IDoc) kbfiles.next().getObject();
					if (content!=null && content.getResources()!=null && content.getResources().size()>0) {
						KBFile kb= (KBFile) content.getResources().get(0);
						if (	kb.getFileName()!=null && (kb.getFileName().toLowerCase().contains("signature") ||
								kb.getFileName().toLowerCase().contains("firma"))) {
							imagefile=kb;
							break;
						}
					}
				}
			}
		}
		**/
		return imagefile;
	}
	
	
	protected Image getImage(UserSignature signature) {
		KBFile imagefile = getSignatureImage(signature);
		
		if (imagefile==null)
			return new InvisibleImage("image");
		
		ResourceReference reference = new WebThumbnailSharedReference(imagefile, ThumbnailSize.MINI);
		String url = String.valueOf((RequestCycle.get().urlFor(reference, null)));
		String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
		Image image = new Image("image", new UrlResourceReference(Url.parse(absoluteUrl)));
		return image;
		
	}
	

	protected String getIdentity(Person person) {
		PersonService personService = person.getService(PersonService.class);
		String type = personService.getIdentityType();
		String document = personService.getIdentityDocument();
		String identity = type!=null && document!=null ? type + " " + document : "";
		return identity;
	}
								
	protected String getIdentityDocument(Person person) {
		PersonService personService = person.getService(PersonService.class);
		String type = personService.getIdentityDocument();
		return type!=null?type:"";
	}
	
	protected String getIdentityType(Person person) {
		PersonService personService = person.getService(PersonService.class);
		String type = personService.getIdentityType();
		return type!=null?type:"";
	}
	
	protected String getOrganization(Person person) {
		String organization = person.getService(PersonService.class).getOrganization();
		return organization!=null ? organization : "";
	}
	
	protected String format(OffsetDateTime date) {
		return ServiceLocator.getService(DateTimeService.class).getDateDisplayString(date);
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
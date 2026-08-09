package kbee.web.portal6.library;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

@SuppressWarnings("serial")
public class BannerPanel extends KBPanel implements PortalViewRender {
	private static final long serialVersionUID = 1L;

	IModel<Person> personmodel;
	IModel<String> title;

	IModel<String> text;
	String icon_css;
	String link;
	String style;
	
	
	String text_style;
	
	
	
			
	public BannerPanel(String id, IModel<Person> model) {
		super(id);
		setPersonModel(model);
	}

	public IModel<Person> getPersonModel() {
		return personmodel;
	}

	public void setPersonModel(IModel<Person> personmodel) {
		this.personmodel = personmodel;
	}
	
	public Person getPerson() {
		return this.personmodel!=null ? this.personmodel.getObject() : null;
	}
	
	public String getTextHTMLStyle() {
		return text_style;
	}
	
	public void setTextHTMLStyle(String s) {
		text_style=s;
	}

	public User getUser() {
		if (personmodel==null) return null;
		UserProfile profile = personmodel.getObject().getProfile(UserProfile.class);
		if (profile==null) return null;
		User user = profile.getUser();
		return user;
	}


	
	boolean is_bck = false;
	
	public boolean isBck() {
		return is_bck; 
	}
	
	public void setBck( boolean b) {
		this.is_bck=b;
	}
	
	
	
			
	@Override
	public void onInitialize() {
		super.onInitialize();
	
		if (getHTMLStyle()!=null)
			add( new AttributeModifier("style", getHTMLStyle()));
		

		WebMarkupContainer  banner_container = new WebMarkupContainer ("banner-container");
		add(banner_container);
		
		ResourceReference abeja = ServiceLocator.getService(BrandingWebService.class).getApplicationBannerBackground();
		
		if (isBck()) {
			String imagehref = RequestCycle.get().urlFor(abeja, null).toString();
			banner_container.add(new AttributeModifier("style", "background: url(" + imagehref + ") no-repeat 0 0 scroll; background-size: cover; float:left; width:100%; border:1px solid #eeeeee; border-radius:6px;"));
		}

		
		Image i_abeja = new Image("image", abeja) { 
			protected boolean shouldAddAntiCacheParameter()	{
				return false;
			}
		};
		WebMarkupContainer link_image = new WebMarkupContainer("link-image");
		link_image.add( new AttributeModifier("href", link));
		link_image.setVisible(false);
		link_image.add(i_abeja);
		banner_container.add(link_image);
		
		
		Label l_title = new Label("title", title);
		l_title.setEscapeModelStrings(false);
		
		Label l_text =  new Label ("text", text);
		l_text.setEscapeModelStrings(false);

		
		if (getTextHTMLStyle()!=null)
			l_text.add( new AttributeModifier("style", getTextHTMLStyle()));
	
		
		ExternalLink link_title = new ExternalLink("link-title", link);
		//link_title.add( new AttributeModifier("href", link));
		link_title.setVisible(this.title!=null || link!=null);
		banner_container.add(link_title);
		
		
		link_title.add(l_title);
		
		banner_container.add(l_text);
		
		
		//WebMarkupContainer registrationpanel = new WebMarkupContainer("registration") {
		//	public boolean isVisible() {
		//		return getUser()==null;
		//	}
		//};
		
		//add(registrationpanel);
		//registrationpanel.add(new ExternalLink("registration-link", getRegistrationUrl(getPerson())));
	}	

	public IModel<String> getTitle() {
		return title;
	}

	public void setTitle(IModel<String> title) {
		this.title = title;
	}

	public IModel<String> getText() {
		return text;
	}

	public void setText(IModel<String> text) {
		this.text = text;
	}

	public String getIcon_css() {
		return icon_css;
	}

	public void setIcon_css(String icon_css) {
		this.icon_css = icon_css;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getHTMLStyle() {
		return style;
	}

	public void setHTMLStyle(String style) {
		this.style = style;
	}
	
//	private String getRegistrationUrl(Person person) {
//		KbeeJson data = new KbeeJson();
//		if (person==null) return null;
//		data.put("id", String.valueOf(person.getId()));
//		data.put("date", person.getCreationOffsetDateTime().toString());
//		data.put("domain", String.valueOf(person.getDomain().getId()));
//		return person.getService(UrlService.class).getServerUrl() + "/registrationinit/" + ServiceLocator.getService(TokenService.class).getToken(data);
//	}
} 
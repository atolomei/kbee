package kbee.web.entity;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Resource;
import com.novamens.content.entity.Person;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.event.EditEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.kbee.wicket.util.GenericPhoto;
import com.novamens.kbee.wicket.util.InvisiblePhoto;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceThumbnailImage;

@SuppressWarnings("serial")
public class UserHeaderPanel extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserHeaderPanel.class.getName());

	private IModel<Person> model;
	
	static final int WIDTH = 82;
	
	public UserHeaderPanel(String id, IModel<Person> model, final boolean is_myaccount) {
		super(id);
		setOutputMarkupId(true);
		setModel(model);

		add(new WicketEventListener<EditEvent<Person>>() {
			public void onEvent(EditEvent<Person> event) {
				 onUpdate(event.getRequestTarget());
			}
		});
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer arc= new WebMarkupContainer("archived") {
			@Override
			public boolean isVisible() {
				try {
					UserProfile userProfile = UserHeaderPanel.this.getModel().getObject().getProfile(UserProfile.class);
					KbeeUser user = (KbeeUser) userProfile.getUser();
					return !(user.getState()==ObjectState.ENABLED);
				} catch (Exception e) {
					return false;
				}
			}
		};
		
		add(new Label("jobprofile",  getModel().getObject().getWorkPosition()));
		add(arc);
		add(getPhoto());
		
		WebMarkupContainer asc= new WebMarkupContainer("avatar-status-container") {
			@Override
			public boolean isVisible() {
				Person person = UserHeaderPanel.this.getModel().getObject();
				if (person!=null && person.getPhoto()!=null && person.getDomain()!=null && person.isPhotoDomainLogo())  
							return true;
				return false;
			}
		};
		add(asc);
		asc.add(getDomainLogo());

		add(new Label("name", new PropertyModel<String>(model, "FirstLastName")));
	}
	
 	
	
	/**
	 * @param user
	 * @return
	 */
	protected Image getDomainLogo() {
		try {
			
			Person person = getModel().getObject();
			
			if (person!=null && person.getPhoto()!=null && person.getDomain()!=null && person.isPhotoDomainLogo()) { 
				KBFile im = ((KbeeDomain)  person.getDomain()).getLogo();
				if (im!=null) {
					Image ima = new ResourceThumbnailImage<>("avatar-status", new ObjectModel<Resource>((Resource) im), ThumbnailSize.AVATAR_STATUS);
					ima.add(new AttributeModifier("title", person.getDomain().getName()));
					return ima;
				}
			}
			return new GenericPhoto("avatar-status");
			
		} catch (Exception e) {
			logger.debug(e);
			return new GenericPhoto("avatar-status");
		}
	}

	public void onUpdate(AjaxRequestTarget target) {
		addOrReplace(getPhoto());
		((WebMarkupContainer) get("avatar-status-container")).addOrReplace(getDomainLogo());
		target.add(this);
	}
	
	protected void setModel(IModel<Person> model) {
		this.model = model;
	}
	
	protected IModel<Person> getModel() {
		return this.model;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}

	protected Image getPhoto() {
		try {
			Person person = getModel().getObject();
			
			
			if (person!=null && person.getPhoto()!=null) 
					 return  new ResourceThumbnailImage("photo", new ObjectModel<Resource>((Resource) person.getPhoto()) , ThumbnailSize.SMALL);

			
			return ServiceLocator.getService(BrandingWebService.class).getUserAvatarPhoto("photo", person );
			
			// return new InvisiblePhoto("photo");
			
			
		} catch (Exception e) {
			logger.error(e);
			return new GenericPhoto("photo");
		}
	}
}
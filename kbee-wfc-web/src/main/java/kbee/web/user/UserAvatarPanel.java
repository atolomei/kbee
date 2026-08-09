package kbee.web.user;
		
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;

import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.wicket.markup.html.console.event.EditEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.kbee.wicket.util.GenericPhoto;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceThumbnailImage;


public class UserAvatarPanel extends ModelPanel<User> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserAvatarPanel.class.getName());

	private IModel<User> model;
	private IModel<Person> pmodel;
	
	private Boolean has_avatar_status;
	
	
	public UserAvatarPanel(String id, IModel<User> model) {
		super(id, model);
		setModel(model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		try {
			Image im = getPhoto(getModel().getObject());
			im.add(new AttributeModifier("title", getModel().getObject().getFirstLastName()));
			add(im);
		} catch (Exception e) {
			logger.error(e);
			add(new GenericPhoto("photo"));
		}
		
		WebMarkupContainer acc= new WebMarkupContainer("avatar-status-container") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return hasAvatarStatus();
			}
		};
		
		add(acc);
		acc.add(getAvatarStatus(getModel().getObject()));
		
	}
	
	

	public IModel<User> getModel() {
		return model;
	}
	

	public void setModel(IModel<User> model) {
		this.model = model;
	}
	

	@Override
	public void onDetach() {
		super.onDetach();
		
		if (model!=null)
			model.detach();
		
		if (pmodel!=null)
			pmodel.detach();
	}
	

	protected UserProfile getUserProfile(User user) {
		return getContentDao().findUserProfileByUser(user);
	}


	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}


	protected boolean hasAvatarStatus() {
		
		if (this.has_avatar_status!=null)
			return has_avatar_status.booleanValue();
		
		Person person = getPersonModel().getObject();
		
		has_avatar_status = Boolean.valueOf(person!=null && person.isPhotoDomainLogo());
		return has_avatar_status.booleanValue();
	}

	
	private IModel<Person> getPersonModel() {
		if (pmodel!=null)
			return pmodel;
		Person person = getContentDao().findUserProfileByUser(getModel().getObject()).getPerson();
		pmodel = new ObjectModel<Person>(person);
		return pmodel;
		
	}
	
	/**
	 * @param user
	 * @return
	 */
	protected Image getAvatarStatus(User user) {
		try {
			
			Person person = getPersonModel().getObject();
			
			if (person!=null && person.getPhoto()!=null && person.getDomain()!=null) { 
			
				KBFile im = ((KbeeDomain) person.getDomain()).getLogo();
			
				if (im!=null) {
					Image ima = new ResourceThumbnailImage<>("avatar-status", new ObjectModel<Resource>((Resource) im), ThumbnailSize.AVATAR_STATUS);
					ima.add(new AttributeModifier("title", person.getDomain().getName()));
					return ima;
				}
			}
			return new GenericPhoto("avatar-status");
			
			
		} catch (Exception e) {
			logger.error(e);
			return new  GenericPhoto("avatar-status");
		}
	}
	
	 
	protected Image getPhoto(User user) {
		try {
			Person person = getPersonModel().getObject();
			
			if (person!=null && person.getPhoto()!=null) 
				return  new ResourceThumbnailImage<>("photo", new ObjectModel<Resource>((Resource) person.getPhoto()) , ThumbnailSize.MINI);
			
			
			return new Image("photo", ServiceLocator.getService(BrandingWebService.class).getUserAvatarResourceReference(person));
			
			} catch (Exception e) {
			logger.error(e);
			return new GenericPhoto("photo");
		}
	}
	
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<EditEvent<Person>>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(EditEvent<Person> event) {
				try {
					Image im = getPhoto(getModel().getObject());
					im.add(new AttributeModifier("title", getModel().getObject().getFirstLastName()));
					 UserAvatarPanel.this.addOrReplace(im);
					 ((WebMarkupContainer) get("avatar-status-container")).addOrReplace(getAvatarStatus(getModel().getObject()));
					 event.getRequestTarget().add(UserAvatarPanel.this);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		});
	}	

}


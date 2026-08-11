package kbee.web.security.user;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.kbee.wicket.util.GenericPhoto;
import com.novamens.kbee.wicket.util.InvisiblePhoto;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceThumbnailImage;


public class UserAvatarColumnPanel extends ModelPanel<Person> {
			
	private static final long serialVersionUID = 1L;
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserAvatarColumnPanel.class.getName());

	private Boolean has_avatar_status;
	
	public UserAvatarColumnPanel(String id,  IModel<Person> mobject) {
		super(id, mobject);
	}

	/**
	 * 
	 */
	@Override
 	public void onInitialize() {
 		super.onInitialize();

		WebMarkupContainer acc= new WebMarkupContainer("avatar-status-container") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return hasAvatarStatus();
			}
		};
		
		add(acc);
		acc.add(getAvatarStatus());

		
		WebMarkupContainer uc = new WebMarkupContainer("user-photo-container");
		add(uc);
 		
 		Link<Void> link = new Link<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				fire(new ClickEvent<Person>(null, UserAvatarColumnPanel.this.getModel(), 0));
			}
 		};
 		
 		uc.add(link);
 		
 		if (getTarget()!=null)
 			link.add(new AttributeModifier("target", getTarget()));
 		
 		if (getCss()!=null) 
 			uc.add(new AttributeModifier("class", getCss()));
 		
		link.add(getPhoto());
 	}	

	
	
	/**
	protected Image getPhoto() {
		try {
			Person person = getModel().getObject();
			if (person!=null && person.getPhoto()!=null) 
				return  new ResourceThumbnailImage("photo", new ObjectModel<Resource>((Resource) person.getPhoto()), ThumbnailSize.MINI);
			return new Image("photo", ServiceLocator.getService(BrandingWebService.class).getUserAvatarResourceReference(person));
		} catch (Exception e) {
			logger.error(e);
			return new GenericPhoto("photo");
		}
	}
**/
	
	protected Image getPhoto() {
		try {

			Person person = getModel().getObject();
			
			if (person!=null && person.getPhoto()!=null) 
				return  new ResourceThumbnailImage<>("photo", new ObjectModel<Resource>((Resource) person.getPhoto()) , ThumbnailSize.MINI);
							
			
			ResourceReference res;
			if (person instanceof KbeePersonMember)
				res = ServiceLocator.getService(BrandingWebService.class).getUserAvatarResourceReference( ((KbeePersonMember) person).getPerson() );
				else
					res = ServiceLocator.getService(BrandingWebService.class).getUserAvatarResourceReference(person);
			return new Image("photo", res);
			
			} catch (Exception e) {
			logger.error(e);
			return new GenericPhoto("photo");
		}
	}


	
	/**
	 * @param user
	 * @return
	 */
	protected Image getAvatarStatus() {
		try {
			
			Person person = getModel().getObject();
			
			if (person!=null && person.getPhoto()!=null && person.getDomain()!=null && person.isPhotoDomainLogo()) { 
				KBFile im = ((KbeeDomain)  person.getDomain()).getLogo();
				
				if (im!=null) {
					Image ima = new kbee.web.resource.ResourceThumbnailImage("avatar-status", new ObjectModel<Resource>( (Resource) im) , ThumbnailSize.AVATAR_STATUS);
					
					ima.add(new AttributeModifier("title", person.getDomain().getName()));
					return ima;
				}
			}
			return new InvisiblePhoto("avatar-status");
			
		} catch (Exception e) {
			return new InvisiblePhoto("avatar-status");
		}
	}
 	
	protected IModel<String> getAnchorTitle() {
		return null;
	}

	protected String getCss() {
		return "userphotocolumn";
	}
	
	
	String target=null;
	
	
	
	protected String getTarget() {
		return target;
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
		
		Person person = getModel().getObject();
		
		has_avatar_status = Boolean.valueOf(person!=null && person.isPhotoDomainLogo());
		return has_avatar_status.booleanValue();
	}
	
}



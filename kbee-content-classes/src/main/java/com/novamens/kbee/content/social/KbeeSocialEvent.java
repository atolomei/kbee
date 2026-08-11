package com.novamens.kbee.content.social;

 
import com.novamens.content.base.Social;
import com.novamens.content.social.SocialEvent;
import com.novamens.event.AbstractEvent;
import com.novamens.security.User;
 
/**
 * Evento generado por instancias de clases sociales:
 * 
 * {@link Vote}
 * {@link Report} 
 *
 */
public class KbeeSocialEvent extends AbstractEvent implements SocialEvent {

	private Social social_object; 		// Vote, Report 
	private User user; 					// User que Voto o Reportó
	private String object_url;			// Url para ir al detalle

	public KbeeSocialEvent(Object object, Social social, User user) {
			this(object, social, user, null);
	}

	public KbeeSocialEvent(Object object, Social social, User user, String url) {
		super(object);
		this.social_object=social;
		this.user=user;
		this.object_url=url;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Social getSocialObject() {
		return social_object;
	}
	
	@Override
	public String getSocialObjectUrl() {
		return object_url;
	}
	
}

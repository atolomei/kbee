package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.beans.BeansService;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EFormDataSource.Url;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.PersonMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.dom.KbeeUrl;
import com.novamens.service.ServiceLocator;

@SuppressWarnings("serial")
public class KbeeERolesModel implements EFieldModel<Role> {
	private static final long serialVersionUID = 1L;
	
	public class DataSourceUrl implements Url {
		String label;
		com.novamens.dom.Url url;
		public DataSourceUrl(String label, com.novamens.dom.Url url) {
			setLabel(label);
			setUrl(url);
			
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public com.novamens.dom.Url getUrl() {
			return url;
		}
		public void setUrl(com.novamens.dom.Url url) {
			this.url = url;
		}
	}

	@Override
	public void set(Object object, List<Role> data) {
	}
	
	@Override
	public void set(Object object, Object data) {
		if (!(object instanceof PersonMember)) return;
		if (data==null) return;
		Person person = ((PersonMember)object).getPerson();
		UserProfile profile = person.getProfile(UserProfile.class);
		if (profile==null) return;
		List<UserRole> roles = new ArrayList<UserRole>();
		for (UserRole userRole : profile.getRoles()) {
			if (userRole.getRole().isEntity() || userRole.getRole().isCanonical()) {
				roles.add(userRole);
			}
		}
		roles.add(new KbeeUserRole((Role)data, profile.getUser(), null));
		profile.setRoles(roles);
	}
	
	@Override
	public List<Role> getValues(Object object) {
		return null;
	}
	
	public Role get(Object object) {
		if (!(object instanceof PersonMember)) return null;
		Person person = ((PersonMember)object).getPerson();
		UserProfile profile = person.getProfile(UserProfile.class);
		if (profile==null) return null;
		for (UserRole userRole : profile.getRoles()) {
			Role role = userRole.getRole();
			if (!role.isCanonical() && !role.isEntity()) {
				return role;
			}
		}
		return null;
	}
	
	@Override
	public EFormDataSource<Role> getDataSource(Classificable object) {
		return new EFormDataSource<Role>() {
			@Override
			public List<Role> getValues() {
				List<Role> roles = new ArrayList<Role>();
				for (Role role : getSecurityDao().getRoles(getDomain())) {
					if (!role.isCanonical() && !role.isEntity()) {
						roles.add(role);
					}
				}
				return roles;
			}
			@Override
			public List<Suggestion> getValues(String pattern) {
				return null;
			}
			@Override
			public List<Suggestion> getValues(String pattern, Map<String, Object> parameters) {
				return null;
			}
			public List<Url> getUrls() {
				List<Url> urls = new ArrayList<Url>();
				urls.add(new DataSourceUrl("Roles", new KbeeUrl("security-roles-page", new HashMap<String, String>())));
				return urls;
			}
			public boolean isReadable() {
				return true;
			}
		};
	}
	
	public String serialize(Classificable formobject, Role value) {
		String serialized = String.valueOf(value.getId());
		return serialized;
	}
	
	@Override
	public Role deserialize(Classificable formobject, String token) {
		Role value = null;
		if (token==null) return null;
		value = getSecurityDao().findRoleById(Long.valueOf(token));
		return value;
	}
	
	public boolean handle(Event event) {
		return false;
	}
	
	@Override
	public List<Role> onEvent(Event event) {
		return null;
	}
	
	@Override
	@JsonIgnore
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	@JsonIgnore
	public String getMetainfoMessage() {
		return null;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		if (object!=null && !(object instanceof Person)) {
			return "no user";
		}
		return null;
	}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		return "Roles";
	}
	
	@Override
	@JsonIgnore
	public String getDescription(Locale locale) {
		return "Roles";
	}
	
	@Override
	public String getTypeLabel() {
		return "Roles";
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
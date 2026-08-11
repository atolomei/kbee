package kbee.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApiUser extends ApiClassificable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String firstName;
	private String lastName;
	private String password;
	private String email;
	private String phone;
	private String timeZone;
	private String workPosition;
	private boolean enabled;
	private Locale locale;
	private ApiResource photo;
	private String externalid;
	
	private List<IUserRole> roles = null;
	private List<ApiProxy> groups = new ArrayList<ApiProxy>();
	private List<IProfile> profiles = null;
	
	private ApiProxy person;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getWorkPosition() {
		return workPosition;
	}
	
	public void setWorkPosition(String position) {
		this.workPosition = position;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getTimeZone() {
		return timeZone;
	}
	
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public ApiResource getPhoto() {
		return photo;
	}
	
	public void setPhoto(ApiResource resource) {
		this.photo = resource;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean value) {
		this.enabled = value;
	}
	
	public String getExternalId() {
		return externalid;
	}
	
	public void setExternalId(String id) {
		this.externalid = id;
	}
	
	public void addGroup(ApiProxy group) {
		groups.add(group);
	}
	
	public List<ApiProxy> getGroups() {
		return groups;
	}
	
	public void setRole(IUserRole userRole) {
		if (roles == null) roles = new ArrayList<IUserRole>();			
		roles.add(userRole);
	}
	
	public List<IUserRole> getRoles() {
		if (roles == null) roles = new ArrayList<IUserRole>();			
		return roles;
	}
	
	public void setProfile(IProfile profile) {
		if (profiles == null) profiles = new ArrayList<IProfile>();			
		profiles.add(profile);
	}
	
	public List<IProfile> getProfiless() {
		if (profiles == null) profiles = new ArrayList<IProfile>();			
		return profiles;
	}

	public ApiProxy getPerson() {
		return person;
	}

	public void setPerson(ApiProxy person) {
		this.person = person;
	}
}

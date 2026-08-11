package com.novamens.kbee.content.query;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.query.SavedQuery;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.ServiceLocator;

/**
 * <p>Saved {@link Query} from grids </p>
 * 
 * @see  
 * {@link Browser}
 * {@link Query}
 *
 */
@Entity
@Table(name = "savedquery")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@DynamicInsert
public class KbeeSavedQuery extends AbstractObject implements SavedQuery {
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeSavedQuery.class.getName());
	
	@Id @GeneratedValue
	@Column(name = "id")
	private Long id;
	
	@Column(name = "console")
	private String console;  
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "browser")
	private String browser;
	
	@Column(name = "statement")
	private String statement;
	
	@Column(name = "position")
	private int position;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeUser.class)
	@JoinColumn(name="USER_ID", nullable=false)
	private User user;
	
	@Column(name = "is_system")
	private boolean is_system;
	
	@Column(name = "is_home")
	private boolean is_home;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeSite.class)
	@JoinColumn(name="site_id",   nullable=true)
	private Site site;

	private transient String t_statement = null;

	
	
	public KbeeSavedQuery() {
	}
	
	public KbeeSavedQuery(User user, String console) {
		setConsole(console);
		setUser(user);
		setPosition(0);
	}
	
	public KbeeSavedQuery(User user, String title, String console, Site site, Map<String, Object> parameters) {
		setTitle(title);
		setConsole(console);
		setStatement(getStatement(parameters));
		setUser(user);
		setPosition(0);
		setSite(site);
	}
	
	@Override
	public Long getId() { 
		return id;		
	}
	
	public String getTitle() { 
		return title;		
	}
	
	public void setTitle(String title) { 
		this.title = title;		
	}
	
	public String getConsole() { 
		return console;		
	}
	
	public void setConsole(String console) { 
		this.console = console;		
	}
	
	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	@Override
	public String getStatement() { 
		return statement;		
	}
	
	public void setStatement(String statment) { 
		this.statement = statment;		
	}
	
	@Override
	public User getUser() {
		return user;
	}
	
	public void setUser(User u) {
		this.user=u;
	}
	
	
	public void setPosition(int position) { 
		this.position = position;		
	}
	
	public Map<String, Object> getParameters() {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if (getStatement()==null)
			return parameters;
		
		String clauses[] = getStatement().split(",");
		List<String> members = new ArrayList<String>();
		String lastvalue = null;
		String lastparameter = null;
		
		for (String clause : clauses) {
			int i = clause.indexOf("=");
			if (i>0) {
				String parameter = clause.substring(0,i);
				String value = clause.substring(i+1);
				if ("member".equals(parameter))
					members.add(value);
				else
					parameters.put(parameter, value);
				lastvalue = value;
				lastparameter = parameter;
			}
			else {
				lastvalue += ", " + clause;
				parameters.put(lastparameter, lastvalue);
			}
		}
		if (!members.isEmpty())
			parameters.put("members", members);
		
		return parameters;
	}
	
	
	/**
	 * 
	 * @param parameters
	 * @return
	 */
	
	
	public String getStatement(Map<String, Object> parameters) {
		
		if (t_statement!=null)
			return t_statement;
		
		StringBuilder result = new StringBuilder();
		for (java.util.Iterator<String> iterator = parameters.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			if (key.equals("members")) {
				@SuppressWarnings("unchecked")
				List<String> members = (List<String>)parameters.get(key);
				for (String path : members) {
					result.append("member=" + path + ",");
				}
			}
			else {
				if (parameters!=null && parameters.get(key)!=null) {
					String value = parameters.get(key).toString();
					if (value!=null) {
						value.replace("=","");
						value.replace(",","");
						result.append(key + "=" + value + ",");
					}
				}
			}
		}
		if (result.length()==0)  
			 t_statement="";
		else
			t_statement = result.toString().substring(0, result.length()-1);
		
		return t_statement;
		
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof SavedQuery)) 
			return false;
		return ((SavedQuery)object).getId().equals(getId());
	}
	
	@Override
	public String getDisplayName() {
		return title;
	}
	
	@Override
	public boolean isSystem() {
		return this.is_system;
	}
	
	public void setIsSystem( boolean b) {
		this.is_system=b;
	}

	@Override
	public void setId(Serializable id) {
		this.id=(Long)id;
	}

	@Override
	public String getName() {
		return getDisplayName();
	}
	
	@Override
	public AuditSet getAuditSet() {
		return AuditSet.SYSTEM;
	}
	
	public ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	@Override
	public boolean isHome() {
		return is_home;
	}

	
	public void setHome(boolean b) {
		is_home=b;
	}
	
	
	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}


}

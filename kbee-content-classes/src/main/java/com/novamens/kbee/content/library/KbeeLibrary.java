package com.novamens.kbee.content.library;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Proxy;

import com.novamens.content.base.Content;
import com.novamens.content.library.Library;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.Criteria;
import com.novamens.kbee.content.security.JavaIqlEvaluator;
import com.novamens.kbee.dom.AbstractObject;

import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/**
 * <p>A Library is a subset Contents. The Library's Subset is defined by the IQL expresssion, all Content that evaluate true to the IQL expression belong to the Library</p>
 *
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_CABINET")
@Proxy(lazy=false)
public class KbeeLibrary extends  AbstractObject implements Library {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeLibrary.class.getName());
			
	private static Map<String, String> library_group = new HashMap<String, String>();
	
	static {
		library_group.put( Library.STANDARD, Library.STANDARD);
		library_group.put( Library.EXTERNAL, Library.EXTERNAL);
		library_group.put( Library.KBASE, Library.KBASE);
		library_group.put( Library.TEMPLATES, Library.TEMPLATES);
	}
	
	@Id 
	@SequenceGenerator(name = "library_sequencer", sequenceName = "domainid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "library_sequencer")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "name")
	private String name;

	
	@Column(name = "display_name")
	private String displayName;
	
	@Column(name = "key")
	private String key;
	
	@Column(name = "criteria")
	private String criteria;
	
	@Column(name = "description")
	private String description;

	@Column(name = "readOnly")
	private boolean readOnly;
	
	@Column(name = "listOrder")
	private int listOrder;
	
	@Column(name = "canonical")
	private boolean canonical;
	
	@Column(name = "page")
	private String page;
	

	/** Delete the Group when the library is deleted  */
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeGroup.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "reader_group", updatable=false)
	private Group readers;
	
	@Override
	public Long getId()	{
		return id;
	}
	
	public void setId(Serializable id) {
		this.id = (Long) id;
	}
	
	@Override
	public int getOrder() {
		return this.listOrder;
	}
	
	public void setOrder(int od) {
		this.listOrder=od;
	}
	
	public void setDescription(String name) {
		this.description=name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setName(String name) {
		this.displayName=name;
	}
	
	@Override
	public String getName() {
		return getDisplayName();
	}
	
	@Override
	public String getDisplayName() {
		if (displayName==null || displayName.length()==0)
			return key;
		return displayName;
	}
	
	public void setDisplayName(String name) {
		if (getReaders()!=null) {
			((KbeeGroup)getReaders()).setName(name);
		}
		displayName = name;
	}
	
	@Override
	public String getKey() {
		return key;
	}
	
	public void  setKey(String name) {
		key=name;
	}
	
	@Override
	public Criteria getCriteria() {
		if (criteria!=null && criteria.contains(";") && criteria.contains("="))
			return new ParametersCriteria(criteria);
		else
			return new IqlCriteria(getDomain(), criteria);
	}
	
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	
	public String getStatement() {
		return criteria;
	}
	
	public void setStatement(String criteria) {
		this.criteria = criteria;
	}
	
	@Override
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public void setReadOnly(boolean b) {
		this.readOnly = b;
	}
	
	@Override
	public boolean isCanonical() {
		return canonical;
	}
	
	public void setCanonical(boolean b) {
		this.canonical = b;
	}
	
	public Group getReaders() {
		return this.readers;
	}
	
	public void setReaders(Group group) {
		this.readers = group;
	}
	
	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	@Override
	public boolean isReadable() {
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		if (user!=null) {
			if (ServiceLocator.getService(SecurityService.class).isMember(user, KbeeGlobalRole.DOMAIN_ADMIN.getId()))
				return true;
			
			if (getReaders()==null)  
				return true;
			
			if (user.isMember(getReaders())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean includes(Content content) {
		if (isCanonical() || "".equals(getStatement())) {
			return true;
		}
		else  {
			if (getCriteria() instanceof IqlCriteria) {
				Expression iqlexpression = getCriteriaExpression();
				if (iqlexpression!=null) {
					JavaIqlEvaluator evaluator = new JavaIqlEvaluator(iqlexpression);
					boolean evaluation = evaluator.evaluate(content);
					return evaluation;
				}
			}
		}
		return false;
	}
	
	@Override
	public String getDisplayCriteria() {
		Expression e=getCriteriaExpression();
		if (e!=null)
			return getCriteriaExpression().toString();
		return null;
	}
	
	
	public Expression getCriteriaExpression() {
		try {
			Expression expression = getDomain().getService(IqlService.class).getExpression(getStatement());
			return expression;
		} 
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}


	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		if (getId()!=null)
			str.append("id: "+ getId().toString());
		else
			str.append("id: null");
			
		if (this.getDisplayName()!=null)
			str.append(" | displayName: "+ this.getDisplayName());
		
		str.append(" | order: "+ String.valueOf(this.getOrder()));
		
		if (this.getCriteria()!=null)
			str.append(" | Criteria" + this.getCriteria().toString());
		
		if (getDomain()!=null)
			str.append(" | Domain" + this.getDomain().getName());
			
		return str.toString();
	}

	@Override
	public boolean isEnabled() {
		return this.getState()==ObjectState.ENABLED;
	}
}

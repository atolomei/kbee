package com.novamens.kbee.content.userlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ObjectId;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.model.KbeeUserListClassification;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.util.KbeeRuntimeException;

/**

index by
--------

content-index-context.xml
userlistitem-schema

The classification returns that of the container object
being "Classifiable" the indexer takes the info of the object

The SolR Index factory
use a native class table: ContentClass (kbeeUserListItem is added)

UserListUpdateListener

ResultSetWrapper
UserListQuery
UserListUpdateListener

*/

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "kb_user_list_item")
@DynamicInsert
public class KbeeUserListItem extends AbstractObject implements UserListItem {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUserListItem.class.getName());
	
	@Id 
	@SequenceGenerator(name = "content_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_sequencer")
	@Column(name = "id")
	private Long id;
	
	/**
	 * put by the UserList
	 */
	@ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.DETACH, targetEntity = KbeeUserList.class)
	@JoinColumn(name="userlist_id", insertable=false, updatable=false, nullable=false)
	private UserList userlist;
	
	/**
	 * put by the UserList
	 * this is a redundant field to speed up retrieval for girds
	 */
	@Column(name = "console", nullable=false)
	private String console;

	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeSite.class)
	@JoinColumn(name="site_id",   nullable=true)
	private Site site;

	
	/**
	 * put by the UserList
	 * this is a redundant field to speed up retrieval for girds
	 */
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "owner_id", nullable=false)
	private User owner;
	
	@Column(name = "type")
	private int type=CONTENT;
	
	@Column(name = "version_match")
	private int version_match=PUBLISHED;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "content_id", nullable=true)
	private Content content;
	
	@Column(name = "oid")
	private Long oid;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeDataSetMember.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "datasetmember_id", nullable=true)
	private DataSetMember datasetmember;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id", nullable=true)
	private User userItem;
	
	transient String objectid = null;

	@Column(name = "title")
	private String 	title; 
	
	
	public KbeeUserListItem() {
	}
	
	public KbeeUserListItem(UserList list) {
		this.userlist=list;
		this.console=list.getConsole();
		this.owner=list.getOwner();
	}
	
	
	public List<Classifier> getClassifiers() {
		if (getObject()==null)
			return new ArrayList<Classifier>();
		return ((Classificable)getObject()).getClassifiers();
	}
	@Override
	public 	List<Classification> getClassification() {
		try {
			if (getObject()==null)
				return null;
			return ((Classificable)getObject()).getClassification();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}


	@Override
	public Map<String, List<String>>  getAttributesAsMap() {
		try {
			return ((Classificable)getObject()).getAttributesAsMap();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	
	
	public void addClassification(KbeeUserListClassification c) {
	}
	
	
	
	
	
	public void setClassification(List<Classification> classifications) {
	}
	
	public List<Classification> getClassification(Classifier classifier) {
		return ((Classificable)getObject()).getClassification(classifier);
	}	
	
	public void setClassification(Classifier classifier, List<DataSetMember> members) {
	}
	
	public void setClassification(Classifier classifier, DataSetMember member) {
	}
	
	
	public void addClassification(Classifier c, DataSetMember dm) {
		// TODO AT
	}
	
	public void addClassification(Classification clasi) {
		
	}
				
	public void removeAllClassification(Classifier classifier) {
		
	}
	
	public void removeClassification(Classification c) {
		
	}
	
	public void setAttributeValues(Attribute name, List<String> values) {
		
	}
	
	public List<String> getAttributeValues(Attribute name) {
		if (getObject()!=null)
			return ((Classificable)getObject()).getAttributeValues(name);
		return null;
	}

	@Override
	public int getVersionMatch() {
		return this.version_match;
	}

	public void setVersionMatch(int v) {
		this.version_match=v;
	}
	
	@Override
	public int getUserListItemType() {
		return this.type;
	}
	
	public void settUserlist(UserList list) {
		this.userlist = list;
	}
	
	public UserList getUserlist() {
		return this.userlist;
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContent(Content content) {
		this.content=content;
		this.oid=Long.valueOf(content.getOId());
		objectid = new ObjectId(getContent()).toString();
		logger.debug("setContent -> " + objectid.toString() + " " + content.getTitle());
		this.type=CONTENT;
	}

	public void setItemUser(User user) {
		this.userItem=user;
		objectid = new ObjectId(getItemUser()).toString();
		logger.debug("setItemUser -> " + objectid.toString() +  " " + user.getDisplayName());
		this.type=USER;
	}
	
	public void setDataSetMember(DataSetMember m) {
		this.datasetmember=m;
		objectid = new ObjectId(getDataSetMember()).toString();
		logger.debug("setItemUser -> " + objectid.toString()+  " " + m.getDisplayName());;
		this.type=DATASETMEMBER;
	}
	
	@Override
	public void add(com.novamens.dom.Object obj) {
		if (obj instanceof Content)
			setContent((Content) obj);
		else if (obj instanceof DataSetMember)
			setDataSetMember((DataSetMember) obj);
		else if (obj instanceof User)
			setItemUser((User) obj);
		else
			throw new KbeeRuntimeException(obj.getClass().getName() + "not supported, obj must be  be CONTENT, DATASETMEMBER, USER");
	}

	@Override
	public boolean holds(com.novamens.dom.Object obj) {
		if (this.type==UserListItem.CONTENT)
			return obj.getId().equals(getContent().getId());
		else if (this.type==UserListItem.DATASETMEMBER)
			return obj.getId().equals(getDataSetMember().getId());
		else if (this.type==UserListItem.USER)
			return obj.getId().equals(getItemUser().getId());
		return false;
	}
	
	@Override
	public String getObjectId() {
		if (objectid!=null)
			return objectid;
		if (this.type==UserListItem.CONTENT)
			objectid = new ObjectId(getContent()).toString();
		else if (this.type==UserListItem.DATASETMEMBER)
			objectid = new ObjectId(getDataSetMember()).toString();
		else if (this.type==UserListItem.USER)
			objectid = new ObjectId(getItemUser()).toString();
		else 
			throw new KbeeRuntimeException("type must be CONTENT, DATASETMEMBER, USER");
		return objectid;
	}
	
	@Override
	public Content getContent() {
		return this.content;
	}
	
	@Override
	public DataSetMember getDataSetMember() {
		return this.datasetmember;
	}
	
	@Override
	public Object getObject() {
		if (this.type==UserListItem.CONTENT)
			return content;
		else if (this.type==UserListItem.DATASETMEMBER)
			return datasetmember;
		else if (this.type==UserListItem.USER)
			return userItem;
		return null;
	}
	
	@Override
	public User getItemUser() {
		return this.userItem;
	}
	
	@Override
	public void setId(Serializable id) {
		this.id=(Long) id;
	}
	
	@Override
	public String getName() {
		return super.getDisplayName();
	}

	@Override
	public Serializable getId() {
		return id;
	}

	public void setConsole(String console) {
		this.console=console;
	}

	public Long getContentOid() {
		return this.oid;
	}
	
	@Override
	public User getOwner() {
		return this.owner;
	}
	
	public void setOwner(User owner) {
		this.owner=owner;
	}
	
	@Override
	public String getConsole() {
		return this.console;
	}

	@Override
	public Site getSite() {
		return this.site;
	}

	public void setSite(Site site2) {
		this.site=site2;
	}
}

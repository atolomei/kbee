package com.novamens.kbee.content.userlist;



import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.OneToMany;

import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.model.ObjectId;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.dom.Object;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;

/**
 * 
 * 
 * 
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "kb_user_list")
@DynamicInsert
public class KbeeUserList extends AbstractObject implements UserList {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUserList.class.getName());

	@Id 
	@SequenceGenerator(name = "content_sequencer", sequenceName = "objectid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_sequencer")
	@Column(name = "id")
	private Long id;

	@Column(name = "title")
	private String 	title;
	
	/** this is redundant information == items.size() */
	@Column(name = "total_items")
	private int 	total_items;
	
	@Column(name = "description")
	private String 	description;
	
	@Column(name = "console")
	private String 	console;
	
	@Column(name = "version_match")
	private int version_match=UserListItem.NEWEST;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade=CascadeType.DETACH, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "owner_id")
	private User owner;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=KbeeSite.class)
	@JoinColumn(name="site_id",   nullable=true)
	private Site site;

	
	/**
	 * UserList sets the userlist_id field in items
	 */
	// Relation entries are Deleted with the List (CascadeType.ALL)
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeUserListItem.class)
	@JoinColumn(name = "userlist_id", nullable=false, updatable=true, insertable=true)
	List<UserListItem> items = new ArrayList<UserListItem>();
	
	@Override
	public boolean belongs(com.novamens.dom.Object ob) {
		for (UserListItem item: getItems()) {
			if (item.holds(ob))
					return true;
		}
		return false;
	}
	
	
	public KbeeUserList(User user, String console) {
		this.owner=user;
		this.console=console.trim().toLowerCase();
	}
	

	public KbeeUserList(User user, String console, Site site) {
		this.owner=user;
		this.console=console.trim().toLowerCase();
		this.site=site;
	}

	
	
	@Override
	public String getDisplayName() {
		return getTitle();
	}
	
	@Override
	public User getOwner() {
		return this.owner;
	}
				
	public void removeAllItems() {
		items.clear();
		total_items = items.size();
	}
	
	@Override
	public void removeItem(UserListItem item) {
		
		items.remove(item);
		total_items = items.size();
		
		/**
		List<UserListItem> li=new ArrayList<UserListItem>();
		for (UserListItem r_item: getItems()) {
			if (!r_item.getId().equals(item.getId())) {
				li.add(r_item);
			}
		}
	
		items.clear();
		items.addAll(li);
		total_items = items.size();
		
		*/
		
	/**
		int index = 0;
		for (UserListItem r_item: getItems()) {
			if (r_item.getId().equals(item.getId())) {
				getItems().remove(index);
				r_item=null;
				total_items = items.size();
				break;
			}
			index++;
		}
		**/
	}
	
	public void  addItem(UserListItem item) {
		
		item.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		item.setLastModifiedUser(getSessionUser());
		item.setDomain(this.getDomain());
		
		
		((KbeeUserListItem) item).setSite(this.getSite());
		((KbeeUserListItem) item).setConsole(this.getConsole());
		
		((KbeeUserListItem) item).setOwner(this.getOwner());
		
		items.add(item);
		total_items = items.size();
	}
	
	public void  setItems(List<UserListItem> list) {
		this.items=list;
	}
		
		
	@Override
	public List<UserListItem> getItems() {
		return items;
	}
	
	
	public KbeeUserList(KbeeUserList src) {
		super(src);
		this.title=src.title;
		this.description=src.description;
		this.console=src.console;
	}

	
	public KbeeUserList() {
	}
	

	@Override
	public void setId(Serializable id) {
		this.id=(Long)id;
	}

	@Override
	public String getName() {
		return title;
	}

	@Override
	public Serializable getId() {
		return this.id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getConsole() {
		return console;
	}

	public void setConsole(String console) {
		this.console = console;
	}
	
	public void setTotalItems(int s) {
		this.total_items=s;
	}
	
	@Override
	public int getTotalItems() {
		return getItems().size();
		//return this.total_items;
	}

	@Override
	public void remove(Object object) {
		ObjectId oid=new ObjectId(object);
		int index = 0;
		
		for (UserListItem item: getItems()) {
			if (item.getObjectId().equals(oid.toString())) {
				logger.debug("remove -> " + oid.toString() + " | " + object.getDisplayName());
				getItems().remove(index);
				setTotalItems(getItems().size());
				break;
			}
			index++;
		}
	}

	
	@Override
	public void add(com.novamens.dom.Object object) {

		if (	getConsole().equals("mytasks") 	||
				getConsole().equals("monitor") 	||
				getConsole().equals("users") 	||
				getConsole().equals("pending"))
			add(object, UserListItem.NEWEST);
		
		else if (getConsole().equals("recycle"))
			add(object, UserListItem.SAVED_VERSION);
		else
			add(object, UserListItem.PUBLISHED);
	}
	
	

	@Override
	public void add(Object object, int versionMatch) {
	
		logger.debug("add -> " + new ObjectId(object).toString());
		
		KbeeUserListItem item = new KbeeUserListItem();
		
		item.add(object);
		item.setTitle(object.getDisplayName());
		item.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		item.setLastModifiedUser(getSessionUser());
		item.setDomain(this.getDomain());
		item.setVersionMatch(versionMatch);
		item.settUserlist(this);
		
		
		item.setSite(this.getSite());
		item.setConsole(this.getConsole());
		item.setOwner(this.getOwner());

		getItems().add(item);	
		setTotalItems(getItems().size());
	}

	
	public void setVersionMatch(int m) {
		this.version_match=m;
	}

	@Override
	public int getVersionMatch() {
		return this.version_match;
	}
	

	@Override
	public Site getSite() {
		return site;
	}

	
	@Override
	public void setSite(Site site) {
		this.site = site;
	}



}

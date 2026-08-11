package com.novamens.kbee.content.properties;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.time.OffsetDateTime;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.properties.ContentProperties;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;


/**
 * 
 * Esto se usa  ????
 * 
 * ARREGLAR ESTO CUANDO SE USE 
 * 
 * <p>String {@code contentProperties} 
 * should be updated before saving the object 
 * and if and only if the map has changed. For this reason the JPA annotations is placed on the getter method
 * </p>
 *  
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")
@Table(name = "ContentProperties")
@DynamicInsert
public class KbeeContentProperties implements ContentProperties {

 	private static final long serialVersionUID = 1L;
 
 	
	static private Logger logger = LogManager.getLogger(KbeeContentProperties.class.getName());

 	
	@Column(name = "lastmodifieddate")
 	private OffsetDateTime lastModifiedDate;

	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "lastModifiedUser")
	@Id
	private User lastModifiedUser;

	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "content_id", updatable=false)
	@Id
	private Content content;

	
	private String contentProperties = null;
	
	@Column(name = "contentproperties")
	public String getContentProperties() {
		if (changed) {
			contentProperties = getSerializedMap(map);
		}
		return contentProperties;
	}
	
	@Transient 
	Map<String, String> map = null;
	
	@Transient 
	boolean changed= false;
	
	
	/**
	 * There may be some bugs related to the encoding 
	 * 
	 * @param s
	 * @return
	 * 
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> getMapFromString(String s) {
		Map<String, String> m = null; 
		try
	      {
			 // InputStream stream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
			 InputStream stream = new ByteArrayInputStream(s.getBytes());
	         ObjectInputStream ois = new ObjectInputStream(stream);
	         m  = (Map<String, String>) ois.readObject();
	         ois.close();
	         stream.close();
	      }catch(IOException ioe) {
	    	  logger.error(ioe);
	         m =new HashMap<String, String>();
	      }catch(ClassNotFoundException c)
	      {
	         logger.error("Class not found ");
	         logger.error(c);
	         m =new HashMap<String, String>();
	      }
		return m;
	}

	 /**
	  * There may be some bugs related to the encoding
	  * 
	  * @param m
	  * @return
	  */
	private String getSerializedMap(Map<String, String> m) {
		String value = null;
		try
		 {
			OutputStream fos = new ByteArrayOutputStream(); 
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(m);
            value = fos.toString();
            changed=false;
            oos.close();
            fos.close();
		 }catch(IOException ioe)
		 {
			 logger.error(ioe);
		 }
		return value;
	 }


	private Map<String, String> getContetProperties() {
		if (map==null && contentProperties!=null) {
			map= getMapFromString(contentProperties);
		}
		else {
			map=new HashMap<String, String>();
		}
		return map;
	}

	
	@Override
	public void setProperty(String key, String value) {
		getContetProperties().put(key, value);
		changed= true;
	}

	@Override
	public String getProperty(String key) {
		return getContetProperties().get(key);
	}

	@Override
	public void removeProperty(String key) {
		getContetProperties().remove(key);
		changed= true;
	}
	
	
	@Override
	public User  getLastModifiedUser() {
		return  lastModifiedUser;
	}

	@Override
	public Content getContent() {
		return content;
	}

	@Override
	public void setLastModifiedOffsetDateTime(OffsetDateTime date) {
		this.lastModifiedDate=date;
	}
	
	
	@Override
	public void setLastModifiedUser(User user) {
		this.lastModifiedUser=user;
	}

	@Override
	public void setContent(Content content) {
		this.content=content;
	}

	@Override
	public OffsetDateTime getLastmodifiedOffsetDateTime() {
		return lastModifiedDate;
	}


}

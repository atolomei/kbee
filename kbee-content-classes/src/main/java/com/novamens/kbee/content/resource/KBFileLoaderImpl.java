package com.novamens.kbee.content.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.content.resource.KBFileLoader;
import com.novamens.content.resource.KBFileProxy;
import com.novamens.security.Identifiable;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "KB_FILE_LOADER")
public class KBFileLoaderImpl implements KBFileLoader, Identifiable {
	
	static private Logger logger = LogManager.getLogger(KBFileLoaderImpl.class.getName());
	
	@Id  
	@Column(name = "Id")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "javaclass")
	private String javaclass;
	
	transient KBFileLoader implementation;
	
	@Override
	public Serializable getId() {
		return id;
	}
	
	@Override
	public InputStream getInputStream(String url) throws IOException {
		return getImplementation().getInputStream(url);
	}
	
	@Override
	public InputStream getInputStream(KBFileProxy file) throws IOException {
		return getImplementation().getInputStream(file);
	}
	
	@Override
	public long getSize(String url) throws IOException {
		return getImplementation().getSize(url);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDisplayName() {
		return getName();
	}
	
	public String getJavaClass() {
		return javaclass;
	}

	public void setJavaClass(String classname) {
		this.javaclass=classname;
	}
	
	public KBFileLoader getImplementation() throws IOException {
		try {
			
			if (implementation!=null) {
				return implementation;
			}
			
			Class<?> javaclass = Class.forName(getJavaClass());
			
			Object instance = javaclass.newInstance(); 
			
			if (!(instance instanceof KBFileLoader))
				throw new InstantiationException();
			
			implementation = (KBFileLoader)instance; 
			
			return implementation;
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)  {
			if (logger.isDebugEnabled()) {
				logger.error("implementation error", e);
			}
			throw new IOException(e);
		}
	}
}

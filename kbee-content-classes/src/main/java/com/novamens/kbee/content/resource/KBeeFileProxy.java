package com.novamens.kbee.content.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.resource.KBFileLoader;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.dom.KBFSStorageType;
import com.novamens.kbfs.FileServerException;
import com.novamens.kbfs.LocalFileServerCache;
import com.novamens.service.ServiceLocator;

@Entity
@Table(name = "KB_FILE_PROXY")
public class KBeeFileProxy extends KBFileImpl implements com.novamens.content.resource.KBFileProxy {
	
	static private Logger logger = LogManager.getLogger(KBeeFileProxy.class.getName());
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KBFileLoaderImpl.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "FILE_LOADER", updatable=false)
	private KBFileLoader loader;
	
	@Column(name = "URL")
	private String proxyurl = null;
	
	@Column(name = "\"size\"")
	private long targetsize;
	
	@Transient
	private File filecache = null;
	
	/**
	 * StorageType external is because the file is currently stored externally
	 * Gateway true is for files that should never be stored locally.
	 * 
	 * Gateway false would be for files in "lazy" mode, ie downloaded from the external source and stored by KBFS on demand
	 * 
	 */
	public KBeeFileProxy() {
		setStorageType(KBFSStorageType.External);
		setGateway(true);
		setSize(-1);
	}
	
	public KBeeFileProxy(KBFileLoader loader, String url) {
		setUrl(url);
		setLoader(loader);
		setGateway(true);
		setStorageType(KBFSStorageType.External);
		setSize(-1);
	}

	@Override
	public String getName() {
		return super.getName();   
	}
	
	@Override
	public String getUrl() {
		return this.proxyurl;
	}
	
	@Override
	public void setUrl(String url) {
		this.proxyurl = url;
	}
	
	@Override
	public File getFile() throws IOException {
		String bucket = "proxy";
		String object = String.valueOf(getId());
		
		File file = null;
		
		LocalFileServerCache cache = getLocalFileServerCache(); 
		
		if (cache.containsKey(bucket, object)) {
			file = cache.get(bucket, object);
		}
		
		if (file == null) {
			cache.put(bucket, object, getInputStream(), getName());
			file =  cache.get(bucket, object);
		}
		
		return file;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return getLoader().getInputStream(this);
	}
	
	public KBFileLoader getLoader() {
		return loader;
	}
	
	public void setLoader(KBFileLoader loader) {
		this.loader = loader;
	}
	
	public boolean isFile() throws IOException {
		
		return true;
	}
	
	public long getTargetSize() {
		return targetsize;
	}
	
	
	public void setSize(long size) {
		this.targetsize=size;
		super.setSize(size);
	}
	
	@Override
	public boolean isBinaryFile() throws IOException {
		return true;
	}
	
	@Override
	public long getSize() {
		try {
			if (getTargetSize()<=0) {
				long size = getLoader().getSize(getUrl());
				setSize(size);
				getService(KBFSResourceService.class).update();
			}
			return super.getSize();
		}
		catch (FileServerException | IOException e) {
			if (logger.isDebugEnabled()) {
				logger.error("file proxy " + getUrl(), e);
			}
			logger.warn(e.getMessage());
			return 0;
		}
	}
	
	@Override
	public boolean isIndexable() {
		return true;
	}
	
	private LocalFileServerCache getLocalFileServerCache() {
		return ServiceLocator.getService(LocalFileServerCache.class);
	}

}

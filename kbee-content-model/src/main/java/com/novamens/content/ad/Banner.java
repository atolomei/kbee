package com.novamens.content.ad;

import java.util.List;

import com.novamens.content.base.Resource;
import com.novamens.content.resource.KBFile;

public interface Banner extends Ad {

	public String getBannerText();
	public void setBannerText(String text);
	
	public String getLink();
	public void setLink(String link); 

	public String getGA();
	public void setGA(String ga); 
	
	public boolean getExternal();
	public void setExternal(boolean external); 

	public void addFile(KBFile image);
	public void removeFile(KBFile image);
	public List<KBFile> getFiles();
	
	public KBFile getImage1();
	public KBFile getImage2();

	public Resource getResource(String name);
}

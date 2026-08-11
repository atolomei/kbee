package com.novamens.content.service;

 

import java.util.List;

import com.novamens.content.resource.KBFile;
import com.novamens.service.BusinessSystemService;


public interface UserImagesService extends BusinessSystemService {

	public KBFile getDefaultImage(String username);
	public void startUp();
	public boolean isInitialized();
	public void evict();
	public List<KBFile> getImages();
}

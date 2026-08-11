package com.novamens.portal6.model;

import com.novamens.content.base.Content;

public interface ViewBKContent extends ViewBK {

	 public void setContent(Content content);
	 public Content getContent();
	 
	 public boolean isResources();
	 public boolean isGalleryViewer();
	 
	 public void setResources(boolean b);
	 public void setGalleryViewer(boolean b);
	 
}

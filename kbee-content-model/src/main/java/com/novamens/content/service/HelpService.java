package com.novamens.content.service;

import com.novamens.service.BusinessSystemService;

public interface HelpService extends BusinessSystemService {
	
	public String getHelpUrl(String page_key);
	
}

package com.novamens.content.service;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentTemplate;
import com.novamens.service.ObjectService;
import com.novamens.workflow.Activity;

public interface TestService extends ObjectService {
	public Content getSampleMonitor();
	public Content getSample();
	public Activity getSampleActivity();
	public Content getSample(ContentTemplate template);
}
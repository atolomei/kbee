package com.novamens.content.qa;

import com.novamens.service.ObjectService;

public interface QAService extends ObjectService  {
	public QAControl eval();
	public void update();
}

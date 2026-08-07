package com.novamens.beans;

import java.util.Map;

import com.novamens.service.SystemService;

public interface BeansService extends SystemService {
	public Object getBean(String name);
	public boolean containsBean(String name);
	public Object getBean(String name, Object... args);
	public <T> Map<String, T> getBeansOfType(Class<T> type);
}

package com.novamens.content.service;

import java.io.Serializable;

import com.novamens.dom.Domain;

public interface DomainPreferences {

		public void setId(Long id);
		public Serializable getId();

		public Domain getDomain();
		public String getName();
		public void setName(String name);

		public String getPreference(String key);
		public void setPreference(String key, String value);

}

package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.model.Classificable;
import com.novamens.event.Event;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.service.ServiceLocator;

public abstract class KbeeEAbstractFieldModel<T> implements EFieldModel<T> {
	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean handle(Event event) {
		return false;
	}
	
	public T get(Object object) {
		return null;
	}
	
	public List<T> getValues(Object object) {
		return new ArrayList<T>();
	}
	 
	@Override
	public void set(Object object, List<T> resources) {
		// ERROR
	}
	
	public List<T> onEvent(Event event) {
		return null;
	}
	
	public EFormDataSource<T> getDataSource(Classificable object) {
		return null;
		
	}
	
	public String serialize(Classificable formobject, T object) {
		return null;
	}
	
	public T deserialize(Classificable formobject, String token) {
		return null;
		
	}
	
	@JsonIgnore
	public String getMetainfoMessage() {
		return null;
	}
	
	@Override
	@JsonIgnore
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		return null;
	}
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	
	//@JsonIgnore
	//public String getModelObjectName() {
	//	return getModelObjectName(Locale.getDefault());
	//}
	
	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		return this.getClass().getName();
		//ResourceBundle res = ResourceBundle.getBundle( KbeeEAttributeFieldModel.class.getName(), Locale.getDefault());
	//return res.getString("attribute");
	}

	
} 
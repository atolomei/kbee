package com.novamens.kbee.content.webapi.type.gson;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.novamens.kbee.dom.AbstractObject;

public class ObjectTypeAdapterFactory implements TypeAdapterFactory {
	
	private Type type;
	
	public ObjectTypeAdapterFactory(Type type) {
		this.type = type;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		if (this.type.getTypeName().contains(type.getRawType().getTypeName())) {
			return null;
		}
		Type[] types = (type.getRawType().getGenericInterfaces());
		for (int i = 0; i<types.length; i++) {
			Type t = types[i];
			if (this.type.getTypeName().contains(t.getTypeName()))
				return null;
			}
			return (AbstractObject.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new ObjectTypeAdapter(gson) : null);
		}
}


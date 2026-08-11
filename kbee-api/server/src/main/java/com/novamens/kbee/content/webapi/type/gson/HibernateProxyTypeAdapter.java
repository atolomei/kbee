package com.novamens.kbee.content.webapi.type.gson;

import java.io.IOException;

import com.novamens.kbee.content.webapi.type.UriHelper;
import org.hibernate.proxy.HibernateProxy;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.novamens.dom.Domain;
import com.novamens.kbee.dom.AbstractObject;

import kbee.api.model.ApiProxy;

public class HibernateProxyTypeAdapter extends TypeAdapter<HibernateProxy> {

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@Override
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			return (HibernateProxy.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new HibernateProxyTypeAdapter(gson) : null);
		}
	};
	
	private final Gson context;

	private HibernateProxyTypeAdapter(Gson context) {
		this.context = context;
	}

	@Override
	public HibernateProxy read(JsonReader in) throws IOException {
		throw new UnsupportedOperationException("Not supported");
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void write(JsonWriter out, HibernateProxy value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		
		// Retrieve the original (not proxy) class
		//Class<?> baseType = Hibernate.getClass(value);
		// Get the TypeAdapter of the original class, to delegate the serialization
		//TypeAdapter delegate = context.getAdapter(TypeToken.get(baseType));
		TypeAdapter delegate = context.getAdapter(TypeToken.get(ApiProxy.class));
		// Get a filled instance of the original class
		Object unproxiedValue = ((HibernateProxy) value).getHibernateLazyInitializer()
				.getImplementation();
		
		String displayName = "";
 		if (unproxiedValue instanceof AbstractObject)
			displayName = ((AbstractObject)unproxiedValue).getDisplayName();
		if (unproxiedValue instanceof Domain)
			displayName = ((Domain)unproxiedValue).getDisplayName();
		
		Object proxy = new ApiProxy(displayName, UriHelper.getUri(unproxiedValue));
		delegate.write(out, proxy);
	}
}


package com.novamens.kbee.content.webapi.type.gson;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.security.Principal;

import kbee.api.model.ApiProxy;

public class PrincipalTypeAdapter extends TypeAdapter<Principal> {

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@Override
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			return (Principal.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new PrincipalTypeAdapter(gson) : null);
		}
	};
	
	private final Gson context;

	public PrincipalTypeAdapter(Gson context) {
		this.context = context;
	}

	@Override
	public Principal read(JsonReader in) throws IOException {
		throw new UnsupportedOperationException("Not supported");
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void write(JsonWriter out, Principal value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		TypeAdapter delegate = context.getAdapter(TypeToken.get(ApiProxy.class));
		Object link = new ApiProxy(value.getDisplayName(), UriHelper.getUri(value));
		delegate.write(out, link);
	}
}


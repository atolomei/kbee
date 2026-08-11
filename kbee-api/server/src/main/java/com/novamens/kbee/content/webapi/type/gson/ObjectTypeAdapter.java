package com.novamens.kbee.content.webapi.type.gson;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.kbee.dom.AbstractObject;

import kbee.api.model.ApiProxy;

public class ObjectTypeAdapter extends TypeAdapter<AbstractObject> {

	private final Gson context;

	public ObjectTypeAdapter(Gson context) {
		this.context = context;
	}

	@Override
	public AbstractObject read(JsonReader in) throws IOException {
		throw new UnsupportedOperationException("Not supported");
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void write(JsonWriter out, AbstractObject value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}

		TypeAdapter delegate = context.getAdapter(TypeToken.get(ApiProxy.class));
		
		Object link = new ApiProxy(value.getDisplayName(), UriHelper.getUri(value));
		delegate.write(out, link);
	}
}
package com.novamens.kbee.content.webapi.type.gson;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.model.KbeeMemberClassification;
import com.novamens.kbee.content.webapi.type.UriHelper;

import kbee.api.model.ApiProxy;

public class MemberClassificationTypeAdapter extends TypeAdapter<KbeeMemberClassification> {

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@Override
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			return (KbeeMemberClassification.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new MemberClassificationTypeAdapter(gson) : null);
		}
	};
	
	private final Gson context;

	public MemberClassificationTypeAdapter(Gson context) {
		this.context = context;
	}

	@Override
	public KbeeMemberClassification read(JsonReader in) throws IOException {
		throw new UnsupportedOperationException("Not supported");
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void write(JsonWriter out, KbeeMemberClassification value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		DataSetMember o = value.getDataSetMember();
		ApiProxy proxy = new ApiProxy(o.getDisplayName(), UriHelper.getUri(o));
		TypeAdapter delegate = context.getAdapter(TypeToken.get(ApiProxy.class));
		delegate.write(out, proxy);
	}
}


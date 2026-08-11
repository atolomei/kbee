package com.novamens.kbee.content.webapi.type.gson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class OffsetDateTimeAdapter extends TypeAdapter<OffsetDateTime> {

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@Override
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			return (OffsetDateTime.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new OffsetDateTimeAdapter(gson) : null);
		}
	};
	
	private final Gson context;

	public OffsetDateTimeAdapter(Gson context) {
		this.context = context;
	}

	@Override
	public OffsetDateTime read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
	        in.nextNull();
	        return null;
	      }
	      try {
	  		String value =in.nextString();
			LocalDateTime local  = LocalDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			
			OffsetDateTime date = OffsetDateTime.of(local, OffsetDateTime.now().getOffset());

					return date;
	      } 
	      catch (DateTimeParseException e) {
	        
	    	  throw new JsonSyntaxException(e);
	      }
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void write(JsonWriter out, OffsetDateTime value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}

		TypeAdapter delegate = context.getAdapter(TypeToken.get(String.class));
		
		DateTimeFormatter dateformat = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		String label = dateformat.format(value);
		
		delegate.write(out, label);
	}
}


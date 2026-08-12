package com.novamens.indexer.java;

import java.util.List;

import com.novamens.content.model.ObjectId;
import com.novamens.indexer.service.Document;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.indexer.service.IndexerException;
import com.novamens.security.Auditable;

public class DocumentBuilder {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DocumentBuilder.class.getName());

	
	private DocumentSchema schema;
		
	public DocumentBuilder(DocumentSchema schema) {
		this.schema = schema;
	}
	
	public Document build(Object object) throws IndexerException {
		return build(object, true);
	}
	
	public Document build(Object object, boolean metainfo) throws IndexerException {
		IndexerDocument document = new IndexerDocument();
		for(FieldSchema fieldSchema : getSchema().getFieldsSchemas()) {
			if (!metainfo || fieldSchema.isMetainfo()) {
				Extractor extractor = fieldSchema.getExtractor();
				Object value = extractor.extract(object);
				if (value!=null)
					if (value instanceof List) {
						List<?> values = (List<?>)value;
						if (values.size()==1)
							value = values.get(0);
						if (fieldSchema.isId()) 
							document.setId((new ObjectId(object)).toString());
						else
							document.addField(fieldSchema.getFieldName(), value);
					}
					else {
						if (fieldSchema.isId()) 
							document.setId((new ObjectId(object)).toString());
						else
							document.addField(fieldSchema.getFieldName(), value);
					}
			}
			//else {
			//	logger.debug("out");
			//}
		}
		
		if (object instanceof Auditable)
			document.setLastModifiedOffsetDateTime(((Auditable)object).getLastModifiedOffsetDateTime());
		
		return document;
	}
	
	public Document build(Object object, String...field) throws IndexerException {
		IndexerDocument document = new IndexerDocument();
		
		for(FieldSchema fieldSchema : getSchema().getFieldsSchemas()) {
			
			boolean build = false;
			
			for (int i=0; i<field.length; i++) {
				if (field[i].equals(fieldSchema.getFieldName())) {
					build = true;
					break;
				}
			}
			
			if (build || fieldSchema.isId()) {
				Extractor extractor = fieldSchema.getExtractor();
				Object value = extractor.extract(object);
				if (value!=null)
					if (value instanceof List) {
						List<?> values = (List<?>)value;
						if (values.size()==1)
							value = values.get(0);
						if (fieldSchema.isId()) 
							document.setId((new ObjectId(object)).toString());
						else
							document.addField(fieldSchema.getFieldName(), value);
					}
					else {
						if (fieldSchema.isId()) 
							document.setId((new ObjectId(object)).toString());
						else
							document.addField(fieldSchema.getFieldName(), value);
					}
			}
		}
		
		if (object instanceof Auditable)
			document.setLastModifiedOffsetDateTime(((Auditable)object).getLastModifiedOffsetDateTime());
		
		return document;
	}
	
	public DocumentSchema getSchema() {
		return schema;
	}
}

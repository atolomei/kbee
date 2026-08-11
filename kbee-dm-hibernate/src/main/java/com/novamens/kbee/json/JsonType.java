package com.novamens.kbee.json;


import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import com.codesnippets4all.json.generators.JSONGenerator;
import com.codesnippets4all.json.generators.JsonGeneratorFactory;
import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.novamens.dom.Json;
 

public class JsonType implements UserType {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(JsonType.class.getName());
	

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable)value;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		return x == y;
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x == null ? 0 : x.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	
	
	
	@SuppressWarnings("rawtypes")
	public Object nullSafeGet(ResultSet rs, String[] names,  SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
		String stringvalue = rs.getString(names[0]);
		
		if(rs.wasNull()) {
			return new KbeeJson();
		}
		
		if(stringvalue == null) {
			return new KbeeJson();
		}
//		JsonParserFactory factory = JsonParserFactory.getInstance();
//		JSONParser parser = factory.newJsonParser();

		try {
//			Map roots = parser.parseJson(stringvalue);
//			List root = (List)roots.get("root");
//			Map jsonData = (Map)root.get(0);
//			return new KbeeJson(jsonData);
			
			return new KbeeJson(stringvalue);
			
			
		} 
		catch (Exception e) {
			logger.error(e, "JSON is broken. resetting to a new empty KbeeJson");
			if(stringvalue != null) 
				logger.error(stringvalue);
			return new KbeeJson();
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
		if (value == null || ((KbeeJson)value).getData().isEmpty()) {
			st.setNull(index, Types.VARCHAR);
		} 
		else {
			//JsonGeneratorFactory factory = JsonGeneratorFactory.getInstance();
			//JSONGenerator generator = factory.newJsonGenerator();
			//String stringvalue = generator.generateJson(((KbeeJson)value).getData());
			String stringvalue = ((KbeeJson)value).toString();
			st.setString(index, stringvalue);
		}
	}
 
	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}
	
	
	@Override
	public Class<Json> returnedClass() {
		return Json.class;
	}
 
	@Override
	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}


}

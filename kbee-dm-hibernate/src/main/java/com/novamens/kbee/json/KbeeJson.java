package com.novamens.kbee.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.novamens.dom.Json;
import com.novamens.util.KbeeRuntimeException;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.fasterxml.jackson.core.type.TypeReference;

public class KbeeJson implements Json, Serializable {
	private static final long serialVersionUID = 1L;

	static private ObjectMapper mapper = new ObjectMapper();
	
	static  {
		//smapper.registerModule(new JavaTimeModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	private Map<String, Object> map;

    public KbeeJson() {
        map=new HashMap<>();
    }
    public KbeeJson(String json) {
        try {
            if (json.trim().startsWith("[")) {
                List<Map<String, Object>> tmp = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
                });
                map = tmp.get(0);
            } else {
                map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
                });
            }
        } 
        catch (IOException e) {
        	//logger.error(e);
        	parse(json);
        }
    }
    
	private void parse(String string) {
		
		JsonParserFactory factory=JsonParserFactory.getInstance();
		JSONParser parser=factory.newJsonParser();
		
		Map roots = parser.parseJson(string);

		try {
		
			List root = (List)roots.get("root");

			if (root!=null)
				this.map = (Map)root.get(0);
			else
				this.map = roots;		
		
		} 
		catch (Exception e) {

            throw new KbeeRuntimeException(e);
		
		}
	}

    public KbeeJson(Map map) {
        this.map = map;
    }

    public String getString(String name) {
        Object o = get(name);
        if (o == null)
            return null;
        return o.toString();
    }

    @Override
    public String getString(String key, String defaultValue) {
        Object o = get(key);
        if (o == null)
            return defaultValue;
        return o.toString();
    }


    public Object get(String name) {
        String path[] = name.split("/");
        Map map = this.map;
        for (int i = 0; i < path.length - 1; i++) {
            Object submap = map.get(path[i]);
            if (submap == null || !(submap instanceof Map)) {
                return null;
            } else {
                map = (Map) submap;
            }
        }
        Object o = map.get(path[path.length - 1]);
        return o;
    }

    public void remove(String name) {
        String path[] = name.split("/");
        Map map = this.map;
        for (int i = 0; i < path.length - 1; i++) {
            Object submap = map.get(path[i]);
            if (submap == null || !(submap instanceof Map)) {
                return;
            } else {
                map = (Map) submap;
            }
        }
        map.remove(path[path.length - 1]);
    }

    @SuppressWarnings("unchecked")
    public List<String> getValues(String name) {
        Object value = map.get(name);
        List<String> list = new ArrayList<String>();
        if (value != null && value instanceof List<?>)
            list = (List<String>) value;
        else if (value != null)
            list.add((String) value);
        return list;
    }

    private String unEscape(String s) {
        if (s == null)
            return null;
        return s.replace("\\'", "\"");
    }

    private String escape(String s) {
        if (s == null)
            return null;
        return s.replace("\"", "\\'");
    }

    public Map getData() {
        return map;
    }

    @SuppressWarnings("unchecked")
    public void put(String key, String value) {
        String path[] = key.split("/");
        Map map = this.map;
        for (int i = 0; i < path.length - 1; i++) {
            Object submap = map.get(path[i]);
            if (submap == null) {
                submap = new HashMap();
                map.put(path[i], submap);
                map = (Map) submap;
            } else {
                map = (Map) submap;
            }
        }
        map.put(path[path.length - 1], value);
    }

    public void put(String key, List<?> values) {
        map.put(key, values);
    }

    public Set<String> keySet() {
        return (Set<String>) map.keySet();
    }

    public void put(String key, Map<String, String> values) {
        map.put(key, values);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this.map);
        } catch (Exception e) {
            throw new KbeeRuntimeException("Cannot parse specified json.");
        }
    }
}

package com.novamens.solr.sync;

import org.noggit.JSONParser;
import org.noggit.ObjectBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class SolrConfigReader {

    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrConfigReader.class.getName());

    private InputStream jsonConfigStream;
    private Map schemaMap;


    public SolrConfigReader(InputStream jsonConfigStream) {
        this.jsonConfigStream = jsonConfigStream;
    }

    public List<Map<String, Object>> getFields() throws IOException {
        Map schemaMap = getSchema();
        List<Map<String, Object>> fields = null;
        if( schemaMap != null)
                fields = (List<Map<String, Object>>) schemaMap.get("fields");
        return fields;
    }

    public List<Map<String, Object>> getFieldTypes() throws IOException {
        Map schemaMap = getSchema();
        List<Map<String, Object>> fields = null;
        if( schemaMap != null)
            fields = (List<Map<String, Object>>) schemaMap.get("fieldTypes");
        return fields;
    }

    public List<Map<String, Object>> getCopyFields() throws IOException {
        Map schemaMap = getSchema();
        List<Map<String, Object>> fields = null;
        if( schemaMap != null)
            fields = (List<Map<String, Object>>) schemaMap.get("copyFields");
        return fields;
    }

    private Map<String, Object> getSchema() throws IOException {
        if(this.schemaMap == null) {
            ObjectBuilder builder = new ObjectBuilder(new JSONParser(new InputStreamReader(jsonConfigStream, "UTF-8")));
            Map rootMap = (Map) builder.getObject();
            schemaMap = (Map) rootMap.get("schema");
            if(schemaMap == null)
                throw new RuntimeException("'schema' entry not found in json file");
        }
        return schemaMap;
    }




}

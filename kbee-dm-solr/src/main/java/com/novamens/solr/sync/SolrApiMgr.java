package com.novamens.solr.sync;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SolrApiMgr {

    SolrClient solrClient;

    public SolrApiMgr(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public SchemaResponse.UpdateResponse processRequests(
            Operation operation,
            Map<String, Object> entry)
            throws IOException, SolrServerException {

        try {
            SchemaResponse.UpdateResponse result =
                    new NamedListSchemaRequest(operation, entry)
                            .process(solrClient);

            if (result.getStatus() != 0) {
                throw new RuntimeException(
                        "Schema operation " + operation.getRestMethod()
                        + " failed: " + result.getException()
                );
            }

            return result;

        } catch (SolrException e) {

            String msg =
                    "Error executing Solr schema operation: "
                    + operation.getRestMethod()
                    + "\nHTTP status: " + e.code()
                    + "\nMessage: " + e.getMessage();

            throw new RuntimeException(msg, e);
        }
    }

    public List<Map<String, Object>> getFields() throws IOException, SolrServerException {
        SchemaResponse.FieldsResponse response = new SchemaRequest.Fields().process(solrClient);
        return response.getFields();
    }

    public List<Map<String, Object>> getFieldTypes()
            throws IOException, SolrServerException {

        SchemaResponse.FieldTypesResponse response =
                new SchemaRequest.FieldTypes().process(solrClient);

        return response.getFieldTypes().stream()
                .map(fieldType -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("name", fieldType.getAttributes().get("name"));
                    map.putAll(fieldType.getAttributes());
                    map.put("analyzer", fieldType.getAnalyzer());
                    map.put("indexAnalyzer", fieldType.getIndexAnalyzer());
                    map.put("queryAnalyzer", fieldType.getQueryAnalyzer());
                    map.put("similarity", fieldType.getSimilarity());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getCopyField()
            throws IOException, SolrServerException {

        SchemaResponse.CopyFieldsResponse response =
                new SchemaRequest.CopyFields().process(solrClient);

        return response.getCopyFields();
    }


    public enum Operation {
        AddField("add-field"),
        DeleteField("delete-field"),
        ReplaceField("replace-field"),
        AddDynamicField("add-dynamic-field"),
        DeleteDynamicField("delete-dynamic-field"),
        ReplaceDynamicField("replace-dynamic-field"),
        AddFieldType("add-field-type"),
        DeleteFieldType("delete-field-type"),
        ReplaceFieldType("replace-field-type"),
        AddCopyField("add-copy-field"),
        DeleteCopyField("delete-copy-field");

        private String operation;

        Operation(String operation) {
            this.operation = operation;
        }

        public String getRestMethod() {
            return operation;
        }
    }

    private class NamedListSchemaRequest extends SchemaRequest.Update {
        private NamedList<Object> namedListWithOp;

        public NamedListSchemaRequest(Operation operation, Map<String, Object> namedList) {
            namedListWithOp = new NamedList<>();

            if (operation != Operation.DeleteField && operation != Operation.DeleteFieldType)
                namedListWithOp.add(operation.getRestMethod(), namedList);
            else {
                NamedList onlyNameList = new NamedList();
                onlyNameList.add("name", namedList.get("name"));
                namedListWithOp.add(operation.getRestMethod(), onlyNameList);
            }
        }

        @Override
        protected NamedList<Object> getRequestParameters() {
            return namedListWithOp;
        }
    }



}

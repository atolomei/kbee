package com.novamens.solr.sync;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SolrApiMgr {

    SolrClient solrClient;

    public SolrApiMgr(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public SchemaResponse.UpdateResponse processRequests(Operation operation, Map<String, Object> entry) throws IOException, SolrServerException {
        NamedList namedListEntry = SolrConfigDifHelper.toNamedList(entry);
        SchemaResponse.UpdateResponse result;
        try {

            result = new NamedListSchemaRequest(operation, entry).process(solrClient);
            if (result.getStatus() != 0) {
                throw new RuntimeException(result.getException());
            }
        } catch (HttpSolrClient.RemoteExecutionException e) {
            String msg = "Exception occurred while processing msg:";
            NamedList metaData = e.getMetaData();

            if (metaData != null) {
                try {
                    List errorDetails = (List) metaData.findRecursive("error", "details");
                    if (errorDetails.size() > 0) {
                        Object errorMessages = ((Map) errorDetails.get(0)).get("errorMessages");
                        msg += "\n*******************";
                        msg += "\n" + errorMessages;
                        msg += "\n*******************";
                    }
                } catch (Exception e1) {
                }

                msg += "\n" + metaData.toString();
            }
            throw new RuntimeException(msg, e);
        }
        return result;
    }

    public List<Map<String, Object>> getFields() throws IOException, SolrServerException {
        SchemaResponse.FieldsResponse response = new SchemaRequest.Fields().process(solrClient);
        return response.getFields();
    }

    public List<Map<String, Object>> getFieldTypes() throws IOException, SolrServerException {
        SchemaResponse.FieldTypesResponse response = new SchemaRequest.FieldTypes().process(solrClient);
        return (List<Map<String, Object>>) response.getResponse().toMap(new LinkedHashMap<>()).get("fieldTypes");
    }

    public List<Map<String, Object>> getCopyField() throws IOException, SolrServerException {
        SchemaResponse.CopyFieldsResponse response = new SchemaRequest.CopyFields().process(solrClient);
        return (List<Map<String, Object>>) response.getResponse().toMap(new LinkedHashMap<>()).get("copyFields");
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

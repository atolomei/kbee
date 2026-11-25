package com.novamens.solr.sync;

import org.apache.solr.client.solrj.RemoteSolrException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.response.schema.SchemaResponse;

import kbee.util.PropertiesFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class SolrSchemaSynchronizer {

    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrSchemaSynchronizer.class.getName());
    InputStream jsonConfigStream;
    String SolrCoreUrl;



    public SolrSchemaSynchronizer(InputStream jsonConfigStream, String solrCoreUrl) {
        this.jsonConfigStream = jsonConfigStream;
        SolrCoreUrl = solrCoreUrl;
    }

    public SchemaDiffOperations calculateOperations() throws IOException, SolrServerException {
        SolrClient client = null;
        SchemaDiffOperations diffOperations = new SchemaDiffOperations();
        try {
            client = getSolrClient();
            SolrApiMgr solrApiMgr = new SolrApiMgr(client);
            SolrConfigReader solrConfigReader = new SolrConfigReader(jsonConfigStream);

            //fieldTypes
            List<Map<String, Object>> fieldTypesCfg = solrConfigReader.getFieldTypes();
            List<Map<String, Object>> fieldTypesApi = solrApiMgr.getFieldTypes();
            diffOperations.setAddedFieldTypes(SolrConfigDifHelper.getAddedEntriesMatchingKey("name", fieldTypesCfg, fieldTypesApi));
            diffOperations.setDeletedFieldTypes(SolrConfigDifHelper.getAddedEntriesMatchingKey("name", fieldTypesApi, fieldTypesCfg));
            diffOperations.setReplacedFieldTypes(SolrConfigDifHelper.getModifiedOrgEntries("name", fieldTypesCfg, fieldTypesApi));

            //fields
            List<Map<String, Object>> fieldsCfg = solrConfigReader.getFields();
            List<Map<String, Object>> fieldsApi = solrApiMgr.getFields();
            diffOperations.setAddedFields(SolrConfigDifHelper.getAddedEntriesMatchingKey("name", fieldsCfg, fieldsApi));
            diffOperations.setDeletedFields(SolrConfigDifHelper.getAddedEntriesMatchingKey("name", fieldsApi, fieldsCfg));
            diffOperations.setReplacedFields(SolrConfigDifHelper.getModifiedOrgEntries("name", fieldsCfg, fieldsApi));

            //copyFields
            List<Map<String, Object>> copyFieldsCfg = solrConfigReader.getCopyFields();
            List<Map<String, Object>> copyFieldsApi = solrApiMgr.getCopyField();
            diffOperations.setAddedCopyFields(SolrConfigDifHelper.getAddedEntriesMatchingFull(copyFieldsCfg, copyFieldsApi));
            diffOperations.setDeletedCopyFields(SolrConfigDifHelper.getAddedEntriesMatchingFull(copyFieldsApi, copyFieldsCfg));

            return diffOperations;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
    
    
    public SolrClient getSolrClient()  {
    	
        String username = PropertiesFactory
            .getInstance("kbee")
            .getProperties()
            .getProperty("solr.username", null);

        String password = PropertiesFactory
        	.getInstance("kbee")
        	.getProperties()
        	.getProperty("solr.password", null);
        
        String baseUrl = SolrCoreUrl.substring(
                0, SolrCoreUrl.lastIndexOf("/")
        );
        
        String coreName = SolrCoreUrl
        	.substring(SolrCoreUrl
        	.lastIndexOf("/")+1);

        HttpJdkSolrClient.Builder builder =
        	new HttpJdkSolrClient.Builder(baseUrl)
        		.withDefaultCollection(coreName)
        		.useHttp1_1(true);
        
        if (username != null && !username.isBlank()
                && password != null && !password.isBlank()) {

            builder.withBasicAuthCredentials(username, password);
        }
        
        SolrClient client = builder.build();
        
        return client;
    }
  
    public void printOperations(SchemaDiffOperations operations, String schemaname) {

        logger.info("********************************************");
        logger.info("*********** SOLR SCHEMA CHANGES ************");
        logger.info("**** URL= " + schemaname + "****");
        logger.info("********************************************");
        if (operations.hasOperations()) {
            //***********        ADD        *********************
            printEntries(operations.getAddedFieldTypes(), "FieldTypes to add: ");
            printEntries(operations.getAddedFields(), "Field to add: ");
            printEntries(operations.getAddedCopyFields(), "CopyFields to add: ");
            //***********        MODIFY        *********************
            printEntries(operations.getReplacedFieldTypes(), "FieldTypes to modify: ");
            printEntries(operations.getReplacedFields(), "Field to modify: ");
            //***********        REMOVE        *********************
            printEntries(operations.getDeletedCopyFields(), "CopyFields to delete: ");
            printEntries(operations.getDeletedFields(), "Field to remove: ");
            printEntries(operations.getDeletedFieldTypes(), "FieldTypes to delete: ");
        }else{
            logger.info("No pending changes.");
        }
        logger.info("");
        logger.info("********************************************");

    }

    public void synchSchema(SchemaDiffOperations operations) throws IOException, SolrServerException {

        SolrClient client = null;

        try {
            client = getSolrClient();
            SolrApiMgr solrApiMgr = new SolrApiMgr(client);
            //***********        ADD        *********************
            //add fieldTypes -> fields -> copyFields
            processRequests(SolrApiMgr.Operation.AddFieldType, operations.getAddedFieldTypes(), solrApiMgr);
            processRequests(SolrApiMgr.Operation.AddField, operations.getAddedFields(), solrApiMgr);
            processRequests(SolrApiMgr.Operation.AddCopyField, operations.getAddedCopyFields(), solrApiMgr);

            //***********        MODIFY        *********************
            //modify fieldTypes -> fields -> copyFields
            processRequests(SolrApiMgr.Operation.ReplaceFieldType, operations.getReplacedFieldTypes(), solrApiMgr);
            processRequests(SolrApiMgr.Operation.ReplaceField, operations.getReplacedFields(), solrApiMgr);

            //***********        REMOVE        *********************
            //delete copyFields -> fields -> fieldTypes
            processRequests(SolrApiMgr.Operation.DeleteCopyField, operations.getDeletedCopyFields(), solrApiMgr);
            processRequests(SolrApiMgr.Operation.DeleteField, operations.getDeletedFields(), solrApiMgr);
            processRequests(SolrApiMgr.Operation.DeleteFieldType, operations.getDeletedFieldTypes(), solrApiMgr);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }


    private static void processRequests(
            SolrApiMgr.Operation operation,
            List<Map<String, Object>> entries,
            SolrApiMgr solrApiMgr)
            throws IOException, SolrServerException {

        for (Map<String, Object> entry : entries) {

            try {
                printEntries(entries,
                    "About to perform operation: " + operation.getRestMethod());

                SchemaResponse.UpdateResponse result =
                    solrApiMgr.processRequests(operation, entry);

                if (result.getStatus() != 0) {
                    throw new RuntimeException(result.getException());
                }

            } catch (RemoteSolrException e) {

                String msg = "Exception occurred while processing request:"
                        + "\nHTTP status: " + e.code()
                        + "\n" + e.getMessage();

                throw new RuntimeException(msg, e);
            }
        }
    }


    private static void printEntries(List<Map<String, Object>> fieldTypesApi, String message) {

        if (fieldTypesApi.size() > 0) {
            logger.info("\n" + message);
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> added : fieldTypesApi) {
                sb.append(SolrConfigDifHelper.getEntryDesc(3, added));
            }
            logger.info(sb.toString());
        }
    }

    public class SchemaDiffOperations {
        //fieldTypes
        private List<Map<String, Object>> addedFieldTypes;
        private List<Map<String, Object>> deletedFieldTypes;
        private List<Map<String, Object>> replacedFieldTypes;

        //fields
        private List<Map<String, Object>> addedFields;
        private List<Map<String, Object>> deletedFields;
        private List<Map<String, Object>> replacedFields;

        //copyFields
        private List<Map<String, Object>> addedCopyFields;
        private List<Map<String, Object>> deletedCopyFields;


        public SchemaDiffOperations() {
        }

        public List<Map<String, Object>> getAddedFieldTypes() {
            return addedFieldTypes;
        }

        public void setAddedFieldTypes(List<Map<String, Object>> addedFieldTypes) {
            this.addedFieldTypes = addedFieldTypes;
        }

        public List<Map<String, Object>> getDeletedFieldTypes() {
            return deletedFieldTypes;
        }

        public void setDeletedFieldTypes(List<Map<String, Object>> deletedFieldTypes) {
            this.deletedFieldTypes = deletedFieldTypes;
        }

        public List<Map<String, Object>> getReplacedFieldTypes() {
            return replacedFieldTypes;
        }

        public void setReplacedFieldTypes(List<Map<String, Object>> replacedFieldTypes) {
            this.replacedFieldTypes = replacedFieldTypes;
        }

        public List<Map<String, Object>> getAddedFields() {
            return addedFields;
        }

        public void setAddedFields(List<Map<String, Object>> addedFields) {
            this.addedFields = addedFields;
        }

        public List<Map<String, Object>> getDeletedFields() {
            return deletedFields;
        }

        public void setDeletedFields(List<Map<String, Object>> deletedFields) {
            this.deletedFields = deletedFields;
        }

        public List<Map<String, Object>> getReplacedFields() {
            return replacedFields;
        }

        public void setReplacedFields(List<Map<String, Object>> replacedFields) {
            this.replacedFields = replacedFields;
        }

        public List<Map<String, Object>> getAddedCopyFields() {
            return addedCopyFields;
        }

        public void setAddedCopyFields(List<Map<String, Object>> addedCopyFields) {
            this.addedCopyFields = addedCopyFields;
        }

        public List<Map<String, Object>> getDeletedCopyFields() {
            return deletedCopyFields;
        }

        public void setDeletedCopyFields(List<Map<String, Object>> deletedCopyFields) {
            this.deletedCopyFields = deletedCopyFields;
        }


        public boolean hasOperations() {
            return addedFieldTypes.size() > 0 ||
                    deletedFieldTypes.size() > 0 ||
                    replacedFieldTypes.size() > 0 ||
                    addedFields.size() > 0 ||
                    deletedFields.size() > 0 ||
                    replacedFields.size() > 0 ||
                    addedCopyFields.size() > 0 ||
                    deletedCopyFields.size() > 0;
        }
    }

}

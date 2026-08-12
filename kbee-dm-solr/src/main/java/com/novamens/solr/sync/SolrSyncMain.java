package com.novamens.solr.sync;

import org.springframework.util.ResourceUtils;

import java.io.*;

public class SolrSyncMain {

    static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrSyncMain.class.getName());

    public static void main(String[] args) {


        SolrSchemaSynchronizerAppParams appParams=null;
        try {
            appParams = getParameterValue(args);
            appParams.validate();
        }catch (Exception e) {
            logger.error(e.getMessage());
            printUsage();
            System.exit(1);
        }

        //String path = "C:\\novamens\\soft\\solr-8.0.0\\server\\solr\\test_core\\conf\\schema.json";
        //String baseSolrUrl = "http://localhost:8983/solr/test_core2";
        try {
            File jsonFile = ResourceUtils.getFile(appParams.getJsonConfigPath());
            try (InputStream jsonStream = new FileInputStream(jsonFile)) {
                SolrSchemaSynchronizer schemaSynchronizer = new SolrSchemaSynchronizer(jsonStream, appParams.getSolrCoreUrl());
                SolrSchemaSynchronizer.SchemaDiffOperations diffOperations = schemaSynchronizer.calculateOperations();
                schemaSynchronizer.printOperations(diffOperations, appParams.getSolrCoreUrl());
                if (appParams.getOperation() == Operation.synchronize) {
                    schemaSynchronizer.synchSchema(diffOperations);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }


    private static void printUsage() {
        logger.error("Solr Synchronizer usage:");
        logger.error("      (cmd) [options] operation");
        logger.error("   Options");
        logger.error("      --solrCoreUrl : solr core http url");
        logger.error("      --jsonConfigPath : path to solr schema file in json format");
        logger.error("   Operations");
        logger.error("       printChanges : print pending changes to apply");
        logger.error("       synchronize : execute changes to synchronize solr core instance");
        logger.error("   Example");
        logger.error("       (cmd) --solrCoreUrl \"http://localhost:8983/solr/test_core2\" --jsonConfigPath \"./schema.json\" synchronize");
    }

    private static class SolrSchemaSynchronizerAppParams {
        private String jsonConfigPath = null;
        private String solrCoreUrl = null;
        private Operation operation = null;

        public String getJsonConfigPath() {
            return jsonConfigPath;
        }

        public void setJsonConfigPath(String jsonConfigPath) {
            this.jsonConfigPath = jsonConfigPath;
        }

        public String getSolrCoreUrl() {
            return solrCoreUrl;
        }

        public void setSolrCoreUrl(String solrCoreUrl) {
            this.solrCoreUrl = solrCoreUrl;
        }

        public Operation getOperation() {
            return operation;
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        public void validate() {
            if(jsonConfigPath == null){
                throw new RuntimeException("jsonConfigPath not specified");
            }
            if(solrCoreUrl == null){
                throw new RuntimeException("solrCoreUrl not specified");
            }
            if(operation == null){
                throw new RuntimeException("operation not specified");
            }
        }
    }

    private static SolrSchemaSynchronizerAppParams getParameterValue(String[] args) {
        SolrSchemaSynchronizerAppParams params = new SolrSchemaSynchronizerAppParams();
        for (int idx = 0; idx < args.length; idx++) {
            if (idx == args.length - 1) {
                Operation operation = Operation.fromString(args[idx]);
                if (operation == null) {
                    throw new RuntimeException("Invalid operation");
                }
                params.setOperation(operation);
            } else {
                switch (args[idx]) {
                    case "--jsonConfigPath":
                        if (++idx >= args.length) {
                            throw new RuntimeException("Missing jsonConfigPath value");
                        }
                        params.setJsonConfigPath(args[idx]);
                        break;
                    case "--solrCoreUrl":
                        if (++idx >= args.length) {
                            throw new RuntimeException("Missing solrCoreUrl value");
                        }
                        params.setSolrCoreUrl(args[idx]);
                        break;

                }
            }
        }

        return params;
    }

    private enum Operation {
        synchronize("synchronize"),
        printChanges("printChanges");

        private String operation;

        Operation(String operation) {
            this.operation = operation;
        }

        public String getOperation() {
            return operation;
        }

        public static Operation fromString(String text) {
            for (Operation b : Operation.values()) {
                if (b.operation.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

}

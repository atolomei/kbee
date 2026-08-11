package com.novamens.kbee.content.command;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.*;
import com.novamens.dom.Domain;
import com.novamens.hibernate.session.Session;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.query.DataSetQuery;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.logging.DataSetValueUpdateEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.util.KbeeRuntimeException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ReclassifyMemberCommand extends AsyncCommand {

    static private Logger txLogger = LogManager.getLogger("TxLogger");

    boolean success = true;
    Long usrId = null;
    List<DateSetMemberTagModifier> dataSetTagModifiers;

    long processedItems;
    long totalItems;
    long dataSetID;

    /**
     *
     * There are modes of operation
     * IQL Sentence -> ResultSet
     * List<DataSetMember> ->
     *
     */
    // List of Content
    private List<DataSetMember> list;
    private Iterator<DataSetMember> it;

    public void setSelection(List<DataSetMember> list) {
    	this.list=list;
    }
    public List<DataSetMember> getSelection() {
    	return this.list;
    }

    private List<Long> list_id = new ArrayList<Long>();

    ResultSet searchResults;

    public ReclassifyMemberCommand(Long dataSetID, List<DateSetMemberTagModifier> dataSetTagModifiers, Long domainID, String iqlExpression) {
        setName(this.getClass().getSimpleName());
        setDescription("Reclassify all dataset Members from a dataset given by a iql expression with the selected classifier/attribute value.");
        setDataSetMemberTagModifiers(dataSetTagModifiers);
        setDomainId(domainID);
        setIqlExpression(iqlExpression);
        setDataSetID(dataSetID);
    }


    private boolean hasNext() {
		if (searchResults!=null)
			return searchResults.hasNext();
		if (it!=null)
			return it.hasNext();
		return false;
    }


    private DataSetMember getNext() {
		if (searchResults!=null) {
		    final SearchResult next = searchResults.next();
		    final DataSetMember contentOrg = (DataSetMember) next.getObject();
		    return contentOrg;
		} else {
			DataSetMember contentOrg = getContentDao().findMemberById(it.next().getId());
			return contentOrg;
		}
	}

    @Override
    protected void executeAsync() {
        try {
            getLogger().info(String.format("Starting member reclassification process with parameters: domainID: %d,iqlExpression: %s.", getDomainId(), getIqlExpression()));

            SessionFactory factory = Session.open();

            validateParameters();
            processedItems = 0;
            totalItems = 0;
            User user = ServiceLocator.getService(SecurityService.class).findUserById(getUsrId());
            ServiceLocator.getService(SecurityService.class).authenticate(user.getUserName());


            String iqls=getIqlExpression();
            if (iqls!=null) {
		            this.searchResults = executeIql(getIqlExpression(), resolveDataSet());
		            totalItems = searchResults.size();
            }
            else {
            	this.totalItems 	= getSelection().size();
            	this.it 			= list.iterator();
            }


        	getLogger().debug(String.format("Starting " + this.getClass().getSimpleName()));
        	getLogger().debug(String.format("User that executed: " + this.getUsrId().toString()));
        	getLogger().debug(String.format("domainID: %d", getDomainId()));
        	getLogger().debug(String.format("iqlExpression: %s", getIqlExpression()!=null?getIqlExpression():"null"));
        	getLogger().debug("List: " + ( (getSelection()!=null) ? (String.valueOf(getSelection().size())+" Contents"):"null"));
            getLogger().debug("Total Items IQL/List: " + String.valueOf(this.totalItems));


            while (hasNext()) {
                final DataSetMember contentOrg = getNext();
                list_id.add( (Long) contentOrg.getId());
    		}

       		if (this.searchResults!=null)
    			this.searchResults.close();


            TransactionService transactionService = ServiceLocator.getService(TransactionService.class);
            for (DateSetMemberTagModifier dateSetMemberTagModifier : getDataSetMemberTagModifiers()) {
                dateSetMemberTagModifier.initializeForCurrentSession();
            }

            Transaction trx = null;
            try {

            	Iterator<Long> ite=list_id.iterator();

                trx = transactionService.beginTransaction(false);

                // searchResults.hasNext()



                while (ite.hasNext()) {


                	//final SearchResult next = searchResults.next();
                    //final DataSetMember member = (DataSetMember) next.getObject();

                    final DataSetMember member= getContentDao().findMemberById(ite.next());

                    List<String> updatedParts = new ArrayList<>();
                    getContentDao().flush();
                    for (DateSetMemberTagModifier contentTagModifier : getDataSetMemberTagModifiers()) {
                        contentTagModifier.modifyMember(member);
                        updatedParts.add(contentTagModifier.getAuditMessage());
                    }
                    txLogger.info(new DataSetValueUpdateEvent((DataSetMember) member, updatedParts));
                    processedItems++;
                    setProgress(100.0 * processedItems / totalItems);
                    if (processedItems % 50 == 0) {
                        trx.commit();
                        ((SessionFactory) ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
                        trx = transactionService.beginTransaction(false);
                    }
                    if (this.getState() == CommandState.CANCELED)
                        break;
                }

            } catch (Exception e) {
                if (trx != null) {
                    trx.rollback();
                    trx=null;
                }
                throw e;
            } finally {
                if (trx != null)
                    trx.commit();
            }
            if (processedItems == totalItems) {
                setProgress(100.0);
                getLogger().info("Command ended successfully.");
                setState(CommandState.COMPLETED);
                //setResultComments(str.toString());
            } else {
                setState(CommandState.ERROR);
                getLogger().info("Command not completed.");
            }

        } catch (Exception e) {
            getLogger().error(e);
            setState(CommandState.ERROR);
            setResultComments(e.getClass().getSimpleName() + "| " + e.getMessage());
        } finally {

            getLogger().debug(success ? "Success" : "Failure");
            setResult(success ? "Success" : "Failure");

            setDateTerminated(OffsetDateTime.now());
            getLogger().debug("Duration: " + String.valueOf(getDuration() / 1000) + " ms");

            Session.close();
            setStatusInfo("DB Session closed.");
        }
    }

    @Override
    public long getTotalItemsProcessed() {
        return this.processedItems;
    }

    @Override
    public long getTotalItems() {
        return this.totalItems;
    }


    public ResultSet executeIql(String statement, DataSet dataset) {
        Query query = new DataSetQuery(getQueryIndex(), statement, dataset) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public Domain getDomain() {
                return getContentDao().findDomainById(getDomainId());
            }
        };
        ResultSet resultSet = query.execute();
        return resultSet;
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    public Domain getDomain() {
        return getContentDao().findDomainById(getDomainId());
    }

    private Index getQueryIndex() {
        return getDomain().getService(JavaIndexerService.class).getIndex();
    }

    private void validateParameters() {

    	if (getSelection()==null && (getIqlExpression() == null || getIqlExpression().length()<1)) {
            throw new KbeeRuntimeException("Selection is null or Variable iqlexpression is not set.");
        }
    	
        if (getDomainId() == null) {
            throw new RuntimeException("Variable domainid is not set.");
        }
        if (getDataSetMemberTagModifiers() == null) {
            throw new RuntimeException("Content Tag Modifier list cannot be null.");
        }
        if (getUsrId() == null) {
            throw new RuntimeException("UserId cannot be null.");
        }
        final DataSet contentTemplate = resolveDataSet();
        for (DateSetMemberTagModifier contentTagModifier : getDataSetMemberTagModifiers()) {
            contentTagModifier.validate(contentTemplate);
        }
    }

    public List<DateSetMemberTagModifier> getDataSetMemberTagModifiers() {
        return dataSetTagModifiers;
    }

    public void setDataSetMemberTagModifiers(List<DateSetMemberTagModifier> contentTagModifiers) {
        this.dataSetTagModifiers = contentTagModifiers;
    }

    public String getIqlExpression() {
        return (String) this.getParameters().get("iqlexpression");
    }

    public void setIqlExpression(String iqlExpression) {
        this.getParameters().put("iqlexpression", iqlExpression);
    }

    public Long getDomainId() {
        Object param = this.getParameters().get("domainid");
        if (param instanceof String)
            return Long.parseLong((String) param);
        return (Long) param;
    }

    public void setDomainId(Long domainId) {
        this.getParameters().put("domainid", domainId);
    }

    public Long getUsrId() {
        return usrId;
    }

    public void setUsrId(Long usrId) {
        this.usrId = usrId;
    }

    public Long getDataSetID() {
        Object param = this.getParameters().get("contenttemplateid");
        if (param instanceof String)
            return Long.parseLong((String) param);
        return (Long) param;
    }

    private DataSet resolveDataSet() {
        final List<DataSet> dataSet = getContentDao().getDataSets(getDomain());
        return dataSet.stream().filter(t -> t.getId().equals(getDataSetID())).findFirst().orElse(null);
    }

    public void setDataSetID(Long dataSetID) {
        this.getParameters().put("contenttemplateid", dataSetID);
    }

    public static interface DateSetMemberTagModifier {
        void modifyMember(DataSetMember c);

        void validate(DataSet dataset);
        void initializeForCurrentSession();
        String getAuditMessage();
    }

    public static class DataSetClassifierTagModifier implements DateSetMemberTagModifier {

        Classifier classifier;
        DataSetMember newDataSetMember;
        String macro;
        TagOperation tagOperation;

        public DataSetClassifierTagModifier(Classifier classifier, DataSetMember dataSetMember, TagOperation tagOperation) {
            this.classifier = classifier;
            this.newDataSetMember = dataSetMember;
            this.tagOperation = tagOperation;
        }

        public DataSetClassifierTagModifier(Classifier classifier, String macro, TagOperation tagOperation) {
            this.classifier = classifier;
            this.macro = macro;
            this.tagOperation = tagOperation;
        }

        public void initializeForCurrentSession(){
            final ContentDao contentDao = getContentDao();
            classifier = (Classifier) contentDao.findModelObjectById(Classifier.class, classifier.getId());
            if (newDataSetMember!=null)
            	newDataSetMember = (DataSetMember) contentDao.findModelObjectById(DataSetMember.class, newDataSetMember.getId());

        }

        private ContentDao getContentDao() {
            final ContentDao contentDao = (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
            return contentDao;
        }


        @Override
        public void modifyMember(DataSetMember dataSetMember) {
            List<DataSetMember> members = new ArrayList<>();
            if (tagOperation==TagOperation.add) {
                List<Classification> classification = dataSetMember.getClassification().stream().filter(c -> c.getClassifier().getId().equals(classifier.getId())).collect(Collectors.toList());
                for (Classification curClf : classification) {
                    if (!curClf.getDataSetMember().getId().equals(dataSetMember.getId()))
                        members.add(curClf.getDataSetMember());
                }
            }
            if (tagOperation!=TagOperation.remove) {
                if(macro != null){
                    String strId = resolveMacro(macro, dataSetMember);
                    try {
                        Long id = Long.parseLong(strId);
                        members.add(getContentDao().findMemberById(id));
                    }catch(NumberFormatException ex){
                        throw new RuntimeException("Macro result '" + strId + "' could not be parsed to Long");
                    }
                }else{
                    members.add(dataSetMember);
                }
            }


            dataSetMember.setClassification(classifier, members);
            dataSetMember.setLastModifiedOffsetDateTime(OffsetDateTime.now());
        }

        private String resolveMacro(String macro, Classificable classificable) {
            KbeeTextTemplate template = new KbeeTextTemplate(macro);
            String text = template.process(classificable);
            return text;
        }

        @Override
        public String getAuditMessage() {
            return tagOperation.getOperationDescription( classifier.getName(), newDataSetMember != null ? newDataSetMember.getDisplayName():null);
        }

        @Override
        public void validate(DataSet dataSet) {
            if (this.classifier == null) {
                throw new RuntimeException("Variable classifierid is not set.");
            }
            if(tagOperation!=TagOperation.remove) {
                if (this.newDataSetMember == null && this.macro == null){
                    throw new RuntimeException("Variable memberid or macro is not set.");
                }
            }
            if (this.tagOperation == null) {
                throw new RuntimeException("Variable tagOperation is not set.");
            }
            boolean hasClassifier = dataSet.getClassifiers().stream().anyMatch(clf -> clf.getId().equals(classifier.getId()));
            if (!hasClassifier) {
                throw new RuntimeException("Specified dataset does not contain classifier '" + classifier.getName() + "'.");
            }


        }
    }


    public static class DataSetAttributeTagModifier implements DateSetMemberTagModifier {
        Attribute attribute;
        String value;
        TagOperation tagOperation;

        public DataSetAttributeTagModifier(Attribute attribute, String value, TagOperation tagOperation) {
            this.attribute = attribute;
            this.value = value;
            this.tagOperation = tagOperation;
        }

        public void initializeForCurrentSession(){
            final ContentDao contentDao = getContentDao();
            if (tagOperation!=TagOperation.remove)
            	attribute = (Attribute) contentDao.findModelObjectById(Attribute.class, attribute.getId());
        }

        private ContentDao getContentDao() {
            final ContentDao contentDao = (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
            return contentDao;
        }

        @Override
        public void modifyMember(DataSetMember dataSetMember) {
            List<String> attributeValues;
            if (tagOperation==TagOperation.add) {
                attributeValues = dataSetMember.getAttributeValues(attribute);
                if (!attributeValues.contains(value))
                    attributeValues.add(value);
            } else if (tagOperation == TagOperation.replace) {
                attributeValues = Arrays.asList(value);
            }else{
                attributeValues= new ArrayList<>();
            }
            dataSetMember.setAttributeValues(attribute, attributeValues);
        }

        @Override
        public String getAuditMessage() {
            return tagOperation.getOperationDescription( attribute.getName(), value);
        }

        @Override
        public void validate(DataSet dataSet) {
            if (this.attribute == null) {
                throw new RuntimeException("Variable attribute is not set.");
            }
            if (tagOperation!=TagOperation.remove && this.value == null) {
                throw new RuntimeException("Attribute value cannot be null.");
            }
            if (this.tagOperation == null) {
                throw new RuntimeException("Variable tagOperation is not set.");
            }
            boolean hasAttribute = dataSet.getAttributes().stream().anyMatch(attr -> attr.getAttribute().getId().equals(attribute.getId()));
            if (!hasAttribute) {
                throw new RuntimeException("Specified dataset does not contain attribute '" + attribute.getName() + "'.");
            }
        }
    }


}

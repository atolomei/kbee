package com.novamens.kbee.content.command;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.*;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.ContentService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.session.Session;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.query.ContentQuery;
import com.novamens.kbee.content.query.MonitorQuery;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.util.KbeeRuntimeException;

import org.apache.logging.log4j.Level;

import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This command seems to be failing when the
 * ResultSet is altered by the loop that
 * re-classifies the files.
 * <p>
 * We have to consider using a cursor.
 */
public class ReclassifyContentCommand extends AsyncCommand {

    private boolean success = true;
    private Long usrId = null;
    private List<ContentTagModifier> contentTagModifiers;

    private long processedItems;
    private long totalItems;

    /**
     * There are modes of operation
     * IQL Sentence -> ResultSet
     * List<Content> ->
     */
    // List of Content
    private List<Content> list;
    private Iterator<Content> it;

    //
    // ResultSet of IQL sentence
    private ResultSet searchResults;

    private List<Long> list_id = new ArrayList<Long>();
    private TagTargetDocumentsPlace tagTargetDocumentsPlace;

    /**
     *
     */
    public ReclassifyContentCommand() {
        setName(this.getClass().getSimpleName());
        setDescription("Reclassify all document given by a IQL or a selection of Contents");
        setContentTagModifiers(contentTagModifiers);
        setDomainId(null);
        setIqlExpression(null);
        setContentTemplateID(null);
    }


    public ReclassifyContentCommand(Long contentTemplateID, List<ContentTagModifier> contentTagModifiers, Long domainID, List<Content> list) {
        setName(this.getClass().getSimpleName());
        setDescription("Reclassify all document given by a IQL or a selection of Contents");
        setContentTagModifiers(contentTagModifiers);
        setDomainId(domainID);
        setIqlExpression(null);
        setSelection(list);
        setContentTemplateID(contentTemplateID);
    }

    public ReclassifyContentCommand(Long contentTemplateID, TagTargetDocumentsPlace tagTargetDocumentsPlace, List<ContentTagModifier> contentTagModifiers, Long domainID, String iqlExpression) {
        setName(this.getClass().getSimpleName());
        setDescription("Reclassify all document given by a IQL or a selection of Contents");
        setContentTagModifiers(contentTagModifiers);
        setDomainId(domainID);
        setSelection(null);
        setIqlExpression(iqlExpression);
        setContentTemplateID(contentTemplateID);
        this.tagTargetDocumentsPlace = tagTargetDocumentsPlace;
    }

    public void setSelection(List<Content> list) {
        this.list = list;
    }

    public List<Content> getSelection() {
        return this.list;
    }


    @Override
    protected void executeAsync() {
        Transaction trx = null;
        try {

        	setDateStarted(OffsetDateTime.now());

            SessionFactory factory = Session.open();

            validateParameters();

            getLogger().debug(String.format("Starting " + this.getClass().getSimpleName()));
            getLogger().debug(String.format("User that executed: " + this.getUsrId().toString()));

            getLogger().debug(String.format("domainID: %d", getDomainId()));
            getLogger().debug(String.format("iqlExpression: %s", getIqlExpression() != null ? getIqlExpression() : "null"));

        	ContentTemplate template = resolveContentTemplate();
            getLogger().debug(String.format("ContentTemplate: " + ((template != null) ? template.getName() : "null")));

            getLogger().debug("List: " + ((getSelection() != null) ? (String.valueOf(getSelection().size()) + " Contents") : "null"));

            processedItems = 0;
            totalItems = 0;

            User user = ServiceLocator.getService(SecurityService.class).findUserById(getUsrId());
            ServiceLocator.getService(SecurityService.class).authenticate(user.getUserName());

            String iqls=getIqlExpression();

            if (iqls!=null) {
            	this.searchResults 	= getQuery(getDomainId(), getContentTemplateID(),tagTargetDocumentsPlace, getIqlExpression()).execute();
            	this.totalItems 	= searchResults.size();
            } else {
                this.totalItems = getSelection().size();
                this.it = list.iterator();
            }

            getLogger().debug("Total Items IQL/List: " + String.valueOf(this.totalItems));


       		if (this.totalItems>0) {

		           		while (hasNext()) {
		                    final Content contentOrg = getNext();
		                    list_id.add( (Long) contentOrg.getId());
		        		}

		           		if (this.searchResults!=null)
		        			this.searchResults.close();

		        		if (getSelection()!=null)
		        			getSelection().clear();

		       		TransactionService transactionService = ServiceLocator.getService(TransactionService.class);

		            for (ContentTagModifier contentTagModifier : getContentTagModifiers()) {
		                contentTagModifier.initializeForCurrentSession();
		            }


		            try {

		                trx = transactionService.beginTransaction(false);
		                Iterator<Long> ite=list_id.iterator();

		                while (ite.hasNext()) {

		                    final Content contentOrg = getContentDao().findContentById(ite.next());

		                    if ((contentOrg != null) && (!contentOrg.isLocked())) {

		                    	List<String> updatedParts = new ArrayList<>();
		                        Content contentToModify;

                            if (tagTargetDocumentsPlace == TagTargetDocumentsPlace.library) {
                                    contentToModify = contentOrg.getService(ContentService.class).checkout();
                            } else {
                                    contentToModify=contentOrg;
                            }


                                getContentDao().flush();

		                        for (ContentTagModifier contentTagModifier : getContentTagModifiers()) {
		                            contentTagModifier.modifyContent(contentToModify);
		                            updatedParts.add(contentTagModifier.getAuditMessage());
		                        }


                                boolean updateTitle = true;
                                if (updateTitle) {
                                    final String titleRuleTemplate = contentToModify.getContentTemplate().getTitleRuleTemplate();
                                    if (titleRuleTemplate != null && !titleRuleTemplate.isEmpty()) {//Has title rule
                                        ExtractionRule rule = contentToModify.getContentTemplate().getTitleRule();
                                        if (rule != null) {//Has title rule
                                            String newTitle = (String) rule.extract(contentToModify);
                                            if (newTitle != null && !newTitle.equals(contentToModify.getTitle())) { //Title changed
                                                final boolean titleManuallyUpdated = "true".equals(contentToModify.getService(PropertyService.class).getProperty("title"));
                                                if(!titleManuallyUpdated) { //Title manually updated
                                                    contentToModify.setTitle(newTitle);
                                                    updatedParts.add(String.format("Tag Tool update title."));
                                                }
                                            }

                                        }
                                    }
                                }


                            if (tagTargetDocumentsPlace == TagTargetDocumentsPlace.library) {
                                if (!contentOrg.isHeadVersion()) {
                                    getLogger().debug(contentOrg.getTitle() + " | " + contentOrg.getId().toString() + " | Can not be processed " + (contentOrg.isHeadVersion() ? "" : "[ not head version ]") + (contentOrg.isLocked() ? "[ isLocked ]" : ""));
                                    continue;
                                }
                                /**  Checkin is silent. Do not send alerts  */
                                    contentToModify.getService(ContentService.class).checkin(updatedParts, false);
                            } else {
                                    contentToModify.getService(ContentService.class).update(updatedParts);
                                    getContentDao().save(contentToModify);
                            }


                            getLogger().debug(contentToModify.getTitle() + " | " + contentToModify.getId().toString() + " | " + contentToModify.getContentTemplate().getDisplayName() + " | " + String.valueOf(processedItems));
                        } else {
                            if (contentOrg != null)
                                getLogger().debug(contentOrg.getTitle() + " | " + contentOrg.getId().toString() + " | Can not be processed " + (contentOrg.isHeadVersion() ? "" : "[ not head version ]") + (contentOrg.isLocked() ? "[ isLocked ]" : ""));
		                    }

		                    processedItems++;

		                    setProgress(100.0 * processedItems / totalItems);

		                    if (processedItems % 50 == 0) {
		                        trx.commit();
                            getLogger().debug("Commit (partial) processedItems -> " + String.valueOf(processedItems));
		                        // ((SessionFactory) ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().clear();
		                        trx = transactionService.beginTransaction(false);
		                    }
		                    if (this.getState() == CommandState.CANCELED)
		                        break;
		                }

		            } catch (Exception e) {
                    getLogger().error(e);
		            	if (trx != null) {
		                    trx.rollback();
		                    trx = null;
		                }
		                throw e;

		            } finally {

                    //((SessionFactory) ServiceLocator.getService(BeansService.class).getBean("sessionFactory")).getCurrentSession().flush();

		        		if (this.searchResults!=null)
		        				this.searchResults.close();

		                if (trx != null && totalItems>0) {
                        getLogger().debug("Commit (final) processedItems -> " + String.valueOf(processedItems));
		                    trx.commit();
		                }

		            }
		            if (processedItems == totalItems) {
		                setProgress(100.0);
                    getLogger().info("Command ended successfully.");
                    end();
                    setResultComments("Processed: " + String.valueOf(processedItems));

		            } else {
                    error();
                    setResultComments("Processed: " + String.valueOf(processedItems));
                    getLogger().info("Command not completed.");
                }

            } else {
                setProgress(100.0);
                getLogger().info("Command ended successfully.");
                end();
       			}

       		} catch (Exception e) {
            getLogger().error(e);
            error();
            setResultComments(e.getClass().getSimpleName() + "| " + e.getMessage());
        } finally {

//
//            if (trx != null) {
//                trx.rollback();
//            }
            getLogger().debug(success ? "Success" : "Failure");

            getLogger().debug("Start: " + this.getDateStarted().toString());
            getLogger().debug("End: " + this.getDateTerminated().toString());

            getLogger().debug("Duration: " + String.valueOf(getDuration()) + " ms");
            getLogger().debug("---------------------------------------------------");

            com.novamens.hibernate.session.Session.close();
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

    public kbee.util.logging.Logger getCustomLogger() {
        return LogingHelper.getPrivateLogger(this.getClass().getName() + "_" + getId(), Level.DEBUG);
    }

    public static String extractDirectoryPath(String path) {
        if ((path == null) || path.equals("") || path.equals("/")) {
            return "";
        }

        int lastSlashPos = path.lastIndexOf('/');

        if (lastSlashPos >= 0) {
            return path.substring(0, lastSlashPos); //strip off the slash
        } else {
            return "";
        }
    }

    public static Query getQuery(Serializable domainId, Serializable contentTemplateId, TagTargetDocumentsPlace tagTargetDocumentsPlace, String statement) {
        final Domain domain = getContentDao().findDomainById(domainId);
        final Index index = domain.getService(JavaIndexerService.class).getIndex();

        Query query;
        if (tagTargetDocumentsPlace == TagTargetDocumentsPlace.monitor) {
            query = new MonitorQuery(index, statement) {

                private static final long serialVersionUID = 1L;

                @Override
                public Domain getDomain() {
                    return getContentDao().findDomainById(domainId);
                }
            };
        } else {
            query = new ContentQuery(index, statement, true) {

                private static final long serialVersionUID = 1L;

                @Override
                public Domain getDomain() {
                    return getContentDao().findDomainById(domainId);
                }
            };
        }


        if (contentTemplateId != null)
            query.getParameters().put("template", "[" + contentTemplateId + "]");

        return query;
    }

    protected static ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    public Domain getDomain() {
        return getContentDao().findDomainById(getDomainId());
    }


    private void validateParameters() {

        if (getSelection() == null && (getIqlExpression() == null || getIqlExpression().length() < 1)) {
            throw new KbeeRuntimeException("Selection is null or Variable iqlexpression is not set.");
        }

        if (getDomainId() == null) {
            throw new KbeeRuntimeException("Variable domainid is not set.");
        }
        if (getContentTagModifiers() == null) {
            throw new KbeeRuntimeException("Content Tag Modifier list cannot be null.");
        }
        if (getUsrId() == null) {
            throw new KbeeRuntimeException("UserId cannot be null.");
        }


        final ContentTemplate contentTemplate = resolveContentTemplate();

        if ((getSelection() == null) && contentTemplate == null)
            throw new KbeeRuntimeException("ContentTemplate cannot be null for IQL Sentences.");
    }

    public List<ContentTagModifier> getContentTagModifiers() {
        return contentTagModifiers;
    }

    public void setContentTagModifiers(List<ContentTagModifier> contentTagModifiers) {
        this.contentTagModifiers = contentTagModifiers;
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

    public TagTargetDocumentsPlace getTagTargetDocumentsPlace() {
        return tagTargetDocumentsPlace;
    }

    public Long getNewContentTemplateID() {
        Object param = this.getParameters().get("newcontenttemplateid");
        if (param instanceof String)
            return Long.parseLong((String) param);
        return (Long) param;
    }


    public Long getContentTemplateID() {
        Object param = this.getParameters().get("contenttemplateid");

        if (param == null)
            return null;

        if (param instanceof String)
            return Long.parseLong((String) param);

        return (Long) param;

    }

    private ContentTemplate resolveContentTemplate() {
        final List<ContentTemplate> templates = getContentDao().getTemplates(getDomain());
        return templates.stream().filter(t -> t.getId().equals(getContentTemplateID())).findFirst().orElse(null);
    }


    public void setContentTemplateID(Long contentTemplateID) {
        this.getParameters().put("contenttemplateid", contentTemplateID);
    }


    public static interface ContentTagModifier {
        void modifyContent(Content c);

        void validate(ContentTemplate contentTemplate);

        void initializeForCurrentSession();

        String getAuditMessage();
    }

    public static class ClassifierContentTagModifier implements ContentTagModifier {

        Classifier classifier;
        DataSetMember dataSetMember;
        String macro;
        TagOperation tagOperation;

        public ClassifierContentTagModifier(Classifier classifier, DataSetMember dataSetMember, TagOperation tagOperation) {
            this.classifier = classifier;
            this.dataSetMember = dataSetMember;
            this.tagOperation = tagOperation;
        }

        public ClassifierContentTagModifier(Classifier classifier, String macro, TagOperation tagOperation) {
            this.classifier = classifier;
            this.macro = macro;
            this.tagOperation = tagOperation;
        }

        public void initializeForCurrentSession() {
            final ContentDao contentDao = (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
            classifier = (Classifier) contentDao.findModelObjectById(Classifier.class, classifier.getId());
            if (dataSetMember != null)
                dataSetMember = (DataSetMember) contentDao.findModelObjectById(DataSetMember.class, dataSetMember.getId());

        }

        @Override
        public void modifyContent(Content content) {
            List<DataSetMember> members = new ArrayList<>();
            if (tagOperation == TagOperation.add) {
                List<Classification> classification = content.getClassification().stream().filter(c -> c.getClassifier().getId().equals(classifier.getId())).collect(Collectors.toList());
                for (Classification curClf : classification) {
                    if (!curClf.getDataSetMember().getId().equals(dataSetMember.getId()))
                        members.add(curClf.getDataSetMember());
                }
            }
            if (tagOperation != TagOperation.remove) {
                if (macro != null) {
                    String strId = resolveMacro(macro, content);
                    try {
                        Long id = Long.parseLong(strId);
                        members.add(getContentDao().findMemberById(id));
                    } catch (NumberFormatException ex) {
                        throw new RuntimeException("Macro result '" + strId + "' could not be parsed to Long");
                    }
                } else {
                    members.add(dataSetMember);
                }
            }
            content.setClassification(classifier, members);
        }

        private String resolveMacro(String macro, Classificable classificable) {
            KbeeTextTemplate template = new KbeeTextTemplate(macro);
            String text = template.process(classificable);
            return text;
        }

        @Override
        public String getAuditMessage() {
            return tagOperation.getOperationDescription(classifier.getName(), dataSetMember != null ? dataSetMember.getDisplayName() : null);
        }

        @Override
        public void validate(ContentTemplate contentTemplate) {
            if (this.classifier == null) {
                throw new RuntimeException("Variable classifierid is not set.");
            }
            if (tagOperation != TagOperation.remove) {
                if (this.dataSetMember == null && this.macro == null) {
                    throw new RuntimeException("Variable memberid or macro is not set.");
                }
            }
            if (this.tagOperation == null) {
                throw new RuntimeException("Variable tagOperation is not set.");
            }
            boolean hasClassifier = contentTemplate.getClassifiers().stream().anyMatch(clf -> clf.getClassifier().getId().equals(classifier.getId()));
            if (!hasClassifier) {
                throw new RuntimeException("Specified ContentTemplate does not contain classifier '" + classifier.getName() + "'.");
            }
        }
    }


    public static class AttributeContentTagModifier implements ContentTagModifier {
        com.novamens.content.model.Attribute attribute;
        String value;
        TagOperation tagOperation;

        public AttributeContentTagModifier(Attribute attribute, String value, TagOperation tagOperation) {
            this.attribute = attribute;
            this.value = value;
            this.tagOperation = tagOperation;
        }

        public void initializeForCurrentSession() {
            final ContentDao contentDao = (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
            attribute = (Attribute) contentDao.findModelObjectById(Attribute.class, attribute.getId());
        }

        @Override
        public void modifyContent(Content content) {
            List<String> attributeValues;
            if (tagOperation == TagOperation.add) {
                attributeValues = content.getAttributeValues(attribute);
                if (!attributeValues.contains(value))
                    attributeValues.add(value);
            } else if (tagOperation == TagOperation.replace) {
                attributeValues = Arrays.asList(value);
            } else {
                attributeValues = new ArrayList<>();
            }
            content.setAttributeValues(attribute, attributeValues);
        }

        @Override
        public String getAuditMessage() {
            return tagOperation.getOperationDescription(attribute.getName(), value);
        }

        @Override
        public void validate(ContentTemplate contentTemplate) {
            if (this.attribute == null) {
                throw new RuntimeException("Variable attribute is not set.");
            }
            if (tagOperation != TagOperation.remove && this.value == null) {
                throw new RuntimeException("Attribute value cannot be null.");
            }
            if (this.tagOperation == null) {
                throw new RuntimeException("Variable replaceValue is not set.");
            }
            boolean hasAttribute = contentTemplate.getAttributes().stream().anyMatch(attr -> attr.getAttribute().getId().equals(attribute.getId()));
            if (!hasAttribute) {
                throw new RuntimeException("Specified ContentTemplate does not contain attribute '" + attribute.getName() + "'.");
            }
        }
    }

    public static class ContentClassModifier implements ContentTagModifier {
        Long newContentTemplateID;
        ContentTemplate contentTemplate;

        public ContentClassModifier(Long newContentTemplateID) {
            this.newContentTemplateID = newContentTemplateID;
        }

        public void initializeForCurrentSession() {
            final ContentDao contentDao = (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
            contentTemplate = contentDao.findContentTemplateById(newContentTemplateID);
        }

        @Override
        public void modifyContent(Content content) {
            content.setContentTemplate(contentTemplate);
        }

        @Override
        public String getAuditMessage() {
            return String.format("Tag Tool [ %s ]. %s ->  %s", "change", "Content template", contentTemplate.getName());
        }

        @Override
        public void validate(ContentTemplate contentTemplate) {
            if (contentTemplate == null) {
                throw new RuntimeException("Variable contentTemplate is not set.");
            }
        }
    }

    public static class CancelWorkflowModifier implements ContentTagModifier {

        public CancelWorkflowModifier() {
        }

        @Override
        public void modifyContent(Content content) {
            content.getService(WorkflowService.class).cancel();
        }

        @Override
        public void initializeForCurrentSession() {
        }

        @Override
        public String getAuditMessage() {
            return String.format("Tag Tool ->  Workflow Canceled");
        }

        @Override
        public void validate(ContentTemplate contentTemplate) {
        }


    }


    private boolean hasNext() {
        if (searchResults != null)
            return searchResults.hasNext();
        if (it != null)
            return it.hasNext();
        return false;
    }


    private Content getNext() {
        if (searchResults != null) {
            final SearchResult next = searchResults.next();
            final Content contentOrg = (Content) next.getObject();
            return contentOrg;
        } else {
            Content contentOrg = getContentDao().findContentById(it.next().getId());
            return contentOrg;
        }
    }


}

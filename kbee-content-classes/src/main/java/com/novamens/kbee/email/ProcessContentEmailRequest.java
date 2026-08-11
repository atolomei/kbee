package com.novamens.kbee.email;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.entity.Person;
import com.novamens.content.model.*;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Versionable;
import com.novamens.kbee.content.model.KbeeClassification;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbfs.FileServerException;
import com.novamens.logging.UpdateEvent;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;
import com.sun.mail.imap.IMAPFolder;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ProcessContentEmailRequest extends AbstractServiceRequest {

    private String storeFactoryBean;
    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");
    static private Logger trx_logger = LogManager.getLogger("TxLogger");

    private Long mailUID;
    private String folder;
    private boolean executed;

    final String DOMAIN_PARAM = "domain";

    @Override
    public void execute() {
        if (isExecuted()) {
            logger.error("-----------------------------------------------------------------------------");
            logger.error(getName() + " | id:" + getId().toString() + ". already executed ");
            logger.error("-----------------------------------------------------------------------------");
            return;
        }

        final MailStoreFactory mailStore = (MailStoreFactory) ServiceLocator.getService(BeansService.class).getBean(storeFactoryBean);
        Store newStore = null;
        try {
            newStore = mailStore.getEmailSession().getStore();

            newStore.connect();
            final IMAPFolder defaultFolder = (IMAPFolder) newStore.getFolder(folder);
            defaultFolder.open(Folder.READ_ONLY);
            final MimeMessage message = (MimeMessage) defaultFolder.getMessageByUID(mailUID);
            EmailParser emailParser = new EmailParser();

            Email email = emailParser.getEmail(message, false, false);
            final EmailBodyParameterParser emailBodyParameter = new LowerEmailBodyParameterParserImpl();
            final Map<String, Object> lowercaseBodyParameters = emailBodyParameter.parseParameters(email.getBody());




            TransactionService transactionService = ServiceLocator.getService(TransactionService.class);
            Transaction transaction = null;
            try {
                transaction = transactionService.beginTransaction(false);

                final Person person = getPerson(email, lowercaseBodyParameters);
                UserProfile userprofile = person.getProfile(UserProfile.class);
                ServiceLocator.getService(SecurityService.class).authenticate(userprofile.getUser().getUserName());

                final boolean emailAlreadyProcessed = emailAlreadyProcessed(email.getMessageID());
                if(emailAlreadyProcessed){
                    logger.error("Email " + email.getMessageID() + " was already processed.");
                    return;
                }


                //processAsBatchFiles(message, email, lowercaseBodyParameters,transaction, userprofile);
                //processAsNewDocument(message, email, lowercaseBodyParameters, transaction, userprofile);
                processAsMyBox(message, email, lowercaseBodyParameters, transaction, userprofile);

            } catch (Exception e) {
                if (transaction != null)
                    transaction.rollback();
            }

        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (newStore != null)
                    newStore.close();
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processAsBatchFiles(MimeMessage message, Email email, Map<String, Object> lowercaseBodyParameters, Transaction transaction, UserProfile userprofile) throws FileServerException, EmailDocumentCreationException, MessagingException {
        IDoc idoc = ServiceLocator.getService(UserService.class).getUploadAndCreateContainer();
        String addedBatchFiles = "";
        for (Email.Attachment attachment : email.getAttachments()) {
            addFileToContent(idoc, attachment.getInputStream(), attachment.getFileName(), attachment.getContentType());
            addedBatchFiles += attachment.getFileName() + "\n";
        }
        respondMessage(message, "Added batch files:\n " + addedBatchFiles);

        idoc.getService(ContentService.class).update();
    }

    private void processAsMyBox(MimeMessage message, Email email, Map<String, Object> lowercaseBodyParameters, Transaction transaction, UserProfile userprofile) throws FileServerException, EmailDocumentCreationException, MessagingException {
        final ContentTemplate template = getMyBoxContentTemplate(userprofile.getDomain());

        DataSet da = null;
        Classifier type = null;
        DataSetMember me = null;

        for (ClassifierTemplate c : template.getClassifiers()) {
            if (c.getClassifier().isContentType()) {
                type = c.getClassifier();
                da = c.getClassifier().getDataSet();
                break;
            }
        }

        if (da != null)
            me = getContentDao().findMemberByValue(da, getContentDao().findSystemParameterValueByKey("datasetvalue.mybox.strvalue", "Resource"));

        String addedBatchFiles = "";
        Long docId = null;
        for (Email.Attachment attachment : email.getAttachments()) {
            IDoc c = (IDoc) ServiceLocator.getService(ContentFactoryService.class).create(template.getName(), true, true);
            c.setTitle(email.getSubject() + " - " + attachment.getFileName());
            c.setLastModifiedOffsetDateTime(OffsetDateTime.now());
            c.setLastModifiedUser(userprofile.getUser());
            c.setState(ObjectState.DRAFT);
            c.setWorkspace((Long) userprofile.getUser().getId());
            c.setDomain(c.getDomain());
            ((Versionable<?>) c).setHeadVersion(false);

            if (me != null) {
                Classification cl = new KbeeClassification(type, me, c);
                c.addClassification(cl);
            }

            getContentDao().save(c);
            getContentDao().flush();

            addFileToContent(c, attachment.getInputStream(), attachment.getFileName(), attachment.getContentType());
            addedBatchFiles += attachment.getFileName() + "\n";

            logger.debug("MyBox file created from email -> " + c.getTitle() != null ? c.getTitle() : "no title" + " | " + (userprofile.getUser() != null ? userprofile.getUser().getUserName() : "") );


            c.getService(ContentService.class).update();
            docId = (Long) c.getId();
        }
        final EmailFileImport emailFileImport = new EmailFileImport(email.getMessageID(), (Long) docId);
        saveEmailProcessedRecord(emailFileImport);

        transaction.commit();

        respondMessage(message, "Files added to MyBox: " + addedBatchFiles );
        //respondMessage(message, "Workflow created. \n Url: " + getUrl(content));
    }

    private void saveEmailProcessedRecord(EmailFileImport emailFileImport) {
        SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
        sf.getCurrentSession().save(emailFileImport);
    }

    private ContentTemplate getMyBoxContentTemplate(Domain domain) {
        ContentTemplate template = null;
        for (ContentTemplate t : getContentDao().getTemplates(domain)) {
            if (t.getAlias() != null && t.getAlias().equals("resources")) {
                template = t;
                break;
            }
        }
        return template;

    }


    private void processAsNewDocument(MimeMessage message, Email email, Map<String, Object> lowercaseBodyParameters, Transaction transaction, UserProfile userprofile) throws FileServerException, EmailDocumentCreationException, MessagingException {
        final Content content = createDocument(email, userprofile, lowercaseBodyParameters);

        trx_logger.info(new UpdateEvent(content, "Document created from email :" + email.getMessageID()));
        transaction.commit();
        getContentDao().flush();
        respondMessage(message, "Workflow created. \n Url: " + getUrl(content));
    }

    private Person getPerson(Email email, Map<String, Object> lowercaseBodyParameters) {
        final List<Person> persons = getContentDao().findPersonByEmail(email.getFrom());
        Person person = null;

        String preferredDomain = getParameterFirstValue(lowercaseBodyParameters.get(DOMAIN_PARAM));
        if (preferredDomain != null) {
            person = persons.stream()
                    .filter(p -> p.getDomain().getName().toLowerCase().equals(preferredDomain.toLowerCase()))
                    .findFirst().orElse(null);
        }
        if (person == null)
            person = persons.stream().findFirst().orElse(null);

        if (person == null) {
            throw new KbeeRuntimeException("No person found for email " + email.getFrom());
        }
        return person;
    }

    private void respondMessage(Message message, String body) throws MessagingException {

        String kbeeFrom = "noreplay@kbee.io";

        BeansService beans = ServiceLocator.getService(BeansService.class);
        JavaMailSender mailsender = (JavaMailSender) beans.getBean("mailSender");

        MimeMessage message2 = mailsender.createMimeMessage();
        message2 = (MimeMessage) message.reply(false);
        message2.setSubject("RE: " + message.getSubject());
        message2.setFrom(new InternetAddress(kbeeFrom));
        message2.setReplyTo(message.getReplyTo());
        message2.addRecipient(Message.RecipientType.TO, message.getFrom()[0]);

        // Create your new message part
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);

        // Create a multi-part to combine the parts
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Create and fill part for the forwarded content
        messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(message.getDataHandler());

        // Add part to multi part
        multipart.addBodyPart(messageBodyPart);

        // Associate multi-part with message
        message2.setContent(multipart);

        mailsender.send(message2);

    }

    private String getUrl(Content content) {
        String url = "";
        WorkflowService ws = content.getService(WorkflowService.class);
        Task task = ws.getTask();
        if (task != null) {
            String content_class_name = content.getClassCode();
            String task_name = task.getName().replaceAll("\\s", "-").toLowerCase();
            url = content.getDomain().getWebsite();
            if (url != null)
                url = url.trim();
            if (!url.endsWith("/"))
                url = url + "/";
            url = url + "task/" + content_class_name + "/" + task_name + "/" + String.valueOf(content.getId());
        }
        return url;
    }


    private Content createDocument(Email email, UserProfile userProfile, Map<String, Object> lowercaseBodyParameters) throws FileServerException, EmailDocumentCreationException {
        final String PARAM_LAUNCHER_NAME = "launcher";

        String launcherName = getParameterFirstValue(lowercaseBodyParameters.get(PARAM_LAUNCHER_NAME));
        if (launcherName == null) {
            throw new InvalidParamEmailDocumentCreationException(PARAM_LAUNCHER_NAME);
        }

        Domain domain = userProfile.getDomain();
        WorkflowDomainService workflowDomainService = domain.getService(WorkflowDomainService.class);
        Process process = workflowDomainService.startProcess(launcherName);

        if (process == null) {
            throw new KbeeRuntimeException("Process cannot be created.");
        }

        final Content content = ((KbeeContext) process.getContext()).getContent();

        content.setDomain(domain);
        content.setTitle(email.getSubject());
        content.setName(email.getSubject());


        for (ClassifierTemplate classifierTemplate : content.getContentTemplate().getClassifiers()) {
            final Classifier classifier = classifierTemplate.getClassifier();

            Object value = lowercaseBodyParameters.get(classifier.getAlias());
            if (value == null)
                value = lowercaseBodyParameters.get(classifier.getName().toLowerCase());

            if (value != null) {
                List<DataSetMember> values = new ArrayList<>();
                if (value instanceof List) {
                    for (Object cflStrVal : ((List) value)) {
                        final DataSetMember memberByValue = getContentDao().findMemberByValue(classifier.getDataSet(), cflStrVal.toString());
                        if (memberByValue != null)
                            ((List) value).add(memberByValue);
                        //else
                    }
                } else {
                    final DataSetMember memberByValue = getContentDao().findMemberByValue(classifier.getDataSet(), value.toString());
                    if (memberByValue != null)
                        values.add(memberByValue);
                }
                content.setClassification(classifier, values);
            }
        }

        for (AttributeTemplate attributeTemplate : content.getContentTemplate().getAttributes()) {
            final Attribute attribute = attributeTemplate.getAttribute();

            Object value = lowercaseBodyParameters.get(attribute.getAlias());
            if (value == null)
                value = lowercaseBodyParameters.get(attribute.getName().toLowerCase());

            if (value != null) {
                List<String> values = new ArrayList<>();
                if (value instanceof List) {
                    for (Object cflStrVal : ((List) value)) {
                        ((List) value).add(cflStrVal);
                    }
                } else {
                    values.add(value.toString());
                }
                content.setAttributeValues(attribute, values);
            }
        }

        for (Email.Attachment attachment : email.getAttachments()) {
            addFileToContent(content, attachment.getInputStream(), attachment.getFileName(), attachment.getContentType());
        }

        return content;
    }

    private boolean emailAlreadyProcessed(String messageid){
        SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
        final EmailFileImport emailMyBox = sf.getCurrentSession().get(EmailFileImport.class, messageid);
        return emailMyBox != null;
    }

    private String getParameterFirstValue(Object paramValue) {
        if (paramValue != null) {
            if (paramValue instanceof List) {
                return (String) ((List) paramValue).stream().findFirst().map(v -> v.toString()).orElse(null);
            } else {
                return paramValue.toString();
            }
        }
        return null;
    }

    private boolean isExecuted() {
        if (this.executed)
            return true;
        return ServiceLocator.getService(SchedulerService.class).containsRequestToken(getId());
    }

    private void addFileToContent(Content content, InputStream fileStream, String filename, String contentType) throws FileServerException {

        KBFileImpl kbfile = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFile(filename);

        kbfile.getService(KBFSResourceService.class).putObject(filename, fileStream);


        kbfile.setTitle(FilenameUtils.getBaseName(filename).replaceAll("(-|_)", " "));

        content.getService(ContentService.class).addFile(kbfile);
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }


    public String getStoreFactoryBean() {
        return storeFactoryBean;
    }

    public void setStoreFactoryBean(String storeFactoryBean) {
        this.storeFactoryBean = storeFactoryBean;
    }

    public Long getMailUID() {
        return mailUID;
    }

    public void setMailUID(Long mailUID) {
        this.mailUID = mailUID;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}

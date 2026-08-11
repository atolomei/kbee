package com.novamens.content.service;

import javax.sql.DataSource;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.Source;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.library.Library;
import com.novamens.content.notes.Billboard;
import com.novamens.content.notification.ContentNotification;
import com.novamens.content.notification.Notification;
import com.novamens.content.notification.NotificationType;
import com.novamens.content.notification.WorkNoteNotification;
import com.novamens.content.resource.KBFile;
import com.novamens.content.rule.ActionRule;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.service.BusinessSystemService;
import com.novamens.service.FactoryService;

public interface ContentFactoryService extends BusinessSystemService, FactoryService {
	
	/**
	 * @param templatename
	 * @param workspace
	 * @param quiet
	 * @return
	 * @throws ContentCreationException
	 * @throws ContentMgmtException
	 */
	public Content create(String templatename, boolean workspace, boolean quiet) throws ContentCreationException, ContentMgmtException;

	public Long getNewNotificationId();
	public Long getNewOId();
	public void setDataSource(DataSource dataSource);
	public void setSchema(String schema);
	public String getSchema();

	public Content create(String templatename) throws ContentCreationException, ContentMgmtException;
	public Content create(String templatename, KBFile file) throws ContentCreationException;

	public Content create(String templatename, KBFile file, ObjectState state) throws ContentCreationException;
	public Content create(String templatename, KBFile file, ObjectState draft, User object);
	
	public Content create(String templatename, boolean workspace) throws ContentCreationException, ContentMgmtException;
	
	public Content createProxy(Content content) throws ContentCreationException, ContentMgmtException;

	public WorkNoteNotification createWorkNoteNotification(Billboard note, User receiver) throws ContentCreationException, ContentMgmtException;
	public ContentNotification createContentPublishNotification(Content note, User receiver) throws ContentCreationException, ContentMgmtException;
	public ContentNotification createContentPublishNotification(Content note, User receiver, boolean deleteOnAccept) throws ContentCreationException, ContentMgmtException;
	public Notification createNotification(NotificationType type, Content note, String text, User receiver) throws ContentCreationException, ContentMgmtException;
								
	public Source createSource(String name, String displayName) throws ContentCreationException, ContentMgmtException;
	public Source createSource(String name, String displayName, Domain domain) throws ContentCreationException, ContentMgmtException;
	
	public Library createLibrary(String name) throws ContentCreationException, ContentMgmtException;
	public Library createLibrary(String name, Domain domain) throws ContentCreationException, ContentMgmtException;
	
	public ActionRule createRule(Content associatedContent) throws ContentCreationException, ContentMgmtException;
	public ActionRule createRule() throws ContentCreationException, ContentMgmtException;
										
	public EmailTemplate createEmailTemplate(String key, String title, String lang, String from, String subject, String text, boolean isDefault, String model) throws ContentCreationException, ContentMgmtException;
	public EmailTemplate createEmailTemplate(Domain domain, String key, String title, String lang, String from, String subject, String text, boolean isDefault, String model) throws ContentCreationException, ContentMgmtException;
	public EmailTemplate createEmailTemplateNoTrx(String key, String title, String lang, String from, String subject, String text, boolean isDefault, String model) throws ContentCreationException, ContentMgmtException;
	public EmailTemplate createEmailTemplateNoTrx(Domain domain, String key, String title, String lang, String from, String subject, String text, boolean isDefault, String model) throws ContentCreationException, ContentMgmtException;
	
	
	public KBFile createKBFile(String name) throws ContentCreationException, ContentMgmtException;
	public KBFile createKBFileNoTrx(String name) throws ContentCreationException, ContentMgmtException;
	public ResourceFolder createFolder(String name) throws ContentCreationException, ContentMgmtException;
	
	public Long getNewResourceOId();
	public Long getResourceNewOId();

}

package com.novamens.kbee.content.service.domain;

import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSetType;
import com.novamens.dom.Domain;
import com.novamens.event.EventService;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.command.CleanIndexCommand;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.logging.DomainDeleteEvent;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;


public class DeleteDomainCommand extends AsyncCommand {
				
	static private Logger txlogger = LogManager.getLogger("TxLogger");
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DeleteDomainCommand.class.getName());
	
	String domainId;
	String domainName;
	
	public DeleteDomainCommand(Domain domain) {
		this.domainId = String.valueOf(domain.getId());
		domainName = domain.getName();
	}
	
	
	/** ----------
	 * TODO IMPORTANTE -> REMOVER LOS KBFILE DEL OBJECT STORAGE ?
	 * ----------
	 */
	public void executeAsync() {
		Transaction transaction = null;
		try {
			
			com.novamens.hibernate.session.Session.open();
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@"+domainName);

			setDateStarted(OffsetDateTime.now());

			
			transaction = beginTransaction();
			
					execute("insert into kb_deleted_domain (id, name) values (?, (select name from domain where id = ?))");
					
					execute("delete from contentresource where content_id in (select id from content where domain_id=?)");
					execute("delete from idoc where content_id in (select id from content where domain_id=?)");
					execute("delete from content where domain_id=?");

					execute("delete from kb_signed_data where signature_id in (select id from kb_signature where domain_id=?)");
					execute("delete from kb_signed_file where signature_id in (select id from kb_signature where domain_id=?)");
					execute("delete from kb_signature where domain_id=?");
					execute("delete from kb_device where domain_id=?");

					execute("delete from wf_launcher where domain_id=?");
					execute("delete from wf_process where procedure_id in (select id from wf_procedure where domain_id=?)");
					execute("delete from wf_procedure where domain_id=?");
					execute("delete from kb_contentattribute where contenttemplate_id in (select id from kb_contenttemplate where domain_id=?)");
					execute("delete from kb_relation_template where targettemplate_id in (select id from kb_contenttemplate where domain_id=?)");
					
										
					execute("delete from kb_datasetattribute where attributetemplate_id in (select id from kb_attributetemplate where attribute_id in (select id from kb_attribute where domain_id=800))");
					execute("delete from kb_attributetemplate where attribute_id in (select id from kb_attribute where domain_id=?)");
					
					
					execute("update kb_role set classifier_id=null where domain_id=?");
					
					execute("delete from kb_ds_element_template where dataset_id in (select id from dataset where domain_id=?)");
					execute("delete from datasetclassifier where dataset_id in (select id from dataset where domain_id=?)");
					execute("delete from kb_contenttemplate where domain_id=?");
					execute("delete from memberclassification where classifier_id in (select id from kb_classifier where dataset_id in (select id from dataset where domain_id=?))");
					execute("delete from kb_classifier where domain_id=?");
					execute("delete from kb_classifier where dataset_id in (select id from dataset where domain_id=?)");
					execute("delete from kb_ds_element_template where dataset_id in (select id from dataset where domain_id=?)");
					execute("delete from kb_attribute where domain_id=?");
					execute("delete from kb_member_role where role_id in (select id from kb_role where domain_id=?)");
					execute("delete from kb_user_role where role_id in (select id from kb_role where domain_id=?)");
					execute("delete from datasetmember where domain_id=?");
					execute("delete from datasetmember where dataset_id in (select id from dataset where domain_id=?)");
					
					execute("delete from dataset where domain_id=? and type<>"+ String.valueOf(DataSetType.USER.getId()));
					
					execute("delete from kb_template_resource_tag where tag_id in (select id from kb_resource_tag where domain_id=?)");
					execute("delete from kb_resource_tag where domain_id=?");
					execute("delete from datasetmember where domain_id=?");
					execute("delete from kb_user_role where userprofile_id in (select id from profile where domain_id=?)");
					execute("delete from userprofile  where id in (select id from profile where domain_id=?);");
					execute("delete from profile  where domain_id=?");
					execute("delete from person where entity_id in (select id from entity  where domain_id=?)");
					
					execute("delete from entity  where domain_id=?");
					execute("delete from kb_role where domain_id=?");
					
					execute("delete from po_area where po_id in (select id from po_portalobject where domain_id=?)");
					execute("delete from po_page_section where po_id in (select id from po_portalobject where domain_id=?)");
					execute("delete from po_portalobject where domain_id=?");
					
					
					
					execute("delete from kb_security_rule where domain_id=?");
					execute("delete from kgroup where id in (select id from principal where domain_id=?)");
					execute("update kfile set uploadeduser=null where resource_id in (select id from kresource where domain_id=?)");
					execute("update kresource set lastmodifieduser=1, domain_id=null where domain_id=?");
					execute("update principal set lastmodifieduser=1 where domain_id=?");
					execute("delete from kb_email_template where domain_id=?");
					execute("delete from kb_cabinet where domain_id=?");
					
					execute("delete from kb_notification where domain_id=?");
					
					execute("delete from dataset where domain_id=? and type="+ String.valueOf(DataSetType.USER.getId()));
					execute("delete from users where id in (select id from principal where domain_id=?)");
					execute("delete from principal where domain_id=?");
					execute("delete from kb_facet_wrapper where domain_id=?");

					execute("delete from domain where id=?");
					execute("delete from logevent where event_domain_id=?");
					execute("delete from kb_import_data where local_domain ='"+domainName+"'");					
					
					ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
		
					//(new CleanIndexCommand("type:domain")).execute();
					//(new CleanIndexCommand("domain:"+domainId)).execute();
					
					txlogger.info(new DomainDeleteEvent(getDomain()));
			
			transaction.commit();
			
			getContentDao().cleanHibernateCache();
			ServiceLocator.getService(EventService.class).fire(new EvictCacheServiceEvent());
			
			(new CleanIndexCommand("type:domain", getDomain().getId().toString())).execute();
			(new CleanIndexCommand("domain:"+domainId, getDomain().getId().toString())).execute();
			
			end();
		}
		catch (Exception e) {
			transaction.rollback();
			logger.error(e);
			getLogger().error(e);
			setResult(e.getMessage());
			stop();
		}
		finally {
			com.novamens.hibernate.session.Session.close();
		}
	}
	
	private void execute(String statement) {
		
		statement = statement.replace("?", domainId);
		NativeQuery<?> query = getSessionFactory().getCurrentSession().createNativeQuery(statement);
		
		
		logger.debug(statement);
		
		query.executeUpdate();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}

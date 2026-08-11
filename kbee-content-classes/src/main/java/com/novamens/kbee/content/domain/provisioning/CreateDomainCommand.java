 package com.novamens.kbee.content.domain.provisioning;

import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.sql.DataSource;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.email.EmailService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.domain.DomainBuilderService;
import com.novamens.content.user.UserService;
import com.novamens.kbee.content.command.AsyncCommand;

import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.workflow.Task;

import kbee.api.service.ApiService;
import kbee.importer.*;

public class CreateDomainCommand extends AsyncCommand {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CreateDomainCommand.class.getName());
	
	
	private ApiService server;
	private String step;
	private String result = "";
	private int total = 9, progress = 0;
	
	public CreateDomainCommand(ApiService server) {
		setServer(server);
	}
	
	public void executeAsync() {
		Transaction transaction = null;
		try {
			
			com.novamens.hibernate.session.Session.open();
			
			setDateStarted(OffsetDateTime.now());

			transaction = beginTransaction();
				
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			DomainBuilderService domainbuilder = ServiceLocator.getService(DomainBuilderService.class);
			
			setStep("Schema");
			
			Domain domain = domainbuilder.createEmptyDomain(this.getNewDomainName(), getParameters());
			
			domainbuilder.setUpRolesPremium(domain, "premium-none");
			getContentDao().flush();
			
			setStep("Users and Roles");
			
			domainbuilder.setUpUsersPremium(domain, getParameters(), "premium-none");
				
			getContentDao().flush();
			
			if (getDomain()!=null) {
				ServiceLocator.getService(SecurityService.class).authenticate("root@"+domain.getName());
				importDataSets();
				importResourceTags();
				importLauncherGroups();
				importClassifiers();
				importAttributes();
				importStructures();
				importValues();
				importGroups();
				importLibraries();
				importTemplates();
				importRoles();
				importSettings();
				importFacets();
				importEMailTemplates();
			}
			
			if (domain.getDomainType()==DomainType.EXPRESS) {
				domainbuilder.setUpExpress(domain);
			}
				
			transaction.commit();
				
			end();	
		}
		catch (Exception e) {
			transaction.rollback();
			getLogger().error(e);
			setResult(e.getMessage());
			stop();
		}
		catch (Throwable e) {
			getLogger().error(e);
			setResult(e.getMessage());
			stop();
		}
		finally {
			com.novamens.hibernate.session.Session.close();
		}
	}
	
	public void setServer(ApiService server) {
		this.server = server;
	}
	
	public ApiService getServer() {
		return this.server;
	}
	
	public Domain getDomain() {
		return getParameter("domain")!=null ? getContentDao().findDomainById((Serializable)getParameter("domain"))  : null;
	}
	
	public Domain getLocalDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	public String getNewDomainName() {
		return (String)getParameter("name");
	}
	
	@Override
	public double getProgress() {
		return progress;
	}
	
	public String getStep() {
		return step;
	}
	
	
	@Override
	public String getResult() {
		return result;
	}
	
	protected int getTotal() {
		return total;
	}
	
	@Override
	protected void setResult(String result) {
		this.result += result;
	}
	
	protected void setStep(String step) {
		this.step = step;
	}
	
	protected void importTemplates() throws ContentMgmtException {
		executeImport(new TemplatesImporter(getServer(), getDomain(), getLocalMatcher() ) {
			@Override
			protected Task createTask() {
				return CreateDomainCommand.this.createTask();
			}
		});
	}
	
	protected void importEMailTemplates() throws ContentMgmtException {
		setStep("Templates");
		ServiceLocator.getService(EmailService.class).setUpTemplates(getLocalDomain());
		getContentDao().flush();
		executeImport(new EmailTemplatesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importDataSets() throws ContentMgmtException {
		setStep("DataSets");
		executeImport(new DataSetsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importClassifiers() throws ContentMgmtException {
		setStep("Classifiers");
		executeImport(new ClassifiersImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importAttributes() throws ContentMgmtException {
		setStep("Attributes");
		executeImport(new AttributesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importResourceTags() throws ContentMgmtException {
		setStep("Resources");
		executeImport(new ResourceTagsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importLauncherGroups() throws ContentMgmtException {
		setStep("Launcher Groups");
		executeImport(new LauncherGroupsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importLibraries() throws ContentMgmtException {
		setStep("Libraries");
		executeImport(new LibrariesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importGroups() throws ContentMgmtException {
		setStep("Groups");
		executeImport(new GroupsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importRoles() throws ContentMgmtException {
		setStep("Roles");
		executeImport(new RolesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importSettings() throws ContentMgmtException {
		setStep("Settings");
		executeImport(new SettingsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importStructures() throws ContentMgmtException {
		setStep("DataSet Structures");
		executeImport(new StructuresImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importValues() throws ContentMgmtException {
		setStep("Values");
		executeImport(new ValuesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importFacets() throws ContentMgmtException {
		setStep("Facets");
		executeImport(new FacetsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void executeImport(Importer importer) throws ContentMgmtException {
		importer.execute();
		setResult(importer.getResult());
	}
	
	protected Task createTask() {
		return null;
	}
	
	protected Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	private LocalMatcher getLocalMatcher() {
		ImportDao dao = new ImportDao();
		dao.setDataSource(getDataSource());
		dao.setServer(getServer().getUrl());
		dao.setLocalDomain(getLocalDomain());
		return dao;
	}
	
	private DataSource getDataSource() {
		return (DataSource)ServiceLocator.getService(BeansService.class).getBean("dataSource");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

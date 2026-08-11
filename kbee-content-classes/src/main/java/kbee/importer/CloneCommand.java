 package kbee.importer;

import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.sql.DataSource;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;
import com.novamens.content.dao.ContentDao;
import com.novamens.kbee.content.command.AsyncCommand;

import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.workflow.Task;

import kbee.api.service.ApiService;
import kbee.util.logging.Logger;

@Deprecated
public class CloneCommand extends AsyncCommand {

	private ApiService server;
	private Importer importer;
	private String result = "";
	private int total = 0, totalprogress = 0;
	
	private static Logger logger = Logger.getLogger(CloneCommand.class.getName());
	
	public CloneCommand(ApiService server) {
		setServer(server);
	}
	
	public void executeAsync() {
		try {
			
			com.novamens.hibernate.session.Session.open();
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@"+getDomain().getName());

			setDateStarted(OffsetDateTime.now());
			
			if ("true".equals(getParameter("datasets"))) {
				importDataSets();
			}
			if ("true".equals(getParameter("resourcetags"))) {
				importResourceTags();
			}
			if ("true".equals(getParameter("launchergroups"))) {
				importLauncherGroups();
			}
			if ("true".equals(getParameter("classifiers"))) {
				importClassifiers();
			}
			if ("true".equals(getParameter("attributes"))) {
				importAttributes();
			}
			if ("true".equals(getParameter("structures"))) {
				importStructures();
			}
			if ("true".equals(getParameter("values"))) {
				importValues();
			}
			if ("true".equals(getParameter("groups"))) {
				importGroups();
			}
			if ("true".equals(getParameter("libraries"))) {
				importLibraries();
			}
			if ("true".equals(getParameter("templates"))) {
				importTemplates();
			}
			if ("true".equals(getParameter("roles"))) {
				importRoles();
			}
			if ("true".equals(getParameter("settings"))) {
				importSettings();
			}
			if ("true".equals(getParameter("emailtemplates"))) {
				importEmailTemplates();
			}
			if ("true".equals(getParameter("facets"))) {
				importFacets();
			}
//			if ("true".equals(getParameter("files"))) {
//				importFiles();
//			}
			end();	
		}
		catch (Exception e) {
			getLogger().error(e);
			setResult(e.getMessage());
			stop();
		}
		catch (Throwable e) {
			e.printStackTrace();
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
		return getContentDao().findDomainById((Serializable)getParameter("domain"));
	}
	
	@Override
	public double getProgress() {
		try {
			double progress;
			if (importer==null)	{
				progress = totalprogress>0 ? (double)totalprogress/(double)getTotal()*100 : 0;
			}
			else {
				progress = (double)(totalprogress+importer.getProgress())/(double)getTotal()*100;
			}	
			return progress;
		}
		catch (Exception e) {
			e.printStackTrace();
			return totalprogress;
		}
	}
	
	@Override
	public String getResult() {
		return result;
	}
	
	protected int getTotal() {
		if (total == 0) {
			if ("true".equals(getParameter("groups"))) {
				total += (new GroupsImporter(getServer(), getDomain(), null)).getTotal();
			}
			if ("true".equals(getParameter("templates"))) {
				total += (new TemplatesImporter(getServer(), getDomain(), getLocalMatcher())).getTotal();
			}
			if ("true".equals(getParameter("datasets"))) {
				total += (new DataSetsImporter(getServer(), getDomain(), getLocalMatcher())).getTotal();
			}
			if ("true".equals(getParameter("classifiers"))) {
				total += (new ClassifiersImporter(getServer(), getDomain(), getLocalMatcher())).getTotal();
			}
			if ("true".equals(getParameter("values"))) {
				total += (new ValuesImporter(getServer(), getDomain(), getLocalMatcher())).getTotal();
			}
			if ("true".equals(getParameter("files"))) {
				String criteria = (String)getParameter("criteria");
				total += (new FilesImporter(getServer(), getDomain(), criteria)).getTotal();
			}
		}
		return total;
	}
	
	@Override
	protected void setResult(String result) {
		this.result += result;
	}
	
	protected void importGroups() throws ContentMgmtException {
		executeImport(new GroupsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importTemplates() throws ContentMgmtException {
		executeImport(new TemplatesImporter(getServer(), getDomain(), getLocalMatcher() ) {
			@Override
			protected Task createTask() {
				return CloneCommand.this.createTask();
			}
		});
	}
	
	protected void importDataSets() throws ContentMgmtException {
		executeImport(new DataSetsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importClassifiers() throws ContentMgmtException {
		executeImport(new ClassifiersImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importAttributes() throws ContentMgmtException {
		executeImport(new AttributesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importResourceTags() throws ContentMgmtException {
		executeImport(new ResourceTagsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importLauncherGroups() throws ContentMgmtException {
		executeImport(new LauncherGroupsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importLibraries() throws ContentMgmtException {
		executeImport(new LibrariesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importRoles() throws ContentMgmtException {
		executeImport(new RolesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importEmailTemplates() throws ContentMgmtException {
		executeImport(new EmailTemplatesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importStructures() throws ContentMgmtException {
		executeImport(new StructuresImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importValues() throws ContentMgmtException {
		executeImport(new ValuesImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importSettings() throws ContentMgmtException {
		executeImport(new SettingsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importFacets() throws ContentMgmtException {
		executeImport(new FacetsImporter(getServer(), getDomain(), getLocalMatcher()));
	}
	
	protected void importFiles() throws ContentMgmtException {
		String criteria = (String)getParameter("criteria");
		FilesImporter importer = new FilesImporter(getServer(), getDomain(), criteria) {
			@Override
			public boolean isRunning() {
				return CloneCommand.this.isRunning();
			}
		};
		
		try {
			String maxfilesstring = (String)getParameter("maxfiles");
			if (maxfilesstring!=null && !"".equals(maxfilesstring.trim())) {
				int maxfiles = Integer.valueOf(maxfilesstring.trim());
				importer.setMaxFiles(maxfiles);
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		this.importer = importer;
		//importer.setLogger(getLogger());
		importer.execute();
		setResult(importer.getResult());
		totalprogress += importer.getProgress();
		this.importer = null;
	}
	
	protected void executeImport(Importer importer) throws ContentMgmtException {
		Transaction transaction = null;
		try {
			transaction = beginTransaction();
			this.importer = importer;
			importer.execute();
			setResult(importer.getResult());
			totalprogress += importer.getProgress();
			transaction.commit();
		}
		catch(Exception e) {
			transaction.rollback();
			logger.error(e);
		}
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
		dao.setLocalDomain(getDomain());
		return dao;
	}
	
	private DataSource getDataSource() {
		return (DataSource)ServiceLocator.getService(BeansService.class).getBean("dataSource");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

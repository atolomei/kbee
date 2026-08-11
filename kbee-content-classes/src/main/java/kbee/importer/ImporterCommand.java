 package kbee.importer;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;
import com.novamens.content.dao.ContentDao;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Deprecated
public class ImporterCommand extends AsyncCommand {

	private KbeeApiService server;
	private Importer importer;
	private String result = "";
	private int total = 0, totalprogress = 0; 
	
	public void executeAsync() {
		try {
			
			com.novamens.hibernate.session.Session.open();
			
			//setLogger("logs/importer-command.log");
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@"+getDomain().getName());

			setDateStarted(OffsetDateTime.now());
			
			setServer(new KbeeApiService((String)getParameter("server"), (String)getParameter("user"), (String)getParameter("password")));
			
			if ("true".equals(getParameter("groups"))) {
				importGroups();
			}
			
			if ("true".equals(getParameter("templates"))) {
				importTemplates();
			}
			
			if ("true".equals(getParameter("datasets"))) {
				importDataSets();
			}
			
			if ("true".equals(getParameter("classifiers"))) {
				importClassifiers();
			}
			
			if ("true".equals(getParameter("values"))) {
				importValues();
			}
			
			if ("true".equals(getParameter("users"))) {
				importUsers();
			}
			
			if ("true".equals(getParameter("emailtemplates"))) {
				importEmailTemplates();
			}
			
			if ("true".equals(getParameter("facets"))) {
				importFacets();
			}

			
			if ("true".equals(getParameter("files"))) {
				importFiles();
			}
			
			end();	
		}
		catch (Exception e) {
			e.printStackTrace();
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
	
	public void setServer(KbeeApiService server) {
		this.server = server;
	}
	
	public KbeeApiService getServer() {
		return this.server;
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
				total += (new TemplatesImporter(getServer(), getDomain(), null)).getTotal();
			}
			if ("true".equals(getParameter("datasets"))) {
				total += (new DataSetsImporter(getServer(), getDomain(), null)).getTotal();
			}
			if ("true".equals(getParameter("classifiers"))) {
				total += (new ClassifiersImporter(getServer(), getDomain(), null)).getTotal();
			}
			if ("true".equals(getParameter("values"))) {
				total += (new ValuesImporter(getServer(), getDomain(), null)).getTotal();
			}
			if ("true".equals(getParameter("users"))) {
				total += (new UsersImporter(getServer(), getDomain(), null)).getTotal();
			}
//			if ("true".equals(getParameter("rules"))) {
//				total += (new RulesImporter(getServer(), getDomain())).getTotal();
//			}
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
		executeImport(new GroupsImporter(getServer(), getDomain(), null));
	}
	
	protected void importTemplates() throws ContentMgmtException {
		executeImport(new TemplatesImporter(getServer(), getDomain(), null));
	}
	
	protected void importDataSets() throws ContentMgmtException {
		executeImport(new DataSetsImporter(getServer(), getDomain(), null));
	}
	
	protected void importClassifiers() throws ContentMgmtException {
		executeImport(new ClassifiersImporter(getServer(), getDomain(), null));
	}
	
	protected void importValues() throws ContentMgmtException {
		executeImport(new ValuesImporter(getServer(), getDomain(), null));
	}
	
	protected void importUsers() throws ContentMgmtException {
		executeImport(new UsersImporter(getServer(), getDomain(), null));
	}
	
	protected void importEmailTemplates() throws ContentMgmtException {
		executeImport(new EmailTemplatesImporter(getServer(), getDomain(), null));
	}
	
	protected void importFacets() throws ContentMgmtException {
		executeImport(new FacetsImporter(getServer(), getDomain(), null));
	}
	
	protected void importFiles() throws ContentMgmtException {
		String criteria = (String)getParameter("criteria");
		FilesImporter importer = new FilesImporter(getServer(), getDomain(), criteria) {
			@Override
			public boolean isRunning() {
				return ImporterCommand.this.isRunning();
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
		importer.execute();
		setResult(importer.getResult());
		totalprogress += importer.getProgress();
		this.importer = null;
	}
	
	protected void executeImport(Importer importer) throws ContentMgmtException {
		this.importer = importer;
		importer.execute();
		setResult(importer.getResult());
		totalprogress += importer.getProgress();
		this.importer = null;
	}
	
	protected boolean getFreeze() {
		return "true".equals(getParameter("freeze"));
	}
	
	
	public Domain getDomain() {
		return getContentDao().findDomainById((Serializable)getParameter("domain"));
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

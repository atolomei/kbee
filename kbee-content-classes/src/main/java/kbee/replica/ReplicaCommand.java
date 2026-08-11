package kbee.replica;

import java.time.OffsetDateTime;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AsyncCommand;

import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;

import kbee.api.model.ApiValue;
import kbee.api.model.ApiClassifier;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IFacet;
import kbee.api.model.IGroup;
import kbee.api.model.ILauncherGroup;
import kbee.api.model.ILibrary;
import kbee.api.model.IModelAttribute;
import kbee.api.model.IResourceTag;
import kbee.api.model.IRole;
import kbee.api.model.ITemplate;
import kbee.util.logging.Logger;

public class ReplicaCommand extends AsyncCommand {

	private Replica replica;
	private Replicator<?> replicator;
	private String result = "";
	private int total = 0, totalprogress = 0;
	
	private static Logger logger = Logger.getLogger(ReplicaCommand.class.getName());
	
	public ReplicaCommand(Replica replica) {
		setReplica(replica);
	}
	
	public Replica getReplica() {
		return replica;
	}


	public void setReplica(Replica replica) {
		this.replica = replica;
	}

	public void executeAsync() {
		try {
			
			com.novamens.hibernate.session.Session.open();
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@"+getDomain().getName());

			setDateStarted(OffsetDateTime.now());
			
			if ("true".equals(getParameter("datasets"))) {
				replicate(getDataSetsReplicator());
			}
			if ("true".equals(getParameter("resourcetags"))) {
				replicate(getResourceTagsReplicator());
			}
			if ("true".equals(getParameter("launchergroups"))) {
				replicate(getLauncherGroupsReplicator());
			}
			if ("true".equals(getParameter("classifiers"))) {
				replicate(getClassifiersReplicator());
			}
			if ("true".equals(getParameter("attributes"))) {
				replicate(getAttributesReplicator());
			}
			if ("true".equals(getParameter("values"))) {
				replicate(getDataSetMembersReplicator());
			}
			if ("true".equals(getParameter("groups"))) {
				replicate(getGroupsReplicator());
			}
			if ("true".equals(getParameter("libraries"))) {
				replicate(getLibrariesReplicator());
			}
			if ("true".equals(getParameter("templates"))) {
				replicate(getTemplatesReplicator());
			}
			if ("true".equals(getParameter("roles"))) {
				replicate(getRolesReplicator());
			}
//			if ("true".equals(getParameter("settings"))) {
//				importSettings();
//			}
//			if ("true".equals(getParameter("emailtemplates"))) {
//				importEmailTemplates();
//			}
			if ("true".equals(getParameter("facets"))) {
				replicate(getFacetsReplicator());
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

	@Override
	public double getProgress() {
		try {
			double progress;
			if (replicator==null)	{
				progress = totalprogress>0 ? (double)totalprogress/(double)getTotal()*100 : 0;
			}
			else {
				progress = (double)(totalprogress+replicator.getProgress())/(double)getTotal()*100;
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
	
	@Override
	public Domain getDomain() {
		return getReplica().getDomain();
	}
	
	public boolean forceUpdate() {
		return "true".equals(getParameter("force"));
	}
	
	protected int getTotal() {
		if (total == 0) {
			if ("true".equals(getParameter("groups"))) {
				total += getGroupsReplicator().getTotal();
			}
			if ("true".equals(getParameter("templates"))) {
				total += getTemplatesReplicator().getTotal();
			}
			if ("true".equals(getParameter("datasets")) || "true".equals(getParameter("structures"))) {
				total += getDataSetsReplicator().getTotal();
			}
			if ("true".equals(getParameter("classifiers"))) {
				total += getClassifiersReplicator().getTotal();
			}
			if ("true".equals(getParameter("attributes"))) {
				total += getAttributesReplicator().getTotal();
			}
			if ("true".equals(getParameter("resourcetags"))) {
				total += getResourceTagsReplicator().getTotal();
			}
			if ("true".equals(getParameter("launchergroups"))) {
				total += getLauncherGroupsReplicator().getTotal();
			}
			if ("true".equals(getParameter("values"))) {
				total += getDataSetMembersReplicator().getTotal();
			}
			if ("true".equals(getParameter("libraries"))) {
				total += getLibrariesReplicator().getTotal();
			}	
			if ("true".equals(getParameter("roles"))) {
				total += getRolesReplicator().getTotal();
			}
			if ("true".equals(getParameter("facets"))) {
				total += getFacetsReplicator().getTotal();
			}	
		}
		return total;
	}
	
	@Override
	protected void setResult(String result) {
		this.result += result;
	}

	protected Replicator<?> getGroupsReplicator() throws ContentMgmtException {
		return new ResultSetReplicator<IGroup>(getReplica(),
			(api) -> api.getGroups(),
			(remoteObject) -> new GroupReplicaHandler(getReplica(), remoteObject),
			(proxy) ->  getReplica().getApi().get(IGroup.class, proxy.getHRef()));
	}
	
	protected Replicator<?> getDataSetsReplicator() throws ContentMgmtException {
		return new ListReplicator<ApiDataSet>(getReplica(),
			(api) -> api.getDataSets(),
			(remoteObject) -> new DataSetReplicaHandler(getReplica(), remoteObject));
	}
	
	protected Replicator<?> getClassifiersReplicator() throws ContentMgmtException {
		return new ListReplicator<ApiClassifier>(getReplica(),
			(api) -> api.getClassifiers(),
			(remoteObject) -> new ClassifierReplicaHandler(getReplica(), remoteObject));
	}
	
	protected Replicator<?> getAttributesReplicator() throws ContentMgmtException {
		return new ListReplicator<IModelAttribute>(getReplica(),
			(api) -> api.getAttributes(),
			(remoteObject) -> new AttributeReplicaHandler(getReplica(), remoteObject));
	}
	
	protected Replicator<?> getResourceTagsReplicator() throws ContentMgmtException {
		return new ListReplicator<IResourceTag>(getReplica(),
			(api) -> api.getResourceTags(),
			(remoteObject) -> new ResourceTagReplicaHandler(getReplica(), remoteObject));
	}
	
	protected Replicator<?> getLauncherGroupsReplicator() throws ContentMgmtException {
		return new ListReplicator<ILauncherGroup>(getReplica(),
			(api) -> api.getLauncherGroups(),
			(remoteObject) -> new LauncherGroupReplicaHandler(getReplica(), remoteObject));
	}
	
	protected Replicator<?> getDataSetMembersReplicator() throws ContentMgmtException {
		return new ResultSetReplicator<ApiValue>(getReplica(),
			(api) -> new IValuesSet(api),
			(remoteObject) -> new DataSetMemberReplicaHandler(getReplica(), remoteObject));
	}

	protected Replicator<?> getLibrariesReplicator() throws ContentMgmtException {
		return new ListReplicator<ILibrary>(getReplica(),
			(api) -> api.getLibraries(),
			(remoteObject) -> new LibraryReplicaHandler(getReplica(), remoteObject));
	}
	
	protected Replicator<?> getRolesReplicator() throws ContentMgmtException {
		return new ResultSetReplicator<IRole>(getReplica(),
			(api) -> api.getRoles(),
			(remoteObject) -> new RoleReplicaHandler(getReplica(), remoteObject),
			(proxy) ->  getReplica().getApi().get(IRole.class, proxy.getHRef()));
	}
	
	protected Replicator<?> getTemplatesReplicator() throws ContentMgmtException {
		return new ListReplicator<ITemplate>(getReplica(),
			(api) -> api.getTemplates(),
			(remoteObject) -> new TemplateReplicaHandler(getReplica(), remoteObject, forceUpdate()));
	}

	
//	protected void importEmailTemplates() throws ContentMgmtException {
//		executeImport(new EmailTemplatesImporter(getServer(), getDomain(), getLocalMatcher()));
//	}
//	
//	protected void importSettings() throws ContentMgmtException {
//		executeImport(new SettingsImporter(getServer(), getDomain(), getLocalMatcher()));
//	}
//
	
	protected Replicator<?> getFacetsReplicator() throws ContentMgmtException {
		return new ListReplicator<IFacet>(getReplica(),
			(api) -> api.getFacets(),
			(remoteObject) -> new FacetReplicaHandler(getReplica(), remoteObject));
	}

	
//	protected void importFiles() throws ContentMgmtException {
//		String criteria = (String)getParameter("criteria");
//		FilesImporter importer = new FilesImporter(getServer(), getDomain(), criteria) {
//			@Override
//			public boolean isRunning() {
//				return ReplicaCommand.this.isRunning();
//			}
//		};
//		
//		try {
//			String maxfilesstring = (String)getParameter("maxfiles");
//			if (maxfilesstring!=null && !"".equals(maxfilesstring.trim())) {
//				int maxfiles = Integer.valueOf(maxfilesstring.trim());
//				importer.setMaxFiles(maxfiles);
//			}
//		}
//		catch (NumberFormatException e) {
//			e.printStackTrace();
//		}
//		
//		this.importer = importer;
//		//importer.setLogger(getLogger());
//		importer.execute();
//		setResult(importer.getResult());
//		totalprogress += importer.getProgress();
//		this.importer = null;
//	}
//	
	protected void replicate(Replicator<?> replicator) throws ContentMgmtException {
		Transaction transaction = null;
		try {
			transaction = beginTransaction();
			this.replicator = replicator;
			replicator.replicate();
			setResult(replicator.getResult());
			if (totalprogress + replicator.getProgress() < total)
			totalprogress += replicator.getProgress();
			transaction.commit();
		}
		catch(Exception e) {
			transaction.rollback();
			setResult(replicator.getResult());
			logger.error(e);
		}
	}
}

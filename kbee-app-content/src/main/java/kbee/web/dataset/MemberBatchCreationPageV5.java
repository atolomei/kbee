package kbee.web.dataset;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.page.ApplicationMenuSection;
import kbee.web.page.ApplicationPage;

		
public class MemberBatchCreationPageV5 extends ApplicationPage<DataSet> {
			
	private static final long serialVersionUID = 1L;

	IModel<DataSet> model;
	IModel<DataSetMember> datasetmember_model;
	
	public MemberBatchCreationPageV5() {
		throw new KbeeRuntimeException("To be completed.");
	}
	
	public MemberBatchCreationPageV5(PageParameters parameters) {
		super();
		
		DataSet dataset= getDataSet(parameters);
		
		if (dataset!=null)
			setModel(new ObjectModel<DataSet>(dataset));
		
		addComponents(getModel());
	}

	
	public MemberBatchCreationPageV5(IModel<DataSet> dataset_model) {
		this(dataset_model,null);
	}
	
	
	public MemberBatchCreationPageV5(IModel<DataSet> dataset_model, IModel<DataSetMember> datasetmember_model) {
		setModel(dataset_model);
		setAggregator(datasetmember_model);
		addComponents(dataset_model);
	}
	
	
	public IModel<DataSetMember> getAggregator() {
		 return datasetmember_model;
	}
	
	public void setAggregator( IModel<DataSetMember> datasetmember_model) {
			this.datasetmember_model=datasetmember_model;
	}
	
	protected DataSet getDataSet(PageParameters parameters) {
		DataSet dataset = null;
		try {
			
			StringValue id = parameters.get("id");
			if (!id.isNull() && !id.isEmpty()) {
					dataset = (DataSet) getContentDao().findModelObjectById(DataSet.class, id.toString());
					if (dataset!=null && !dataset.getDomain().equals(getDomain())) {
						dataset = null;
					}
				}
	
		} catch (Exception e ) {
			// logger.error(e);
			
		}
		return dataset;
	}

	
	public void setModel(IModel<DataSet> model) {
		 this.model=model;
	}
	
	public IModel<DataSet> getModel() {
		return model;
	}

	@Override
	public void onDetach() {
		if (model!=null)
			model.detach();
		
		if (datasetmember_model!=null)
			datasetmember_model.detach();
			
		super.onDetach();
	}

	 
 	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.SETTINGS;
	}

	
	@Override
	protected boolean hasPermissions() {
		final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
		final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		// final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
		final boolean has_permission = is_root || is_domain_admin;				
		return has_permission;
	}

	
	private void addComponents(IModel<DataSet> dataset_model) {

		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
		
		if (dataset_model!=null && hasPermissions()) {
			setPageTitle( new Model<String>(dataset_model.getObject().getName()));
			add (new MemberBatchCreationPanelV5("editor", dataset_model, getAggregator()) {
				private static final long serialVersionUID = 1L;
				protected void onClose() {
					
					if (datasetmember_model!=null &&  datasetmember_model.getObject()!=null) {
						MemberPage page=new MemberPage(datasetmember_model);
						// page.setInitialTab("3");  
						setResponsePage(page);
						
					}
					else {
						setResponsePage(new DataSetMembersPage(getModel()));
					}
				}
			});
			getPageParameters().set("id", getModel().getObject().getId());			
		}
		else {
			addOrReplace(new Label("editor", "Permission issue."));
		}
	}
}

package kbee.web.nav;



import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;

import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.service.ApplicationSiteMapService;

public class MembersBC extends BCElement {
	
	private static final long serialVersionUID = 1L;
	
	private IModel<DataSet> model;

	public MembersBC(DataSet dataset) {
		model = new ObjectModel<DataSet>(dataset);
	}
	

	@Override
	public void onClick() {
		if (model!=null)
			setResponsePage(getDataSetPage(model.getObject()));
		else
			setResponsePage(new ApplicationErrorPage<>(new Model<String>("dataset model is null")));
	}
	
	@Override
	public IModel<String> getLabel() {
			if (model!=null) {
				return new Model<String>(model.getObject().getDisplayName());
			}
			else 
				return super.getLabel();
	}
	
	@Override
	public void onDetach() {
		if (model!=null)
			model.detach();
		super.onDetach();
	}
	

	private Page getDataSetPage(DataSet dataset) {
	    PageParameters pa= new PageParameters();
	    pa.add("id", dataset.getId().toString());
	    // pa.add("id", "79650");
		return ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-page", pa);
	}
	
	/**
	private DataSet getDataSet(String name) {
		DataSet dataSet = null;
		Serializable domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain().getId();
		for(DataSet ds : getContentDao().getDataSets(domain)) {
			if (ds.getName().toLowerCase().replaceAll(" ", "_").equals(name)) {
				dataSet = ds;
				break;
			}
		};
		return dataSet;
	}
	**/
	
	
	// private DataSetMembersPage getDataSetPage() {
	
	/**
	private Page getDataSetPage() {
	
		try {
			String d_id = getUser().getService(PreferencesService.class).getValue("dataset-member-selected", "dataset");
			
			if (d_id==null) {
				setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-page"));
			}
			else {
			   //String d_id = getDataSet(name).getId().toString();
			PageParameters pa= new PageParameters();
			    pa.add("id", d_id);
			    setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-page", pa));
			    
			    // ----
				// return new DataSetMembersPage(new ObjectModel<DataSet>(getDataSet(name), true));
			    // ----
			}
			    
			
		}
		catch (RuntimeException e) {
			String name = getDefaultDataSetName();
			if (name!=null && getDataSet(name)!=null) {

				// return new DataSetMembersPage(new ObjectModel<DataSet>(getDataSet(name), true));
				
			    String d_id = getDataSet(name).getId().toString();
			    PageParameters pa= new PageParameters();
			    pa.add("id", d_id);
			    setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("settings-dataset-members-page", pa));

			}
		}
		return null;
	}
	**/
	
	
	/**
	private String getDefaultDataSetName() {
		DataSet xdst = null;
		for (DataSet dataSet : getDataSets()) {
			if (    dataSet.getDataSetType().equals(DataSetType.STRING) ||
					dataSet.getDataSetType().equals(DataSetType.USERSUBSET) ||
					dataSet.getDataSetType().equals(DataSetType.ENTITY) ||
					dataSet.getDataSetType().equals(DataSetType.SECURED))  {
				xdst = dataSet;
				break;
			}
		}
		String name = xdst.getName()!=null ? xdst.getName().toLowerCase().replaceAll(" ", "_") : null;
		if (name!=null)
			getUser().getService(PreferencesService.class).setValue("useractions", "dataset", name);
		return name;
	}

	
	private List<DataSet> getDataSets() {
		return getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain());
	}
	
	
	private KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	*/
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

package kbee.web.nav;



import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
// import com.novamens.content.web.model.markup.DataSetsPage;
import com.novamens.dom.Domain;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;

public class SitesDropdownBC extends DropDownMenuBC<Void> {

	
	private static final long serialVersionUID = 1L;
	
	private List<IModel<Site>> site_list_model;
	
	public void onDetach() {
		super.onDetach();
		for (IModel<Site> m: getSites()) {
			m.detach();
		}
	}
	
	
	public SitesDropdownBC() {
	
		addElement(new SitesBC());
		
		if ( getSites().size()>0) {
			addElement( new BCElement("bc.dataset.members") {
				public void onClick() {
					// setResponsePage(new DataSetMembersPage( getDataSets().get(0)));
					//setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("model-datasets-page"));
				}
				private static final long serialVersionUID = 1L;
			});
		}
	}
	
		
				 
	
	
	/**
	 * 
	 * 
	 */
	public List<IModel<Site>> getSites() {
		if (this.site_list_model!=null)
			return this.site_list_model;
		
		this.site_list_model = new ArrayList<IModel<Site>>();
		
		/**
		for (Site dataset: getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain())) {
			if (dataset.getDataSetType()== DataSetType.STRING   ||
				dataset.getDataSetType()== DataSetType.EXTERNAL ||
				dataset.getDataSetType()== DataSetType.ENTITY 	||
				dataset.getDataSetType()== DataSetType.LABEL 	||
				dataset.getDataSetType()== DataSetType.PEOPLE)
			this.datasetlist_model.add(new ObjectModel<DataSet>(dataset));
		}
		*/
		return this.site_list_model;
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
//	@SuppressWarnings("unused")
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}

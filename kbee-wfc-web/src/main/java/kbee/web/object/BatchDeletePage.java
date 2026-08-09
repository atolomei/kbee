package kbee.web.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;


import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.content.user.UserService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.DataSetMembersBC;
import kbee.web.nav.DataSetMembersSectionBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;
			

/** 
 * 
 * {@link DataSetMemberMembersConsole}
 * 
 *  Batch Delete Tasks no usa esta página usa 
 *  {@link GenericBatchActionPage}
 *   
 * @param <T>
 */
public class BatchDeletePage<T> extends ApplicationPage<T> {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<T>> selection;
	private String selectionLabel;
	private IModel<String> section;
	
	
	
	/**
	 * @param selectionLabel
	 * @param selection
	 * @param section
	*/ 
	public BatchDeletePage(String selectionLabel, List<IModel<T>> selection, List<BCElement> list) {
		setSelection(selection);
		setSection(section);
		this.selectionLabel = selectionLabel;
	}
	
	@Override 
	public void onInitialize() {
		super.onInitialize();
		
		setPageTitle(new StringResourceModel("bc.batchdatasetvaluesdelete", this, null));
		setPageDescription(getPageTitle());
		
		setTopNavigation(getMainTopbar()); 	
		setMenu(getMainLaternalMenu()); 	
		
		PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>(null);
		panel.setTitle(new StringResourceModel("bc.batchdatasetvaluesdelete", this, null));
		
		MenuBreadCrumbPanel<Void>  bc = new MenuBreadCrumbPanel<Void>();
		bc.addElement(new SettingsDropDownBC());
		
		DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
		dd.addElement(new BCElement("bc.dataset.members"), true);
		dd.addElement(new DataSetMembersSectionBC());
		dd.addElement(new SeparatorBC());
		for (IModel<DataSet> ds: getDataSets()) 
			 dd.addElement( new DataSetMembersBC(ds));
		bc.addElement(dd);
		
		
		//DropdownMenuBC<?> dd = new DropdownMenuBC<Void>();
		//dd.addElement(new BCElement("bc.dataset.members"), true);
		//dd.addElement(new DataSetMembersSectionBC());
		//dd.addElement(new SeparatorBC());
		//		
		//for (DataSet ds: getDataSets()) 
		//	 dd.addElement( new DataSetMembersBC(ds));
		//bc.addElement(dd);
		//if (getAggregatorModel()!=null) {
		//	bc.addElement(new  HREFBCElement("bc-menu-item", "/dataset/"+getAggregatorModel().getObject().getDataSet().getId().toString(), new Model<String>(getAggregatorModel().getObject().getDataSet().getName())));
		//	bc.addElement(new BCElement( new Model<String>(getAggregatorModel().getObject().getName())));
		//}
		//bc.addElement(new  HREFBCElement("bc-menu-item", "/dataset/"+getModel().getObject().getDataSet().getId().toString(), new Model<String>(getModel().getObject().getDataSet().getName())));
		//bc.addElement(new BCElement( new Model<String>(getModel().getObject().getName())));
		
		bc.addElement(new BCElement(new StringResourceModel("bc.batchdatasetvaluesdelete", this, null)));
		panel.setBreadcrumbPanel(bc);

		
		setSearchPanel(false);
		setClearAllSearch(false);
		setAdvancedSearch(false);
		setSuggester(false);
		setPageContentHeader(panel);

		
		
		addComponents();
	}

	
	public  List<IModel<DataSet>> getDataSets() {
		List<IModel<DataSet>> datasetlist = new ArrayList<IModel<DataSet>>();
		for (DataSet dataset: getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain())) {
			if (dataset.getDataSetType()== DataSetType.STRING   ||
				dataset.getDataSetType()== DataSetType.EXTERNAL ||
				dataset.getDataSetType()== DataSetType.ENTITY 	||
				dataset.getDataSetType()== DataSetType.SECURED 	||
				dataset.getDataSetType()== DataSetType.LABEL 	||
				dataset.getDataSetType()== DataSetType.PEOPLE)
			datasetlist.add( new ObjectModel<DataSet>(dataset));
		}
		return datasetlist;
	}
	
	/**
protected Panel getHeaderPanelBreadcrumbPanel() {
		
		try {
			
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			bc.addElement( new HomeBC());
			bc.addElement( new SettingsDropdownBC());
			DropdownMenuBC<?> dd = new DropdownMenuBC<>();
			dd.addElement(new BCElement("bc.dataset.members"), true);
			dd.addElement(new DataSetMembersSectionBC());
			dd.addElement(new SeparatorBC());
			for (IModel<DataSet> ds: getItems()) 
				 dd.addElement( new DataSetMembersBC(ds.getObject())); 
			bc.addElement(dd);
			bc.addElement(new BCElement("bc.dataset.members.home"));
			return bc;
			
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}
	**/
	

	@Override
	public void onDetach() {
		if (section!=null)
			section.detach();
		for (IModel<T> model : getSelection()) 
			model.detach();
		
		//if (get("bc2")!=null) {
		//	get("bc2").detach();
		//}
		
		super.onDetach();
	}

	 

	protected void onClose() {}

	 

	public void setSelection(List<IModel<T>> selection) {
		this.selection = selection;
	}

	
	public List<IModel<T>> getSelection() {
		return selection;
	}
	
	 
	
/**
 * 
 */
	private void addComponents() {
											
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());  
		
	 	
		add(new BatchDeletePanel<T>("editor", selectionLabel, getSelection()) {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onClose() {
				BatchDeletePage.this.onClose();
			}
			@Override
			protected String executeDelete(IModel<T> model) {
				return BatchDeletePage.this.executeDelete(model);
			}
		});
		
		
		
		
		
		
		
		
		
		
		
	}

	protected String executeDelete(IModel<T> model) {
		return null;
	}
	 

	private void setSection(IModel<String> section2) {
		this.section=section2;
		
	}

}


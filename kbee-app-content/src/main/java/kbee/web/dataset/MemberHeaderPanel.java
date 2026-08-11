package kbee.web.dataset;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.user.UserService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.MembersBC;
import kbee.web.nav.SettingsDropDownBC;
			
public class MemberHeaderPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private IModel<DataSetMember> model;
	private IModel<DataSetMember> aggregatormodel;
	
	public MemberHeaderPanel(IModel<DataSetMember> model, IModel<DataSetMember> aggregatormodel) {
		super("member-panel");
		setOutputMarkupId(true);
		setModel(model);
		setAggregatorModel(aggregatormodel);
		add(new Label("name", new PropertyModel<String>(model, "displayName")));
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		target.add(this);
	}
	
	public void onInitialize() {
		super.onInitialize();
		MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<Void>("dm-breadcrumb");
		bc.addElement(new SettingsDropDownBC());
		DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
		dd.addElement(new BCElement("bc.dataset.members"), true);
		for (DataSet ds: getDataSets()) 
			dd.addElement(new MembersBC(ds));
		bc.addElement(dd);
		if (getAggregatorModel()!=null) {
			bc.addElement(new MembersBC(getAggregatorModel().getObject().getDataSet()));
			bc.addElement(new BCElement( new Model<String>(getAggregatorModel().getObject().getName())));
		}
		bc.addElement(new MembersBC(model.getObject().getDataSet()));
		bc.addElement(new BCElement( new Model<String>(model.getObject().getName())));
		add(bc);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (getModel()!=null)
			getModel().detach();
		if (getAggregatorModel()!=null)
			getAggregatorModel().detach();
	}
	
	protected void setModel(IModel<DataSetMember> model) {
		this.model = model;
	}
	
	protected IModel<DataSetMember> getModel() {
		return this.model;
	}
	
	protected void setAggregatorModel(IModel<DataSetMember> model) {
		this.aggregatormodel = model;
	}
	
	protected IModel<DataSetMember> getAggregatorModel() {
		return this.aggregatormodel;
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	public  List<DataSet> getDataSets() {
		List<DataSet> datasetlist = new ArrayList<DataSet>();
		for (DataSet dataset: getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain())) {
			if (dataset.getDataSetType()== DataSetType.STRING   ||
				dataset.getDataSetType()== DataSetType.EXTERNAL ||
				dataset.getDataSetType()== DataSetType.ENTITY 	||
				dataset.getDataSetType()== DataSetType.LABEL 	||
				dataset.getDataSetType()== DataSetType.PEOPLE)
			datasetlist.add(dataset);
		}
		return datasetlist;
	}
	
}

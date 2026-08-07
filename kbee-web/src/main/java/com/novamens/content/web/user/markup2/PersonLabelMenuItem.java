package com.novamens.content.web.user.markup2;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LabelMember;
import com.novamens.content.service.DOMObjectService;

import com.novamens.kbee.content.model.KbeeMemberClassification;
import com.novamens.kbee.wicket.markup.html.console.panel.ObjectLabelMenuItem;

public class PersonLabelMenuItem extends  ObjectLabelMenuItem<Person> {
				
	private static final long serialVersionUID = 1L;
	
	IModel<DataSetMember> datasetmember_model;
	
	
	static Logger logger = LogManager.getLogger(PersonLabelMenuItem.class.getName());

	private Boolean isenabled;

	public PersonLabelMenuItem(String id, IModel<LabelMember> model, IModel<Person> person_model, IModel<DataSetMember> object_model) {
		super(id, model, person_model);
		this.datasetmember_model=object_model;
	}
	

	
	public IModel<DataSetMember> getDatasetMemberModel() {
		return this.datasetmember_model;
	}
	
	
	@Override
	protected boolean isEnabledLabelMember() {
		if (isenabled!=null)
			return isenabled.booleanValue();
		for (Classification c: getDatasetMemberModel().getObject().getClassification()) {
			if (c.getDataSetMember()!=null && c.getDataSetMember().equals(getLabelMemberModel().getObject())) {
				isenabled=Boolean.valueOf(true);
				break;
			}
		}
		if (isenabled==null)
			isenabled=Boolean.valueOf(false);
		
		return isenabled.booleanValue();
	}

	/**
	 * 
	 */
	@Override
	protected void checkLabelMember() {
		isenabled = Boolean.valueOf(!isEnabledLabelMember());
		try {
			// Remove Tag
			if (!isenabled) {
				for (Classification c: getDatasetMemberModel().getObject().getClassification()) {
					if (c.getDataSetMember()!=null && c.getDataSetMember().equals(getLabelMemberModel().getObject())) {
						logger.debug("Remove " + c.getStrValue());
						getDatasetMemberModel().getObject().removeClassification(c);
						break;
					}
				}
				getDatasetMemberModel().getObject().getService(DOMObjectService.class).update("Remove Tag " + getLabelMemberModel().getObject().getDisplayName());
			}
			// Add Tag
			else {
				KbeeMemberClassification clasi;
				for (Classifier z: getDatasetMemberModel().getObject().getDataSet().getClassifiers()) {
					if (z.getDataSet()!=null && z.getDataSet().equals(getLabelMemberModel().getObject().getDataSet())) {
						clasi = new KbeeMemberClassification(z, getLabelMemberModel().getObject(), getDatasetMemberModel().getObject());
						getDatasetMemberModel().getObject().addClassification(clasi);
						getDatasetMemberModel().getObject().getService(DOMObjectService.class).update("Add Tag " + getLabelMemberModel().getObject().getDisplayName());		
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}

	@Override
	protected void onUpdate(AjaxRequestTarget target) {
	}	

	@Override
	public void onDetach() {
		super.onDetach();
		datasetmember_model.detach();
	}

}

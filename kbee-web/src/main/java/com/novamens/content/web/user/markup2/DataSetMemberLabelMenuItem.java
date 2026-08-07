package com.novamens.content.web.user.markup2;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LabelMember;
import com.novamens.content.service.DOMObjectService;

import com.novamens.kbee.content.model.KbeeMemberClassification;
import com.novamens.kbee.wicket.markup.html.console.panel.ObjectLabelMenuItem;

/**
 * 
 *
 */
public class DataSetMemberLabelMenuItem extends ObjectLabelMenuItem<DataSetMember> {
				
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataSetMemberLabelMenuItem.class.getName());
	

	private Boolean isenabled;

	public DataSetMemberLabelMenuItem(String id, IModel<LabelMember> model, IModel<DataSetMember> object_model) {
		super(id, model, object_model);
	}
	
	@Override
	protected boolean isEnabledLabelMember() {
		if (isenabled!=null)
			return isenabled.booleanValue();
		for (Classification c: getObjectModel().getObject().getClassification()) {
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
		
		
		this.isenabled = Boolean.valueOf(!isEnabledLabelMember());
		
		try {
			
			/**
			 *	Remove Tag 
			 */

			if (!isenabled) {
				for (Classification c: getObjectModel().getObject().getClassification()) {
					if (c.getDataSetMember()!=null && c.getDataSetMember().equals(getLabelMemberModel().getObject())) {
						logger.debug("Remove " + c.getStrValue());
						getObjectModel().getObject().removeClassification(c);
						break;
					}
				}
				getModelObject().getService(DOMObjectService.class).update("Remove Tag " + getLabelMemberModel().getObject().getDisplayName());
			}
			
			/**
			 *	Add Tag 
			 */
			else {
				KbeeMemberClassification clasi;
				for (Classifier z: getObjectModel().getObject().getDataSet().getClassifiers()) {
					if (z.getDataSet()!=null && z.getDataSet().equals(getLabelMemberModel().getObject().getDataSet())) {
						clasi = new KbeeMemberClassification(z, getLabelMemberModel().getObject(), getObjectModel().getObject());
						getObjectModel().getObject().addClassification(clasi);
						getObjectModel().getObject().getService(DOMObjectService.class).update("Add Tag " + getLabelMemberModel().getObject().getDisplayName());		
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

}

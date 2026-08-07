package com.novamens.content.web.user.markup2;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classification;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.LabelMember;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.wicket.markup.html.console.panel.ObjectLabelMenuItem;

public class ContentLabelMenuItem extends ObjectLabelMenuItem<Content> {

			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentLabelMenuItem.class.getName());

	private Boolean isenabled;

	public ContentLabelMenuItem (String id, IModel<LabelMember> model, IModel<Content> object_model) {
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
		isenabled = Boolean.valueOf (!isEnabledLabelMember());
		try {
			// Remove Tag
			if (!isenabled) {
				for (Classification c: getObjectModel().getObject().getClassification()) {
					if (c.getDataSetMember()!=null && c.getDataSetMember().equals(getLabelMemberModel().getObject())) {
						getObjectModel().getObject().removeClassification(c);
						break;
					}
				}
				getObjectModel().getObject().getService(ContentService.class).update("Remove Tag " + getLabelMemberModel().getObject().getDisplayName());
				
				
			}
			// Add Tag
			else {
				for (ClassifierTemplate z: getObjectModel().getObject().getContentTemplate().getClassifiers()) {
					if (z.getClassifier()!=null && z.getClassifier().getDataSet().equals(getLabelMemberModel().getObject().getDataSet())) {
						getObjectModel().getObject().addClassification(z.getClassifier(), getLabelMemberModel().getObject());
						getObjectModel().getObject().getService(ContentService.class).update("Add Tag " + getLabelMemberModel().getObject().getDisplayName());
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

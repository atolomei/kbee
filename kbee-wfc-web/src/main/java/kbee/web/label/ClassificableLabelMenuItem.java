package kbee.web.label;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LabelMember;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DOMObjectService;
import com.novamens.kbee.wicket.markup.html.console.panel.ObjectLabelMenuItem;

public class ClassificableLabelMenuItem<T extends Classificable> extends ObjectLabelMenuItem<T> {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ClassificableLabelMenuItem.class.getName());

	private Boolean isenabled;

	public  ClassificableLabelMenuItem (String id, IModel<LabelMember> model, IModel<T> object_model) {
		super(id, model, object_model);
	}
	
	/**
	public String getIconCssClass() {
		if (getLabelMemberModel()==null)
			return null;
		return getLabelMemberModel().getObject().getLabelColor().getKey() + " far fa-tag "; 	
	}**/
	
	
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
						logger.debug("Remove " + c.getStrValue());
						getObjectModel().getObject().removeClassification(c);
						break;
					}
				}
				if (getObjectModel().getObject() instanceof Content) {
					((Content) getObjectModel().getObject()).getService(ContentService.class).update("Remove Tag " + getLabelMemberModel().getObject().getDisplayName());
				}
				else if (getObjectModel().getObject() instanceof DataSetMember) { 
					((DataSetMember) getObjectModel().getObject()).getService(DOMObjectService.class).update("Remove Tag " + getLabelMemberModel().getObject().getDisplayName());
				}
			}
			// Add Tag
			else {
								
				for (Classifier c: getObjectModel().getObject().getClassifiers()) {
					if (c.getDataSet().equals(getLabelMemberModel().getObject().getDataSet())) {
					
						getObjectModel().getObject().addClassification(c, getLabelMemberModel().getObject());
						if (getObjectModel().getObject() instanceof Content) {
							((Content) getObjectModel().getObject()).getService(ContentService.class).update("Add Tag " + getLabelMemberModel().getObject().getDisplayName());
						}
						else if (getObjectModel().getObject() instanceof DataSetMember) { 
							((DataSetMember) getObjectModel().getObject()).getService(DOMObjectService.class).update("Add Tag " + getLabelMemberModel().getObject().getDisplayName());
						}
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

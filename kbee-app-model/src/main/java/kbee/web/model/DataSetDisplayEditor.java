package kbee.web.model;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.service.DOMObjectService;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeExtractionMacro;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class DataSetDisplayEditor<T extends DataSet> extends DomainObjectEditor<T> {
	private static final long serialVersionUID = 1L;

	static Logger logger =  Logger.getLogger(DataSetDisplayEditor.class.getName());

	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private String displayNameTemplate, sublineTemplate, consoleDisplayNameTemplate;
	
	
	
	public DataSetDisplayEditor(String id, IModel<T> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		setDisplayNameRule(model.getObject().getDisplayNameRule());
		setConsoleDisplayNameTemplate(model.getObject().getConsoleDisplayNameTemplate());
		
		setSublineRule(model.getObject().getSublineRule());
		//add(new InfoDialog("help-modal"));
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new BooleanField("displayNameEditable"));
		form.add(new TextAreaField<String>("displayNameTemplate", new PropertyModel<String>(this, "displayNameTemplate")));
		form.add(new TextAreaField<String>("alternativeDisplayNameTemplate", new PropertyModel<String>(this, "consoleDisplayNameTemplate")));
		form.add(new TextAreaField<String>("sublineTemplate", new PropertyModel<String>(this, "sublineTemplate")));
		
		add(form);
		
		add(new EditButtonsV5<T>(this) {
			@Override
			public boolean isEnabled() {
				if (isRoot())
					return true;
				if (getModel().getObject().isOnlyRootEdit())
					return false;
				return (role_admin && !isExpressVersion());
			}
		});
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				
				((KbeeDataSet)getModelObject()).setDisplayNameRule(getDisplayNameRule());
				((KbeeDataSet)getModelObject()).setConsoleDisplayNameTemplate(getConsoleDisplayNameTemplate());
				
				
				((KbeeDataSet)getModelObject()).setSublineRule(getSublineRule());
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				super.reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public String getDisplayNameTemplate() {
		return displayNameTemplate;
	}
	
	public void setDisplayNameTemplate(String template) {
		this.displayNameTemplate = template;
	}
	
	
	public String getConsoleDisplayNameTemplate() {
		return consoleDisplayNameTemplate;
	}
	
	public void setConsoleDisplayNameTemplate(String template) {
		this.consoleDisplayNameTemplate = template;
	}
	
	
	public void setDisplayNameRule(ExtractionRule rule)  {
		if (rule instanceof KbeeExtractionMacro) {
			setDisplayNameTemplate(((KbeeExtractionMacro)rule).getMacro());
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public ExtractionRule getDisplayNameRule()  {

		if (getDisplayNameTemplate()==null) 
			return null;
		
		KbeeExtractionMacro rule = new KbeeExtractionMacro();
		rule.setMarco(getDisplayNameTemplate());
		return rule;
	}
	
	public String getSublineTemplate() {
		return sublineTemplate;
	}
	
	public void setSublineTemplate(String template) {
		this.sublineTemplate = template;
	}
	
	public void setSublineRule(ExtractionRule rule)  {
		if (rule instanceof KbeeExtractionMacro) {
			setSublineTemplate(((KbeeExtractionMacro)rule).getMacro());
		}
	}
	
	public ExtractionRule getSublineRule()  {
		KbeeExtractionMacro rule = new KbeeExtractionMacro();
		rule.setMarco(getSublineTemplate());
		return rule;
	}
	
	protected void onCancel(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);							
	}
}
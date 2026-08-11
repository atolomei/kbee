package com.novamens.kbee.content.workflow;


import java.io.Serializable;
import java.util.List;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EDisposition;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EIdentifiableForm;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.FormLayout;



public class KbeeTaskForm implements EIdentifiableForm, Serializable  {
			
	private static final long serialVersionUID = 1L;
	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeTaskForm.class.getName());
	
	private String eformId;
	private EForm eform;
	private String name = null;
	private boolean enabled = true;
	private boolean readonly;
	private boolean signatureRequired; 
	private FormLayout layout;  
	

	public KbeeTaskForm() {
		
	}

	
	public KbeeTaskForm(EForm eform) {
		setForm(eform);
	}
	
	
	public FormLayout getFormLayout() {
		return this.layout!=null?this.layout:FormLayout.EDITOR;
	}
	
	public void setFormLayout( FormLayout la) {
		this.layout=la;
	}
	
	public Serializable getId() {
		return ((EIdentifiableForm)getForm()).getId();
	}
	
	public int getVersion() {
		return ((EIdentifiableForm)getForm()).getVersion();
	}

	public List<EFormComponent> getComponents() {
		return getForm().getComponents();
	}
	
	public List<EFormField<?>> getFields() {
		return getForm().getFields();
	}
	
	public boolean isEnabled() {
		return enabled && !readonly;
	}
	
	public void setEnabled(boolean value) {
		this.enabled = value;
	}
	
	public boolean isReadOnly() {
		return readonly;
	}
	
	public void setReadOnly(boolean value) {
		this.readonly = value;
	}
	
	@Override
	public boolean isVisible(EFormData data) {
		return getForm().isVisible(data);
	}
	
	public String getName() {
		return name==null && getForm()!=null ? getForm().getName() : null;
	}
	
	@Override
	public String getDisplayName() {
		return getForm().getDisplayName();
	}
	
	@Override
	public String getCssClass() {
		return getForm().getCssClass();
	}
	
	@Override
	public String getViewer() {
		return getForm().getViewer();
	}
	
	@Override
	public EFormAccessLevel getFormAccessLevel() {
		EFormAccessLevel e = getForm().getFormAccessLevel();
		return e!=null?e : EFormAccessLevel.GENERAL;
	}
	
	@Override
	public boolean isUseInline() {
		return getForm().isUseInline();
	}

	@Override
	public boolean isFileContainer() {
		return getForm().isFileContainer();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFormId() {
		return eformId;
	}

	public void setForm(EForm form) {
		if (form instanceof Identifiable) {
			eformId = String.valueOf(((Identifiable)form).getId());
		}
		if (form instanceof Serializable) {
			eform = form;
		}
	}
	
	@Override
	public EDisposition getDisposition() {
		return getForm().getDisposition();
	}
	
	@Override
	public List<String> getBehaviors() {
		return getForm().getBehaviors();
	}
	
	@Override
	public EFormField<?> getField(String name) {
		return getForm().getField(name);
	}
	
	public EForm getForm() {

		EForm eform;

		if (eformId!=null) { 
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			eform = (EForm)sf.getCurrentSession().load(KbeeEForm.class, Long.valueOf(this.eformId));
			
			if (eform==null)
				logger.error("eform is null for id -> " + Long.valueOf(this.eformId).toString());
			
			logger.debug("eform class -> " + eform.getClass().getName());
		}
		else {
			eform = this.eform;
		}
			
		return eform;
	}

	public boolean isSignatureRequired() {
		return signatureRequired;
	}

	public void setSignatureRequired(boolean signatureRequired) {
		this.signatureRequired = signatureRequired;
	}

	public boolean hasToolbar() {
		return getForm().hasToolbar();
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof EForm)) 
			return false;
		
		if (object instanceof KbeeTaskForm)
			return getForm().equals(((KbeeTaskForm)object).getForm());
		return getForm().equals((EForm)object);
	}
}

package com.novamens.content.web.admin.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.service.DomainLifeCycleService;
import com.novamens.kbee.system.KbeeSystemParameter;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.SystemParameter;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.Tuple;
import kbee.web.form.EditButtonsV5;

public class SystemPropertiesPanel extends ObjectEditor<Person> {

	private static final long serialVersionUID = 1L;
	
	final boolean is_root =ServiceLocator.getService(SecurityService.class).isRoot();
						
	
	final boolean role_service_admin = is_root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());

	private final boolean admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private final boolean role_api_developer   = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.API_DEVELOPER.getId());
	private final boolean role_factory_manager = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	private final boolean role_operations      = admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.OPERATIONS_ENGINEER.getId());
							

	
	public SystemPropertiesPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	private String key;
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}



	private String value;
	
	
	 @Override
	public void onInitialize() {
		super.onInitialize();
		
		AreaInfoPanel area = new AreaInfoPanel("properties");
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");
		GridInfoPanel gp = new GridInfoPanel("element", systemParameters(), new Model<String>("Properties"), true);
		gp.setLabelCss("col-xs-6 col-lg-3 col-md-4");
		gp.setValueCss("col-xs-6 col-lg-9 col-md-8");
		area.addPanel(gp);
		add(area);
		
		com.novamens.wicket.markup.html.form.Form<?> form = new com.novamens.wicket.markup.html.form.Form<Void>("form", Disposition.VERTICAL);
		TextField<String> ke = new TextField<String>("key", new PropertyModel<String>(this, "key"));
		form.add(ke);
		
		TextField<String> va = new TextField<String>("value", new PropertyModel<String>(this, "value"));
		form.add(va);

		add(form);
		
		form.setOutputMarkupId(true);
		
		form.setVisible(admin || role_service_admin ||  role_factory_manager);
		
		
		
		add(new EditButtonsV5<Person>(this, false) {
			
			private static final long serialVersionUID = 1L;
			@Override
			protected String getEditClass() {
				return "btn btn-default btn-sm";
			}

			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			
			@Override
			public boolean isVisible() {
				return admin || role_service_admin ||  role_factory_manager;
			}
		});
	}

	 @Override
	 public void update(AjaxRequestTarget target) {
		 
		 boolean changed = false;
		 
		try {
			if (getKey()!=null) {
				if (getValue()==null || getValue().trim().isEmpty()) {
					for (SystemParameter sp: getContentDao().getSystemParameters()) {
						if (sp.getKey().equals(getKey().trim().toLowerCase())) {
							ServiceLocator.getService(DomainLifeCycleService.class).deleteSystemParameter(sp);
							changed=true;
							break;
						}
					}
				}
				else {
					for (SystemParameter sp: getContentDao().getSystemParameters()) {
						if (sp.getKey().equals(getKey().trim().toLowerCase())) {
							sp.setValue(getValue());
							ServiceLocator.getService(DomainLifeCycleService.class).saveSystemParameter(sp);
							changed=true;
							break;
						}
					}
					
					if (!changed) {
						ServiceLocator.getService(DomainLifeCycleService.class).saveSystemParameter(new KbeeSystemParameter(getKey().toLowerCase().trim(), getValue()));
						changed=true;
					}
				}
				
				if (changed) {
					AreaInfoPanel area = new AreaInfoPanel("properties");
					area.setSections(AreaInfoPanel.ONE_SECTION);
					area.setCss("col-lg-12");
					GridInfoPanel gp = new GridInfoPanel("element", systemParameters(), new Model<String>("Properties"));
					gp.setLabelCss("col-xs-6 col-lg-3 col-md-4");		
					gp.setValueCss("col-xs-6 col-lg-9 col-md-8");
					area.addPanel(gp);
					addOrReplace(area);
					}
				
				target.add(SystemPropertiesPanel.this);
			}
		}
		
		catch (Exception e) {
			//LogManager.getLogger(UserEditor.class.getName()).error(e);
			//throw new RuntimeException(e);
		}
	}
	
	
	 private List<Tuple> systemParameters() {
		List<Tuple> data = new ArrayList<Tuple>();
		for (SystemParameter sp: getContentDao().getSystemParameters() ) {
			data.add(new Tuple( sp.getKey(), sp.getValue()!=null? sp.getValue():"[null]"));
		}
		return data;
	}

	
	


}

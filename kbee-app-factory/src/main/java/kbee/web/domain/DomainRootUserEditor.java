package kbee.web.domain;


import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.logging.DomainUpdateEvent;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

public class DomainRootUserEditor extends ObjectEditor<Domain> {
			
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(DomainRootUserEditor.class.getName());

	private com.novamens.service.SecurityService service = null;
	

	// TODO BORRAR ESTO
	
	
	public DomainRootUserEditor(String id, IModel<Domain> model) {
		super(id, model);
		
		final boolean IS_DOMAIN_KBEE = isDomainKbee();
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("organization"));
		form.add(new StaticField<String>("name"));
		
		
		// Type: Express | Enterprise
		
		if (!IS_DOMAIN_KBEE || model.getObject().getDomainType()==DomainType.SYSTEM)
			form.add(new StaticField<String>("type", new Model<String>(model.getObject().getDomainType().getLabel( getUser().getLocale()))));
		else {
			
			// Selector Dropdown [ Express | Enterprise ] 
			//
			form.add(new StaticField<String>("type", new Model<String>(model.getObject().getDomainType().getLabel( getUser().getLocale()))));
			
			
		}
		
		
		form.add(new TextField<String>("address") {
			@Override
			protected String getAutoComplete() {
				return "street-address";
			}
		});

		form.add(new TextField<String>("website") {
			@Override
			protected String getInputType() {
				return "url";
			}
			@Override
			protected String getAutoComplete() {
				return "street-address";
			}
		});
		
		form.add(new TextField<String>("description"));
		
			
		add(form);
		add(new EditButtonsV5<Domain>(this));
	}
	


	public void update(AjaxRequestTarget target) {
			
		try {
		if (!getUpdatedParts().isEmpty()) {
				logger.info(new DomainUpdateEvent(getModelObject(), getUpdatedParts()));
				ServiceLocator.getService(UserService.class).evict();
				reset();
			}
	} catch (Exception e) {
		logger.error(e);
		fire(new ErrorEvent(target, e));
	}

	}
	
	public List<String> getStatus() {
		List<String> status = new ArrayList<String>();
		status.add(getStringResource("service.enabled"));
		status.add(getStringResource("service.disabled"));
		return status;
	}
	
	
	@Override
	public void onDetach() {
		service=null;
		super.onDetach();

	}
	
	protected String getStringResource(String key) {
		return (new StringResourceModel(key, this, null)).getObject();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	@SuppressWarnings("unused")
	private com.novamens.service.SecurityService getSecurityService() {
		if (service!=null)
			return service;	
		service = ServiceLocator.getService(com.novamens.service.SecurityService.class);
		return service;
	}
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	
	private boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			return false;
		}
	}
	
	
	
	
	
	
	

	
	
	
}

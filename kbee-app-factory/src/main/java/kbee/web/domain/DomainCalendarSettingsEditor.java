package kbee.web.domain;



import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.model.IModel;

import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.calendar.CalendarService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;
import kbee.web.panel.AlertPanel;

public class DomainCalendarSettingsEditor extends DomainObjectEditor<Domain> {

	
	private static final long serialVersionUID = 1L;
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DomainCalendarSettingsEditor.class.getName());

	private Json json;
	public class DomainPropertyModel implements IModel<String> {
		private static final long serialVersionUID = 1L;
		private String key; 
		public DomainPropertyModel(String key) {
			this.key = key;
		}
		public String getObject() {
			return (String)getSettings().get(key);
		}
		public void setObject(String value) {
			getSettings().put(key, value.toString());
		}
		public void detach() {
		}
	}
	 
		
	/** 
	   It is important to use getModelObject() for the Domain instead of getDomain because
	   sometimes the Domain being edited is different from the user's domain.
	  
	   @param id
	   @param model
	 */
	public DomainCalendarSettingsEditor(String id, IModel<Domain> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		
	
		
		
		AlertPanel<Void> pa=new AlertPanel<Void>("note",AlertPanel.INFO,  null, 
				null, 
				getLabel("note"));
		pa.setIcon(AlertPanel.HELP_INFO);
		addOrReplace(pa);
		
		
		
		
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
						
		TextField<String> cal = new TextField<String>("calendarSettings", new DomainPropertyModel(DomainSettingsService.CALENDAR_NON_WORKABLE_DAYS))   {
					private static final long serialVersionUID = 1L;
			};
		
		form.add(cal);


		
		ChoiceField<String> sh = new ChoiceField<String>("starthour",	new DomainPropertyModel(DomainSettingsService.START_HOUR), new PropertyModel<List<String>>(this, "hours"))   {
			private static final long serialVersionUID = 1L;
		};
		form.add(sh);
		
						
		ChoiceField<String> eh = new ChoiceField<String>("endhour",	new DomainPropertyModel(DomainSettingsService.END_HOUR), new PropertyModel<List<String>>(this, "hours"))   {
			private static final long serialVersionUID = 1L;
		};
		form.add(eh);

		
		
		
		ChoiceField<String> cof = new ChoiceField<String>("cutoffTime",	new DomainPropertyModel(DomainSettingsService.CUTOFF_TIME), new PropertyModel<List<String>>(this, "hours"))   {
					private static final long serialVersionUID = 1L;
		};
		
		form.add(cof);
		add(form);
		
					
		add(new EditButtonsV5<Domain>(this) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isEnabled()  {
					if (isSupportUser() && !isRoot())
						return false;
					return isAdminSessionUser() || isFactoryAdminSessionUser() ||  isServiceAdminSessionUser();
			}
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}

			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
		});
	}
	

	

	public List<String> getHours() {
		List<String> list = new ArrayList<String>(); 
		for (int n=0; n<24; n++ ) {
				list.add(String.valueOf(n));
		}
		return list;
	}



	public void update(AjaxRequestTarget target) {

			if (!getUpdatedParts().isEmpty()) {
				getDomain().getService(DomainSettingsService.class).SetValues(getSettings(), getUpdatedParts());

				// TODO. Pasar e Evento
				 getDomain().getService(CalendarService.class).evict();
				 ServiceLocator.getService(UserService.class).evict();

				 reset();
			}
	}
	
	
	public Json getSettings() {
		if (this.json==null) {
			this.json = getModelObject().getService(DomainSettingsService.class).getValues();
			if (this.json==null) {
				this.json = new KbeeJson();
				this.json.put(DomainSettingsService.CALENDAR_NON_WORKABLE_DAYS, "");
				this.json.put(DomainSettingsService.CUTOFF_TIME, "17");
				
				this.json.put(DomainSettingsService.START_HOUR, "8");
				this.json.put(DomainSettingsService.END_HOUR, "17");
			}
		}
		
		if (this.json.get(DomainSettingsService.CALENDAR_NON_WORKABLE_DAYS) == null) {
			this.json.put(DomainSettingsService.CALENDAR_NON_WORKABLE_DAYS, CalendarService.DEFAULT_NON_WORKABLE );
		}
		
		if (this.json.get(DomainSettingsService.CUTOFF_TIME) == null) {
			this.json.put(DomainSettingsService.CUTOFF_TIME, CalendarService.DEFAULT_CUTOFF_TIME);
		}
		
		if (this.json.get(DomainSettingsService.START_HOUR) == null) {
			this.json.put(DomainSettingsService.START_HOUR, CalendarService.START_HOUR);
		}
		
		if (this.json.get(DomainSettingsService.END_HOUR) == null) {
			this.json.put(DomainSettingsService.END_HOUR, CalendarService.END_HOUR);
		}
		
		return this.json;
	}
	
	
	@Override
	public void onDetach() {
		this.json = null;
		super.onDetach();
	}
	
	protected String getStringResource(String key) {
		return (new StringResourceModel(key, this, null)).getObject();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getUser());
	}
	
	/**
	 *  User that edits the Form
	 */
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	protected boolean isAdminUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}

}

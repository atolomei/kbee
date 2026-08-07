package com.novamens.content.web.console.markup.searchselector;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.content.web.workflow.markup.AssignationPanel;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.console.AdvancedSearchSelectorEditor;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.form.EditButtonsV5;

import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.markup.html.form.TextAreaField;

@SuppressWarnings("serial")
public class AuditAdvancedSearchSelectorPanel extends AdvancedSearchSelectorEditor<Void> {
	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(AuditAdvancedSearchSelectorPanel.class.getName());
	

	private IModel<User> usermodel;
	private String type;
	private OffsetDateTime from, to;
	private List<Group> enabledGroups;
	
	public AuditAdvancedSearchSelectorPanel(String id) {
		super(id);
		setFrom(OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS));
		setTo(OffsetDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS));
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void onAfterRender() {
		super.onAfterRender();
		
		//if (get("form")!=null) {
		//	((AutoCompleteFieldV5<User>)get("form:userName")).clearCache(getResponse());
		//}
		
	}
	public void onDetach() {
		super.onDetach();
		if (usermodel!=null) 
			usermodel.detach();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(true);
		
		setType("All");
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ChoiceField<String>("type", new PropertyModel<String>(this, "type"), () -> getTypes()));
		
		form.add(new OffsetDateTimeField("from", ZoneId.of( getDomain().getTimeZone()), new PropertyModel<OffsetDateTime>(this, "from")));
		form.add(new OffsetDateTimeField("to", ZoneId.of( getDomain().getTimeZone()), new  PropertyModel<OffsetDateTime>(this, "to")));
		
		form.add(new AutoCompleteFieldV5<User>("userName", new PropertyModel<User>(this, "userName"), true) {
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters= new HashMap<String, Object>();
				if (getEnabledGroups()!=null && !getEnabledGroups().isEmpty()) {
					parameters.put("groups", getEnabledGroups());
				}
				return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern, parameters);
			}
			@Override 
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				//target.focusComponent(((TextAreaField<?>)AssignationPanel.this.get("aform:note")).getInput());
			}
			@Override 
			public String getHistoryKey() {
				return "collaborator"; 
			}
			@Override 
			protected boolean isValid(IModel<User> model) {
				if (getEnabledGroups()!=null && !getEnabledGroups().isEmpty()) {
					for (Group group : getEnabledGroups()) {
						if (group.isMember(model.getObject())) {
							return true;
						}
					}
					return false;
				}
				else {
					return true;
				}
			}
		});

		form.add(new EditButtonsV5<Void>(this, true) {
			
			@Override
			public boolean getDisableAfterSubmit() {
				return false;
			}
			
			@Override
			protected IModel<String> getSubmitLabel() {
				return new StringResourceModel("apply", AuditAdvancedSearchSelectorPanel.this, null);
			}
			
			@Override
			protected String getSubmitClass() {
				return "btn btn-default btn-sm";
			}
		});
		
		add(form);
	}
	
	@Override
	public  void update(AjaxRequestTarget target) {
	
		logger.debug(getFilters().toString());
		
		setEditionEnabled(true);
		fire(new FilterSelectorEvent(target, getFilters()));
		target.add(this);
	}
	
	public User getUserName() {
		return usermodel!=null ? usermodel.getObject() : null;
	}

	public void setUserName(User user) {
		
		usermodel = new ObjectModel<User>(user);
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public OffsetDateTime getFrom() {
		return from;
	}

	public void setFrom(OffsetDateTime date) { 
		this.from = date;
	}
	
	public OffsetDateTime getTo() {
		return to;
	}

	public void setTo(OffsetDateTime date) { 
		this.to = date;
	}

	
	
	public List<Group> getEnabledGroups() {
		return enabledGroups;
	}

	public void setEnabledGroups(List<Group> enabledGroups) {
		this.enabledGroups = enabledGroups;
	}

	public List<String> getTypes() {
		List<String> values = new ArrayList<String>();
		values.add("Content");
		values.add("Security");
		values.add("Workflow");
		values.add("All");
		return values;
	}
	
	private Map<String, Object> getFilters() {
		
		Map<String, Object> filters = new HashMap<String, Object>();
		
		filters.put("todate", getTo()!=null?getTo():"null");
		filters.put("fromdate", getFrom()!=null?getFrom():"null");
		filters.put("username", getUserName()!=null?getUserName().getLasName():"null");
		filters.put("type", getType()!=null?getType():"null");
		
		return filters;
	}

	@Override
	protected void clearAll() {
		// TODO Auto-generated method stub
		
	} 
}

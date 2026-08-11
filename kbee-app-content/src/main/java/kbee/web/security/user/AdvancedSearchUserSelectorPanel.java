package kbee.web.security.user;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.indexer.query.PhoneticFilter;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.indexer.query.WordsFilter;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorEvent;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.console.AdvancedSearchSelectorEditor;
import kbee.web.form.EditButtonsV5;


@SuppressWarnings("serial")
public class AdvancedSearchUserSelectorPanel extends AdvancedSearchSelectorEditor<Void> {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AdvancedSearchUserSelectorPanel.class.getName());
	
	private String lastName;
	private String firstName;
	private String userName;
	private String userId;
	private String email;
	
	public AdvancedSearchUserSelectorPanel(String id) {
		super(id);
	}

	
	@Override
	protected void clearAll() {
		setLastName(null);
		setFirstName(null);
		setUserName(null);
		setUserId(null);
		setEMail(null);
		((Form<?>) AdvancedSearchUserSelectorPanel.this.get("form")).clearInput();
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(true);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		TextField<String> t1 = new TextField<String>("firstName", 
			new PropertyModel<String>(this, "firstName"));
		t1.setPlaceholderLabel(true);
		t1.listenEnter(true);
		form.add(t1);
		
		TextField<String> t2 = new TextField<String>("lastName", 
			new PropertyModel<String>(this, "lastName"));	
		t2.setPlaceholderLabel(true); 
		t2.listenEnter(true);
		form.add(t2);
		
		TextField<String> t3 = new TextField<String>("userName", 
			new PropertyModel<String>(this, "userName"));	
		t3.setPlaceholderLabel(true); 
		t3.listenEnter(true);
		form.add(t3);
		
		//TextField<String> t4 = new TextField<String>("userId", new PropertyModel<String>(this, "userId"));		t4.setPlaceholderLabel(true); form.add(t4);
		
		TextField<String> t5 = new TextField<String>("email", new PropertyModel<String>(this, "email"));
		t5.setPlaceholderLabel(true); 
		t5.listenEnter(true);
		form.add(t5);
		
		WorkingIndicatorAjaxLinkV5<Void> clearall = new WorkingIndicatorAjaxLinkV5<Void>("clear-all", new StringResourceModel("clear-all", this, null).getString()) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				clearAll();
				target.add(AdvancedSearchUserSelectorPanel.this);
				fire(new FilterSelectorClearAllEvent(target));
			}
			@Override
			public String getWorkingLabel() {
				return new StringResourceModel("working", this, null).getString();
			}
		};
		
		
		form.add(clearall);
				
		form.add(new EditButtonsV5<Void>(this, true) {
			@Override
			public boolean getDisableAfterSubmit() {
				return false;
			}
			@Override
			protected IModel<String> getSubmitLabel() {
				return new StringResourceModel("apply", AdvancedSearchUserSelectorPanel.this, null);
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-default btn-sm";
			}
		});
		
		IFormSubmittingComponent btn =
			    (IFormSubmittingComponent) form.get("buttons").get("submit");
		form.setDefaultButton(btn);
		
		add(form);
	}
	
	/**
	 * 
	 * 
	 */
	@Override
	public  void update(AjaxRequestTarget target) {

		logger.debug(getFilters().toString());
		setEditionEnabled(true);
		
		fire(new FilterSelectorEvent(target, getFilters()));
		target.add(this);
	}
	
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String id) {
		this.userId = id;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String name) {
		this.userName = name;
	}
	
	public String getEMail() {
		return email;
	}

	public void setEMail(String email) {
		this.email = email;
	}

	/**
	 * 
	 * 
	 */
	private Map<String, Object> getFilters() {
		
		Map<String, Object> filters = new HashMap<String, Object>();
		
		if (getLastName()!=null && !"".equals(getLastName())) 
			filters.put("lastname", new PhoneticFilter("lastname", getLastName()));
		
		if (getFirstName()!=null && !"".equals(getFirstName())) 
			filters.put("firstname", new WordsFilter("firstname", getFirstName()));
		
		if (getUserId()!=null && !"".equals(getUserId()))
			filters.put("userid", new ValueFilter("userid", getUserId()));
		
		if (getUserName()!=null && !"".equals(getUserName())) {
			String stm = getUserName();
			int i = stm.indexOf("@");
			if (i>0) stm = stm.substring(0,i);
			filters.put("username", new ValueFilter("username", stm, getUserName()));
		}	
		
		if (getEMail()!=null && !"".equals(getEMail())) 
			filters.put("email", new ValueFilter("email", getEMail()));

		return filters;
	} 

}

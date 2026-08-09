package kbee.web.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.validator.routines.EmailValidator;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EAutoCompleteField;
import com.novamens.content.form.EDisposition;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EFormMemberData;
import com.novamens.content.form.EValidatable;
import com.novamens.content.form.EValidation;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.kbee.content.form.EFormAbstractField;
import com.novamens.kbee.content.form.KbeeEFilePropertyModel;
import com.novamens.kbee.content.form.KbeeEFormRow;
import com.novamens.kbee.content.form.KbeeEFormSection;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.content.form.KbeeEStringPropertyModel;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.kbee.content.form.KbeeMemberForm;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@SuppressWarnings("serial")
public class KbeeUserForm  implements EForm, Serializable {
	private static final long serialVersionUID = 1L;
	private List<EFormComponent> components;
	private String name;
	private String cssClass;
	
	public class KbeeEMailValidation implements EValidation, Serializable {
		public KbeeEMailValidation() {
		}
		public boolean isSubmit() {
			return true;
		}
		public void validate(EValidatable validatable) {
			Object data =  validatable.getData().getData(validatable.getField());
			if (data!=null) {
				DataSetMember user = null;
				List<Person> mails = getContentDao().findPersonByEmail(data.toString());
				if (validatable.getData() instanceof EFormMemberData) {
					user = ((EFormMemberData)validatable.getData()).getMember(); 
				}
				if (!EmailValidator.getInstance().isValid(data.toString())) {
					validatable.error("error.mail_not_valid");
				}
				else
				if (mails!=null &&	!mails.isEmpty()) {
					if (user!=null && user instanceof PersonMember)  {
						for (Person person : mails) {
							if (!person.getId().equals(((PersonMember)user).getPerson().getId()) &&
									person.getDomain().equals(((PersonMember)user).getPerson().getDomain())) {
								validatable.error("error.mail_not_unique");
								break;
							}
						}
					}
					else {
						validatable.error("error.mail_not_unique");
					}	
				}
			}
		}
	}
	
	public KbeeUserForm(PersonMember member) {
		this.components = getComponents(member);
		this.name = member.getDataSet().getAlias();
	}
	
	public List<EFormComponent> getComponents() {
		return components;
	}
	
	public void setComponents(List<EFormComponent> components) {
		this.components = components;
	}
	
	public List<EFormField<?>> getFields() {
		return getFields(getComponents());
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String getDisplayName() {
		return name;
	}
	
	@Override
	public EDisposition getDisposition() {
		return EDisposition.VERTICAL;
	}
	
	@Override
	public EFormAccessLevel getFormAccessLevel() {
		return EFormAccessLevel.GENERAL;
	}
	
	@Override
	public boolean isUseInline() {
		return false;
	}

	@Override
	public boolean isFileContainer() {
		return false;
	}
	
	@Override
	public boolean isVisible(EFormData data) {
		return true;
	}
	
	@Override
	public String getViewer() {
		return null;
	}
	
	@Override
	public List<String> getBehaviors() {
		return new ArrayList<String>();
	}
	
	@Override
	public EFormField<?> getField(String name) {
		for (EFormField<?> field : getFields(getComponents())) {
			if (name.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}
	
	@Override
	public boolean hasToolbar() {
		return false;
	}
	
	protected List<EFormComponent> getComponents(PersonMember member) {
		
		List<EFormComponent> components;
		
		components = new ArrayList<EFormComponent>();
		KbeeEFormRow rowname = new KbeeEFormRow();
		rowname.setName("rowname");
		EFormAbstractField<?> field = new KbeeEStringField();
		field.setName("firstName");
		field.setLabel(getLabel("firstName"));
		KbeeEStringPropertyModel model = new KbeeEStringPropertyModel();
		model.setProperty("firstName");
		field.setCssClass("col-xs-6 col-lg-6 col-md-6 form-group");
		field.setModel(model);
		components.add(field);
		
		field = new KbeeEStringField();
		field.setName("lastName");
		field.setRequired(true);
		field.setLabel(getLabel("lastName"));
		model = new KbeeEStringPropertyModel();
		model.setProperty("lastName");
		field.setCssClass("col-xs-6 col-lg-6 col-md-6 form-group");
		field.setModel(model);
		components.add(field);
		
		rowname.setComponents(components);
		
		components = new ArrayList<EFormComponent>();
		KbeeEFormRow rowmail = new KbeeEFormRow();
		rowmail.setName("rowmail");
		field = new KbeeEStringField();
		field.setName("email");
		field.setRequired(true);
		field.addValidation(new KbeeEMailValidation());
		field.setLabel(getLabel("email"));
		model = new KbeeEStringPropertyModel();
		model.setProperty("email");
		field.setCssClass("col-xs-6 col-lg-6 col-md-6 form-group");
		field.setModel(model);
		components.add(field);
		
		field = new KbeeEStringField();
		field.setName("phone");
		field.setLabel(getLabel("phone"));
		model = new KbeeEStringPropertyModel();
		model.setProperty("phone");
		field.setCssClass("col-xs-6 col-lg-6 col-md-6 form-group");
		field.setModel(model);
		components.add(field);
		rowmail.setComponents(components);
		
		components = new ArrayList<EFormComponent>();
		KbeeEFormRow rowphoto = new KbeeEFormRow();
		rowphoto.setName("rowphoto");
		field = new KbeeEResource();
		field.setName("photo");
		field.setLabel(getLabel("photo"));
		field.setCssClass("col-xs-12 col-lg-12 col-md-12 form-group");
		field.setModel(new KbeeEFilePropertyModel("photo"));
		components.add(field);
		rowphoto.setComponents(components);
		
		
		components = new ArrayList<EFormComponent>();
		KbeeEFormRow rowaddress = new KbeeEFormRow();
		rowaddress.setName("rowaddress");
		field = new KbeeETextField();
		field.setName("address");
		field.setLabel(getLabel("address"));
		model = new KbeeEStringPropertyModel();
		model.setProperty("address");
		field.setCssClass("col-xs-12 col-lg-12 col-md-12 form-group");
		field.setModel(model);
		components.add(field);
		rowaddress.setComponents(components);
		
		
		components = new ArrayList<EFormComponent>();
		KbeeEFormRow rowposition = new KbeeEFormRow();
		rowposition.setName("rowposition");
		field = new KbeeEStringField();
		field.setName("position");
		field.setLabel(getLabel("position"));
		model = new KbeeEStringPropertyModel();
		model.setProperty("workPosition");
		field.setCssClass("col-xs-12 col-lg-12 col-md-12 form-group");
		field.setModel(model);
		components.add(field);
		rowposition.setComponents(components);
		
		components = new ArrayList<EFormComponent>();
		components.add(rowname);
		components.add(rowmail);
		components.add(rowaddress);
		components.add(rowposition);
		components.add(rowphoto);
		
		KbeeMemberForm memberform = new KbeeMemberForm(member) {
			protected boolean editableDisplayName(DataSetMember member) {
				return false;
			}
		};
		
		if (!memberform.getComponents().isEmpty()) {
			KbeeEFormSection section = new KbeeEFormSection(); 
			section.setName(member.getDataSet().getName());
			section.setLabel(getLabelString("dataset.section.label", member.getDataSet().getDisplayName()));
			
			List<EFormComponent> membercomponents = memberform.getComponents();
			for (EFormComponent membercomponent : membercomponents) {
				if (membercomponent instanceof EAutoCompleteField<?>) {
					
				}
			}
			
			section.setComponents(membercomponents);
			
			components.add(section);
		}
	
		return components;
	}
	
	private List<EFormField<?>> getFields(List<EFormComponent> components) {
		List<EFormField<?>> fields = new ArrayList<EFormField<?>>();
		for (EFormComponent component : components) {
			if (component instanceof EFormField) {
				fields.add((EFormField<?>)component);
			}
			if (component instanceof EFormContainer) {
				fields.addAll(getFields(((EFormContainer)component).getComponents()));
			}
		}
		return fields;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private String getLabel(String key) {
		Locale locale = getSessionUser()!=null ? getSessionUser().getLocale() : Locale.getDefault(); 
		ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), locale);
		return  resources.getString(key);
	}
	
	private String getLabelString(String key, String... parameter) {
		Locale locale = getSessionUser().getLocale();
		ResourceBundle resources = ResourceBundle.getBundle(getClass().getName(), locale);
		String label = resources.getString(key);
		for (int p=0; p<parameter.length; p++) {
			label = label.replace("{"+String.valueOf(p)+"}", parameter[p]);
		}
		return label;
	}
}
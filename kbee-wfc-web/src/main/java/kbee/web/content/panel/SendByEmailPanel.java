package kbee.web.content.panel;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EListField;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.form.EFormAbstractField;
import com.novamens.kbee.content.form.KbeeEMemForm;
import com.novamens.kbee.content.form.KbeeEMembersListField;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.content.form.KbeeEStringModel;
import com.novamens.kbee.content.form.KbeeEStringPropertyModel;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.kbee.content.form.KbeeExternalFormValuesModel;
import com.novamens.wicket.markup.html.form.Form;

import kbee.web.eform.EFormEditor;
import kbee.web.eform.ObjectFormEditor;
import kbee.web.panel.AlertPanel;

import com.novamens.wicket.markup.html.form.Form.Disposition;

public class SendByEmailPanel<T extends Content> extends ObjectFormEditor<T> {
	private static final long serialVersionUID = 1L;
	
	private Form<?> form;
	
	public SendByEmailPanel(String id) {
		this(id, null);
	}
	
	public SendByEmailPanel(String id, IModel<T> model) {
		super(id, model, false, false);
		setOutputMarkupId(true);
	}
	
	@SuppressWarnings("unchecked")
	public List<PersonMember> getReceivers() {
		List<PersonMember> receivers = new ArrayList<PersonMember>();  
		for (DataSetMember member : getDataModel().getObject().getValues((EListField<DataSetMember>)getData().getForm().getField("to"))) {
			receivers.add((PersonMember)member);
		};
		return receivers;
	}
	
	public String getText() {
		return  (String)getData().getData(getData().getForm().getField("text"));
	}
	
	public List<String> getToMails() {
		List<String> mails = new ArrayList<String>(); 
		String value =  (String)getData().getData(getData().getForm().getField("tomails"));
		if (value!=null) {
			StringTokenizer tokenizer = new StringTokenizer(value, ",");
			while (tokenizer.hasMoreTokens()) {
				mails.add(tokenizer.nextToken());
			}
		}
		return mails;
	}
	
	
	
	
	protected Form<?> getWicketForm() {
		return form;
	}

	public void onBeforeRender() {
		super.onBeforeRender();
			
		setEditionEnabled(true);
		
		if (form!=null)
			return;

		AlertPanel<Void> pa=new AlertPanel<Void>("alert-text",AlertPanel.INFO,  null, 
				getLabel("email"), 
				getLabel("alert-text"));
		pa.setIcon("fa-duotone fa-envelope");
		addOrReplace(pa);
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new EFormEditor("eform", getDataModel()));
		
		addOrReplace(form);
			
		addOrReplace(new FeedbackPanel());
	}	
	
	
	/**
	 * 
	 */
	protected EForm getForm(T object) {
		KbeeEMemForm eform = new KbeeEMemForm();
		List<EFormComponent> components = new ArrayList<EFormComponent>();
		
		EFormAbstractField<?> field = new KbeeEStringField();
		field.setName("title");
		field.setLabel(getLabelString("title"));
		field.setReadOnly(true);
		field.setModel(new KbeeEStringPropertyModel("Title"));
		components.add(field);
		
		field = new KbeeEMembersListField();
		field.setName("to");
		field.setLabel(getLabelString("to"));
		((KbeeEMembersListField)field).setInfoTemplate("${email}");
		KbeeExternalFormValuesModel model = new KbeeExternalFormValuesModel();
		Classifier classifier;
		if (DomainType.EXPRESS.equals(((Content)object).getDomain().getDomainType())) { 
			classifier = getPersonClassifier(((Content)object).getDomain());
			if (classifier==null) classifier = getUserClassifier(((Content)object).getDomain());
		}	
		else {
			classifier = getUserClassifier(((Content)object).getDomain());
		}
		model.setClassifier(classifier);
		field.setModel(model);
		components.add(field);
		
		field = new KbeeEStringField();
		field.setName("tomails");
		field.setLabel(getLabelString("tomails"));
		field.setModel(new KbeeEStringModel());
		field.setHelpText(getLabelString("tomails.help"));
		components.add(field);
		
		field = new KbeeETextField();
		field.setName("text");
		field.setLabel(getLabelString("text"));
		field.setModel(new KbeeEStringModel());
		components.add(field);
		
		eform.setComponents(components);

		return eform;
	}
	
	
	
	protected Classifier getPersonClassifier(Domain domain) {
	
		
		for (Classifier classifier : getContentDao().getClassifiers(domain)) {
			if (classifier !=null && classifier.isDistribution())
				return classifier;
		}
		return null;
	}
	
	protected Classifier getUserClassifier(Domain domain) {
		for (Classifier classifier : getContentDao().getClassifiers(domain)) {
			if ("user".equals(classifier.getAlias())) {
				return classifier;
			}
		}
		return null;
	}
}

package kbee.web.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EValidatable;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.form.KbeeEMemContentData;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

@SuppressWarnings("serial")
public abstract class ObjectFormEditor<T extends Classificable> extends ObjectEditor<T> {
	private static final long serialVersionUID = 1L;
	
	private IModel<EFormData> datamodel;

	/** ---------------------
	 * 
	 *
	 */
	public class FeedbackPanel extends Fragment {
		public FeedbackPanel() {
			super("feedback", "feedback-fragment", ObjectFormEditor.this);
			add(new ListView<String>("message", ()->getMessages()) {
				public void populateItem(ListItem<String> item) {
					item.add(new Label("text", item.getModelObject()));	
				}
			});
		}
		public List<String> getMessages() {
			List<String> messages = new ArrayList<String>();
			for (FeedbackMessage message : ObjectFormEditor.this.getFeedbackMessages()) {
				if (message.getMessage() instanceof EFormMessage) {
					EFormMessage eformmessage = (EFormMessage)message.getMessage(); 
					messages.add((String)eformmessage.getMessage());
				}
			}
			return messages;
		}
		public boolean isVisble() {
			return !getMessages().isEmpty();
		}
	}	
	

	/** ---------------------
	 * 
	 *
	 */
	public class KbeeEValidatable implements EValidatable {
		EForm form;
		EFormField<?> field;
		public KbeeEValidatable(EForm form, EFormField<?> field) {
			this.form = form;
			this.field = field;
		}
		public Object getValue() {
			return getData().getData(getField());
		}
		public EFormField<?> getField() {
			return field;
		}
		public EFormData getData() {
			return datamodel.getObject();
		}
		public void error(String key) {
			error(key, getField().getLabel());
		}
		public void error(String key, String... parameter) {
			String message = getLabelString(key, parameter);
			setError(getField(), message);
			ObjectFormEditor.this.error(new FieldMessage(ObjectFormEditor.this, getEForm(), getField(), message, FeedbackMessage.ERROR));
		}
	}
	
	/** ---------------------
	 * 
	 *
	 */

	public ObjectFormEditor(String id, IModel<T> model, boolean isNew, boolean isReadOnly) {
		super(id, model);
		
		setIsNew(isNew);
		setReadOnly(isReadOnly);
		setEditionEnabled(isNew);
	}
	
	public void update(T object) {
		for (EFieldPanel<?> fieldpanel : getFieldPanels()) {
			fieldpanel.update(object);
		}
	}
	
	public void setModel(IModel<T> model) {
		if (model!=null) {
			setDataModel(getFormData(model.getObject()));
		}
		super.setModel(model);
	}
	
	public EFormData getData() {
		return getDataModel().getObject();
	}
	
	public IModel<EFormData> getDataModel() {
		return datamodel;
	}
	
	public void setDataModel(IModel<EFormData> model) {
		this.datamodel = model;
	}

	public boolean hasErrors()	{
		return getFeedbackMessages().hasMessage(FeedbackMessage.ERROR);
	}
	
	public void setError(EFormField<?> field, Serializable message) {
		onInitialize();
		getFormPanel().setError(field, message);
	}
	
	
	protected abstract Form<?> getWicketForm();
	
		
	protected EFormEditor getFormPanel() {
		return (EFormEditor)getWicketForm().get("eform");
	}
	
	
	protected EForm getEForm() {
		return getFormPanel().getForm();
	}
	
	protected List<EFieldPanel<?>> getFieldPanels() {
		List<EFieldPanel<?>> panels = new ArrayList<EFieldPanel<?>>();
		for (Panel panel : getFormPanel().getPanelFactory().getPanels()) {
			if (panel instanceof EFieldPanel) {
				panels.add((EFieldPanel<?>)panel);
			}
		}
		return panels;
	}
	
	protected abstract EForm getForm(T object);
	
	protected IModel<EFormData> getFormData(T object) {
		EForm form = getForm(object);
		EFormData data = null;
		if (object instanceof Content) {
			data = new KbeeEMemContentData(form, (Content)object);
		}
		if (object instanceof DataSetMember) {
			data = new KbeeEMemMemberData(form, (DataSetMember)object);
		}
		for (EFormField<?> field : form.getFields()) {
			field.get(object, data);
		}	
		IModel<EFormData> model = new EFormDataModel(data);
		return model;
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	protected boolean isSupportSessionUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
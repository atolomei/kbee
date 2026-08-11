package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EForm;
import com.novamens.content.model.ContentTemplate;
import com.novamens.kbee.content.form.KbeeEForm;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class EFormsEditor extends RelationEditor<ContentTemplate, EForm> {	
	private static final long serialVersionUID = 1L;

	public EFormsEditor(String id) {
		super(id);
		
		
	}
	
	@Override
	public String getProperty() {
		return "forms"; 
	}
	
	
	@Override
	public String getTarget() {
		return 	null;
	}

	
	@Override
	protected void onValueClick(IModel<EForm> model) {
		setResponsePage(new RedirectPage( getServerUrl()+"/eform/"+getModelObject().getId()+"/"+((KbeeEForm)model.getObject()).getId()));
	}
	
	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		return properties;
	}
	
	protected WebMarkupContainer getCreationPanel() {
		return new EFormFactoryPanel("creation-panel", getModel()) {
			@Override
			public boolean isVisible() {
				return getEditor()!=null && getEditor().isEditionEnabled() && creationEnabled() && !isReadOnly();
			}
			@Override
			public void onCreate(AjaxRequestTarget target, EForm newform) {
				EFormsEditor.this.add(new NewValueModel(newform));
				target.add(EFormsEditor.this);
			}
		};
	}

	@Override
	protected Property<?> getKey() {
		return null;
	}
	
	protected List<EForm> getForms() {
		List<EForm> forms =  new ArrayList<EForm>();
		getModelObject().getForms();
		return forms;
	}
	
	protected String getTitle(EForm value) {
		return value.getDisplayName() + " ("+value.getName()+")";
	}
	
	@Override
	protected String getPart() {
		return "eforms";
	}
	
	@Override
	protected EForm getNewValue() {
		return null;
	}
}
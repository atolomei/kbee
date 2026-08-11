package kbee.web.model;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.ModelElement;
import com.novamens.content.service.DOMObjectService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class ElementGridEditor<T extends ModelElement> extends DomainObjectEditor<T> {
					
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ElementGridEditor.class.getName());
	
	
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private IModel<String> info;
	
	
	public ElementGridEditor(IModel<T> model, final boolean editon) {
		this("editor", model, editon);
	}
	
	public ElementGridEditor(String id, IModel<T> model, final boolean editon) {
		super(id, model);
		setOutputMarkupId(true);
		setEditionEnabled(editon);
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new VisibilityPanel<T>(ElementGridEditor.this));
		
		form.add(new BooleanField("isDefaultGridColumn"));
		
		
		
		
		
		
		// column can be ordered
		form.add(new BooleanField("ordered"));
		
		add(form);
		
		
		add(new EditButtonsV5<T>(this)  {
			@Override
			public boolean isEnabled() {
				return role_admin;
			}
		});
		
		WebMarkupContainer wm=new WebMarkupContainer("info-alert") {
			public boolean isVisible() {
				return (getInfo()!=null && getInfo().getObject()!=null);
			}
		};
		form.add(wm);
	}
	
	
	public void onBeforeRender() {
		super.onBeforeRender();
		((WebMarkupContainer)get("form:info-alert")).addOrReplace((new Label("info", getInfo())).setEscapeModelStrings(false));
	}
	
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	public IModel<String> getInfo() {
		return info;
	}
	
	public void setInfo(IModel<String> model) {
		info=model;
	}
	

}

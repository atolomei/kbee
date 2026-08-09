package kbee.web.multidimensional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.multidimensional.FacetService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Facet;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

import com.novamens.wicket.markup.html.form.StaticField;

@SuppressWarnings("serial")
public class FacetEditor extends ObjectEditor<Facet> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(FacetEditor.class.getName());
	

	
	public FacetEditor(PageParameters parameters) {
		super("editor", null);
		throw new KbeeRuntimeException("not implemented");
	}

	
	public FacetEditor(IModel<Facet> model) {
		this("editor", model);
	}

	
	public FacetEditor(String id, IModel<Facet> model) {
		super(id, model);
		setOutputMarkupId(true);
		setEditionEnabled(false);
	}

	
	public void onDetach() {
		super.onDetach();
		
	if (getModel()!=null)
		getModel().detach();
	
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new StaticField<String>("id", new Model<String>( String.valueOf(getModel().getObject().getName()))));
		form.add(new TextField<String>("displayName", true));
		form.add(new BooleanField("suggester"));
		
		add(form);
		
		add(new EditButtonsV5<Facet>(this) {
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
		});	
	}

	public void onClose(AjaxRequestTarget target) {
		
	}
	
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				getDomain().getService(FacetService.class).update(getModelObject(), getUpdatedParts());
				super.reset();
				target.add(FacetEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));
		}
	}

	protected void onCancel(AjaxRequestTarget target) {
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}

	protected void onUpdate(AjaxRequestTarget target) {
		
	}
}

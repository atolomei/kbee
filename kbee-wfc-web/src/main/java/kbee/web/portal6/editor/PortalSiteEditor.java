package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;


/**
 * Imagen 
 * Search form
 * Advanced Search form 
 * 
 * --------------------------
 * Portal Contents
 * 
 * Home
 * Site -> Site Editor | Pages (Home, Seccion, [_Detalle-Template_] ) | Contents | Reports | Security 
 * Page -> Page Editor | Areas 
 * Area -> Area Editor | Blocks
 * Block -> Block Editor | items
 *  
 * 
 */
public class PortalSiteEditor extends DomainObjectEditor<Site> {
																									
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalSiteEditor.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private List<ObjectState> states = null;
	
	public PortalSiteEditor(String id, IModel<Site> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	public void onDetach() {
		super.onDetach();
		states=null;
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		
		//Label title = new Label("title", getModel().getObject().getTitle() + " <span class=\"suffix\">( " +getModel().getObject().getClassKey()+" ) </span>");
		//title.setEscapeModelStrings(false);
		//add(title);

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
		
		form.add(new TextField<String>("title", true));
		form.add(new TextField<String>("url", true));
		form.add(new TextField<String>("key", true));
		form.add(new TextAreaField<String>("description", 4, 40, false));
		
		if (getModel()!=null && getModel().getObject()!=null)
			form.add((new StaticField<String>("id", new Model<String>(getModel().getObject().getId().toString()))).setVisible(isAdminSessionUser()));
		else
			form.add(new InvisiblePanel("id"));
			
		
		add(new EditButtonsV5<Site>(this) {
			
			private static final long serialVersionUID = 1L;
			
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
			
			@Override
			public boolean isVisible() {
				return !isReadOnly();
			}
			
			@Override
			public boolean isEnabled()  {
				return true;
			}
		});		
		
	}
	

	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
		target.add(PortalSiteEditor.this.getParent());
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add(PortalSiteEditor.this.getParent());
	}

	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				getModel().getObject().getService(SiteService.class).update(getUpdatedParts());
				super.reset();
				target.add(PortalSiteEditor.this);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));

		}
	}

	public List<ObjectState> getStates() {
		
		if (states!=null)
			return states;
		
		states = new ArrayList<ObjectState>();
		states.add(ObjectState.ENABLED);
		states.add(ObjectState.ARCHIVED);
		
		return states;
	}

}

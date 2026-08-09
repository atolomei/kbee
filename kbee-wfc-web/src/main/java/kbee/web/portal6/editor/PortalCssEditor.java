package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.dom.ObjectID;
import com.novamens.kbee.portal.model.KbeePortalObject;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalModel;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

public class PortalCssEditor<T extends PortalModel> extends DomainObjectEditor<T> {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalCssEditor.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	
	
	public PortalCssEditor(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	public void onDetach() {
		super.onDetach();
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
		
		form.add(new TextField<String>("css", false));
		
		
		add(new EditButtonsV5<T>(this) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-xs";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-xs";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-xs";
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
		target.add( PortalCssEditor.this.getParent());
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add( PortalCssEditor.this.getParent());
	}

	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				//PortalObject pm = getPortalDao().findPortalObjectById( new ObjectID(getModel().getObject()));
				if (getModel().getObject() instanceof PortalObject) {
					Site site=((PortalObject)getModel().getObject()).getSite();
					site.getService(SiteService.class).update(getUpdatedParts());
				}
				super.reset();
				target.add( PortalCssEditor.this);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<T>(target, getModel(),  e));

		}
	}
	

	

}


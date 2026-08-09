package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

public class PortalPageEditor extends DomainObjectEditor<Page> {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalSiteEditor.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	
	
	public PortalPageEditor(String id, IModel<Page> model) {
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
		/**
		Label title = new Label("title", getModel().getObject().getTitle() + " <span class=\"suffix\">( " +getModel().getObject().getClassKey()+" ) </span>");
		title.setEscapeModelStrings(false);
		add(title);
		AjaxLink<Page> close = new AjaxLink<Page>("close", PortalPageEditor.this.getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll (new PortalCloseEditAjaxEvent<Page>(target, PortalPageEditor.this.getModel()));
			}
		};
		add(close);
	**/
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
		
		form.add(new TextField<String>("title", true));
		form.add(new TextField<String>("key", true));
		
		form.add(new BooleanField("isHome"));
		form.add(new BooleanField("isSiteSection"));
		
		// form.add(new BooleanField("isAggregator"));
		
		form.add(new TextField<String>("relativeUrl", true));
		form.add(new TextAreaField<String>("description", 4, 40, true));
		
		
		add(new EditButtonsV5<Page>(this) {
			
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
		target.add( PortalPageEditor.this.getParent());
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add( PortalPageEditor.this.getParent());
	}

	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				Page page = getPortalDao().findPageById(getModel().getObject().getId());
				Site site=page.getSite();
				site.getService(SiteService.class).update(getUpdatedParts());
				
				super.reset();
				target.add( PortalPageEditor.this.getParent());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Page>(target, getModel(), e));

		}
	}

 

}

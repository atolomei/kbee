package kbee.web.portal6.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PageSectionDisposition;
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

public class PortalPageSectionEditor extends DomainObjectEditor<PageSection> {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalPageSectionEditor.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	
	public PortalPageSectionEditor(String id, IModel<PageSection> model) {
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
				AjaxLink<PageSection> close = new AjaxLink<PageSection>("close", PortalPageSectionEditor.this.getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll (new PortalCloseEditAjaxEvent<PageSection>(target, PortalPageSectionEditor.this.getModel()));
			}
		};
		add(close);


*
*/
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
		
		form.add(new TextField<String>("title", true));
		form.add(new TextField<String>("key"));
		form.add(new TextAreaField<String>("description", 4, 40, true));
		
		BooleanField he = new BooleanField("header", 	new PropertyModel<Boolean>(this, "header"));
		form.add(he);

		
		// PageSectionType
		// PageSectionDisposition
		
		//if (getModel()!=null && getModel().getObject()!=null)
		//	form.add((new StaticField<String>("id", new Model<String>(getModel().getObject().getId().toString()))).setVisible(isAdminSessionUser()));
		//else
		//	form.add(new InvisiblePanel("id"));
		
		add(new EditButtonsV5<PageSection>(this) {
			
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
	
	
	public Boolean getHeader() {
		return Boolean.valueOf(getModel().getObject().isHeader());
		
	}
	
	
	public void setHeader(Boolean b) {
		getModel().getObject().setHeader(b);
	}

	

	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
		target.add( PortalPageSectionEditor.this);
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add( PortalPageSectionEditor.this);
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				
				PageSection sec = getPortalDao().findPageSectionById(getModel().getObject().getId());
				Site site=sec.getSite();
				site.getService(SiteService.class).update(getUpdatedParts());

				
				super.reset();
				target.add( PortalPageSectionEditor.this);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<PageSection>(target, getModel(), e));

		}
	}



}

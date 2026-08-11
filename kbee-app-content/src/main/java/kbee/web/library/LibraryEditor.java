package kbee.web.library;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.library.Library;
import com.novamens.content.service.DomService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;
import kbee.web.searcher.page.SearcherHomePage;

import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.form.StaticField;

@SuppressWarnings("serial")
public class LibraryEditor extends ObjectEditor<Library> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(LibraryEditor.class.getName());
	final boolean is_root 			=  ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 

	class IqlValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String statement = validatable.getValue();
			try {
				if ((statement==null || "".equals(statement)) && LibraryEditor.this.getModelObject().isCanonical())
					return;
				IqlService iqlservice = getDomain().getService(IqlService.class);
				ResultSet set = iqlservice.execute(statement);
				set.hasNext();
			} 
			catch (RuntimeException e) {
				logger.error(e);
				validatable.error(new ValidationError(this));
			}
		}
	}

	
	public LibraryEditor(IModel<Library> model) {
		this("editor", model, false);
	}

	
	public LibraryEditor(String id, IModel<Library> model, boolean isnew) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
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
		form.add(new TextField<String>("key", true));
		form.add((new WebMarkupContainer("iscanonical")).setVisible(getModelObject().isCanonical()));
		form.add(new StaticField<String>("id", new Model<String>( String.valueOf(getModel().getObject().getId()))));
		form.add(new TextField<String>("displayName", true));
		form.add(new TextAreaField<String>("description"));
		form.add(new TextAreaField<String>("statement", new IqlValidator(), 4, 4) {
			@Override
			public boolean isHelpInfo(){
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return  new StringResourceModel("how-to-criteria", LibraryEditor.this, null).getObject(); }, 
					getPredicatesHelp() 	/** getText("statement.helptext") **/);
			}
		});
		
		form.add( (new BooleanField("readOnly")).setVisible(false));
		form.add(new NumberField<Integer>("listOrder"));
		form.add(new TextField<String>("page") {
			public boolean isVisible() {
				return is_root;
			}
		});
		
		add(form);
		
		Link<Void> link = new Link<Void>("portal") {
			@Override
			public void onClick() {
					Site site = getPortalDao().getLibrarySite(LibraryEditor.this.getModel().getObject());
					if (site!=null) 
						setResponsePage(new SearcherHomePage(new ObjectModel<Site>(site)));
					else
						setResponsePage(new ApplicationErrorPage<>(new Model<String>("site is null")));
			}
		};
		form.add(link);
		
		
		add(new EditButtonsV5<Library>(this) {
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
		
		add(new InfoDialog("help-modal"));
	}

	public void onClose(AjaxRequestTarget target) {
		
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		onCancel(target);
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				
				getModelObject().getService(DomService.class).update(getUpdatedParts());
				
				if (isNew()) {
					Site site=getPortalDao().getLibrarySite(getModelObject());
					if (site!=null) {
						site.setDescription(this.getModelObject().getDescription());
						site.setUrl("lib/"+getModelObject().getKey());
						site.getService(SiteService.class).save();
					}
				}
				
				super.reset();
				target.add(getPage());
			}
		}
		catch (Exception e) {

			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}
	}
	
	public IModel<String> getText(String key) {
		return new StringResourceModel(key, this, null);
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
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	protected IModel<String> getPredicatesHelp() {
		return new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getPredicatesHelp());
	}
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}

}
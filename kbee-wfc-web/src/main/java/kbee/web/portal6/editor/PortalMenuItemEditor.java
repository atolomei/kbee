package kbee.web.portal6.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PortalMenuItem;
import com.novamens.portal6.model.PortalPersistentMenu;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;




import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.portal6.event.PortalMenuEditEvent;
import kbee.web.portal6.event.PortalMenuEditUpdateEvent;
import kbee.web.portal6.panel.PortalErrorPanel;

public class PortalMenuItemEditor extends ObjectEditor<PortalMenuItem> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalMenuItemEditor.class.getName());

	
	public PortalMenuItemEditor(String id, IModel<PortalMenuItem> model, boolean is_editing) {
		super(id, model);
		setEditionEnabled(is_editing);
		add(new InvisiblePanel("error-panel"));
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<PortalMenuEditEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalMenuEditEvent event) {
					if (event.isEditing()) {
						setEditionEnabled(true);
					}
					else
						setEditionEnabled(false);
					
					logger.debug(getModel().getObject().getDisplayName() + " edit: " + (event.isEditing()?"true":"false"));
			}
		});
		
		
		add(new WicketEventListener<PortalMenuEditUpdateEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalMenuEditUpdateEvent event) {
				PortalMenuItemEditor.this.update(event.getRequestTarget());
				logger.debug(" PortalMenuEditUpdateEvent -> " + getModel().getObject().getDisplayName());
			}
		});

	}
	
	public void onInitialize() {
		super.onInitialize();
		logger.debug("onInitialize() -> " +  getModel().getObject().getDisplayName() );
		
		setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
		
		form.add(new TextField<String>("title", true) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void onUpdate(AjaxRequestTarget target) {
				
				logger.debug(getValue());
				
				PortalMenuItemEditor.this.getModel().getObject().setTitle(getValue());
			}
		});
	

		form.add(new TextField<String>("HRef", true) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void onUpdate(AjaxRequestTarget target) {
				logger.debug(getValue());
				PortalMenuItemEditor.this.getModel().getObject().setHRef(getValue());
			}
			
			@Override
			public IModel<String> getLabel() {
				return new Model<String>("link");
			}
		});

	 
	 		
			
	}
	
	
		@Override
		public void edit(AjaxRequestTarget target) {
			super.edit(target);
			target.add( PortalMenuItemEditor.this.getParent());
		}
				
		@Override
		public void cancel(AjaxRequestTarget target) {
			super.cancel(target);
			target.add( PortalMenuItemEditor.this.getParent());
		}

		
		@Override
		public void update(AjaxRequestTarget target) {
			try {
				
				
				logger.debug("update ->" + getModel().getObject().treeString());
				
				//if (!getUpdatedParts().isEmpty()) {
					// Page page = getPortalDao().findPageById(getModel().getObject().getId());
					
					// PortalPersistentMenu menu =  getRepository(PortalPersistentMenu.class).findById(getModel().getObject().getId());
					// Site site=menu.getSite();
					// site.getService(SiteService.class).update(getUpdatedParts());
					// super.reset();
					// target.add( PortalMenuItemEditor.this.getParent());
					
				// }
			}
			catch (Exception e) {
				// logger.error(e);
				fire(new ErrorEvent<PortalMenuItem>(target, getModel(), e));

			}
		}
	
	

}

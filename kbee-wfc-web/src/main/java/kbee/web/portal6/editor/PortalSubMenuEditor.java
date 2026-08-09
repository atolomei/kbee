package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PortalMenu;
import com.novamens.portal6.model.PortalMenuItem;
import com.novamens.portal6.model.PortalPersistentMenu;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;

import kbee.web.portal6.event.PortalMenuEditEvent;
import kbee.web.portal6.event.PortalMenuEditUpdateEvent;
import kbee.web.portal6.panel.PortalErrorPanel;
			
public class PortalSubMenuEditor extends ObjectEditor<PortalMenu> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalSubMenuEditor.class.getName());

	
	private static final long serialVersionUID = 1L;

	private List<PortalMenuItem> list = null;
	PortalMenuItemListPanel list_menu_items;
	IModel<PortalPersistentMenu> model_owner;
	
	
	public PortalSubMenuEditor(String id, IModel<PortalMenu> model, IModel<PortalPersistentMenu> model_owner, boolean is_edition) {
		super(id, model);
		setEditionEnabled(is_edition);
		model_owner =model_owner;
	}
	
 	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ErrorEvent<?>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ErrorEvent<?> event) {
					Panel err= new PortalErrorPanel<>("error-panel", event.getThrowable());
					PortalSubMenuEditor.this.addOrReplace(err);
					event.getRequestTarget().add(PortalSubMenuEditor.this);
			}
		});
		
		
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
				PortalSubMenuEditor.this.update(event.getRequestTarget());
				
				logger.debug(" PortalMenuEditUpdateEvent -> " + getModel().getObject().getDisplayName());
			}
		});

	}
	
 	@Override
	public void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
		
		logger.debug("onInitialize() -> " +  getModel().getObject().getDisplayName() );
		
		add(new InvisiblePanel("error-panel"));
		
		
		
		//Label title = new Label("title", getModel().getObject().getDisplayName());
		//title.setEscapeModelStrings(false);
		//add(title);
	
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
		
		form.add(new TextField<String>("title", true) {
				private static final long serialVersionUID = 1L;
				public void onUpdate(AjaxRequestTarget target) {
					PortalSubMenuEditor.this.getModel().getObject().setTitle(getValue());
				}
				
				@Override
				public IModel<String> getLabel() {
					return new Model<String>("Title");
				}
				
		});
		
		
		form.add(new TextField<String>("HRef", true) {
			
			private static final long serialVersionUID = 1L;

			public void onUpdate(AjaxRequestTarget target) {
				logger.debug(getValue());
				PortalSubMenuEditor.this.getModel().getObject().setHRef(getValue());
			}
			
			@Override
			public IModel<String> getLabel() {
				return new Model<String>("link");
			}
		});
		
		list_menu_items =new PortalMenuItemListPanel("menu-items-list", getModel(), model_owner, isEditionEnabled());
		add(list_menu_items);
		

	}
 	
	@Override
	public Editor<PortalMenu> getEditor() {
		return PortalSubMenuEditor.this;
	}

 	

	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
		target.add( PortalSubMenuEditor.this.getParent());
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add( PortalSubMenuEditor.this.getParent());
	}

	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
	
			logger.debug("update ->" + getModel().getObject().treeString());
			
			//if (!getUpdatedParts().isEmpty()) {
			// }
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<PortalMenu>(target, getModel(), e));

		}
	}

	
	public void onDetach() {
		super.onDetach();
		list=null;
		logger.debug("onDetach() -> " +  getModel().getObject().getDisplayName() );
	} 
	
	
	public void onBeforeRender() {
		super.onBeforeRender();
		logger.debug("onBeforeRender() ->  " + getModel().getObject().getDisplayName() );
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public List<PortalMenuItem> getItems() {
	
		if (list!=null)
			return list;
		
		list = new ArrayList<PortalMenuItem>();
		
		for (PortalMenuItem p: getModel().getObject().getPortalMenuItems()) {
			list.add(p);
		}
		
		/**
		list.sort(new Comparator<Page>() {

			@Override
			public int compare(Page o1, Page o2) {
				
				try {
					
					if (o1.getOrder()<o2.getOrder()) return -1;
					if (o1.getOrder()>o2.getOrder()) return 1;
					
					if (o1.getTitle()==null) return 1;
					if (o2.getTitle()==null) return -1;
					
					return o1.getTitle().compareToIgnoreCase(o2.getTitle());
					
				} catch (Exception e) {
					logger.error(e);
				}
				return 0;
			}
			
		});
		*/
		
		return  list;
		
	}

}

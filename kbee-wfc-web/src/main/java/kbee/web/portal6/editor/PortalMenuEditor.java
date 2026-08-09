package kbee.web.portal6.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.dom.ObjectState;
import com.novamens.kbee.portal.model.KbeePortalMenu;
import com.novamens.kbee.portal.model.KbeePortalMenuItem;
import com.novamens.kbee.portal.model.KbeePortalPersistentMenu;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PortalMenu;
import com.novamens.portal6.model.PortalMenuItem;
import com.novamens.portal6.model.PortalPersistentMenu;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.portal6.event.PortalEditEvent;
import kbee.web.portal6.event.PortalMenuEditEvent;
import kbee.web.portal6.event.PortalMenuEditUpdateEvent;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.portal6.panel.Site6HitPanel;
import kbee.web.portal6.panel.Site6NonHibernateHitPanel;


/**
 * 
 * 
 * PortalMenuItemPanel 
 * ->
 * PortalMenuItemLabelPanel [title, href]
 * PortalMenuItemSubmenuPanel [title, href, List <PortalMenuItemPanel>]
 * 
 *
 */
public class PortalMenuEditor extends DomainObjectEditor<PortalPersistentMenu> {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalMenuEditor.class.getName());
	
	private List<PortalMenuItem> list = null;
	private PortalMenuItemListPanel list_menu_items;
	
	public PortalMenuEditor(String id, IModel<PortalPersistentMenu> model) {
		super(id, model);
		
		setEditionEnabled(false);
		add(new InvisiblePanel("error-panel"));
		logger.debug(getModel().getObject().treeString());
	}
	
	
 	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ErrorEvent<?>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ErrorEvent<?> event) {
					Panel err= new PortalErrorPanel<PortalPersistentMenu>("error-panel", event.getThrowable());
					PortalMenuEditor.this.addOrReplace(err);
					event.getRequestTarget().add(PortalMenuEditor.this);
			}
		});
	}
	
	
 	@Override
	public void onInitialize() {
		super.onInitialize();
		
		logger.debug("onInitialize() -> " +  getModel().getObject().getDisplayName() );
		
		setOutputMarkupId(true);
		
		Label title = new Label("title", getModel().getObject().getTitle() + " <span class=\"suffix\">( " +getModel().getObject().getClassKey()+" ) </span>");
		title.setEscapeModelStrings(false);
		add(title);
	
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
		
		form.add(new TextField<String>("title", true));
		//form.add(new TextField<String>("key", false));
		//form.add(new TextAreaField<String>("description", 4, 40, false));
		
		PortalMenu menu = getModel().getObject().getPortalMenu();
		
		list_menu_items =new PortalMenuItemListPanel("menu-items-list", new Model<PortalMenu>(menu), getModel(), isEditionEnabled());
		add(list_menu_items);
		
	 
		// click edit -> tell the panels edit on
 		add(new EditButtonsV5<PortalPersistentMenu>(this) {
			
			private static final long serialVersionUID = 1L;

			public void onEditClick(AjaxRequestTarget target) {
				super.onEditClick(target);
				list_menu_items.setEdition(true);
				fireScanAll (new PortalMenuEditEvent(target, getModel(), true));
				target.add(PortalMenuEditor.this);
			}
			
			
			public void onCancelClick(AjaxRequestTarget target) {
				super.onCancelClick(target);
				list_menu_items.setEdition(false);
				fireScanAll (new PortalMenuEditEvent(target, getModel(), false));
				target.add(PortalMenuEditor.this);
			}
			
			public void onSubmitClick(AjaxRequestTarget target) {
				super.onSubmitClick(target);
				list_menu_items.setEdition(false);
				fireScanAll  (new PortalMenuEditEvent(target, getModel(), false));
				target.add(PortalMenuEditor.this);
			}
			
			
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
		
		
		AjaxLink<PortalPersistentMenu> close = new AjaxLink<PortalPersistentMenu>("close", PortalMenuEditor.this.getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new PortalCloseEditAjaxEvent<PortalPersistentMenu>(target, PortalMenuEditor.this.getModel()));
			}
		};
		add(close);
	}
 	
 	
 	
	 

	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
		target.add(PortalMenuEditor.this.getParent());
		logger.debug("edit");
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add( PortalMenuEditor.this.getParent());
		logger.debug("cancel");
	}

	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			
			logger.debug("update");
			
			logger.debug(getModel().getObject().treeString());
			
			fireScanAll(new PortalMenuEditUpdateEvent(target, PortalMenuEditor.this.getModel()));
			
			
			logger.debug(getModel().getObject().treeString());
			
		//	if (!getUpdatedParts().isEmpty()) {
				
				
			
				
				PortalPersistentMenu menu =  getRepository(PortalPersistentMenu.class).findById(getModel().getObject().getId());
				
				Site site=menu.getSite();
				site.getService(SiteService.class).update(getUpdatedParts());
				
				super.reset();
				target.add( PortalMenuEditor.this.getParent());
		//	}
		}
		catch (Exception e) {
			// logger.error(e);
			fire(new ErrorEvent<PortalPersistentMenu>(target, getModel(), e));

		}
	}

	
	public void onDetach() {
		super.onDetach();
		list=null;
		
		logger.debug("onDetach");
	} 
	
	
	
	/**
	 * 
	 * @return
	 */
	public List<PortalMenuItem> getItems() {
	
		if (list!=null)
			return list;
		
		list = new ArrayList<PortalMenuItem>();
		
		for (PortalMenuItem p: getModel().getObject().getMenuItems()) {
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

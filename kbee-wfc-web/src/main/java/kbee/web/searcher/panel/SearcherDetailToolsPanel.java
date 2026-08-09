package kbee.web.searcher.panel;

import java.io.File;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.userlist.UserListItem;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.event.ShareContentEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ContentExportService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;

import kbee.web.content.menu.DeleteMenuItem;
import kbee.web.content.menu.ProcessLauncherMenu;

import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;

/**
 * Vote
 * Comments
 * --
 * Send by Email
 * Add to Favs
 * Download
  * 
 * @param <T>

 */
@SuppressWarnings("serial")
public class SearcherDetailToolsPanel<T extends Content> extends SearcherDetailPanel<T> {
	private static final long serialVersionUID = 1L;


	private Boolean readOnly;
	
	
	public SearcherDetailToolsPanel(String id, IModel<T> model,  IModel<Site> site_model) {
		super(id, model, site_model);
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		 add(getMenu());
	}
	
	
	protected Panel getMenu() {

		if (getSessionUser()==null)
			return new InvisiblePanel("menu");
			
		ContextMenuPanel<T> menu = new ContextMenuPanel<T>(getModel());
		
		
		int index = 0;
		if (getModel().getObject().isHeadVersion()) {
			menu.addItem(id ->
				new HeaderMenuItemPanelV5<T>(id) {
					@Override
					public String getLabel() {
						return getLabelString("edition");
					}
			});
			for (MenuItemFactory<T> item : (new ProcessLauncherMenu<T>(getModel())).getItems()) {
				index++;
				menu.addItem(item);
			}
		}
		
		if (index>=0)  {
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<T>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				});
		}
		
		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						SearcherDetailToolsPanel.this.fireScanAll(new 
								ShareContentEvent<T>(target, SearcherDetailToolsPanel.this.getModel()));
					}
					@Override
					public String getLabel() {	
						return getLabelString("send-email");
					}
					
					@Override 
					public boolean isVisible() {
						if ( SearcherDetailToolsPanel.this.getModel().getObject().getState()==ObjectState.ENABLED ||
							 SearcherDetailToolsPanel.this.getModel().getObject().getState()==ObjectState.ARCHIVED)
						return true;
						return false;
					}

				};	
			}
		});
		
		
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<T>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
		});

		
		if (getModel().getObject().getState()!=ObjectState.DELETED) {
			menu.addItem(id -> 
				new DeleteMenuItem<T>(id, this) {
					protected void refresh(AjaxRequestTarget target) {
						target.add(getPage());
					}
			});
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<T>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
			});
		}
		

		
		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new SubMenuAjaxUserListItemPanel<T>(id, SearcherDetailToolsPanel.this.getModel(), SearcherDetailToolsPanel.this.getName(), getSiteModel(), UserListItem.PUBLISHED);
			}
		});

		
		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						
					}
					@Override
					public String getLabel() {	
						return new StringResourceModel("add-favorites", SearcherDetailToolsPanel.this, null).getObject();
					}
					
					public boolean isVisible() {
						return false;
					}
				};	
			}	
		});

		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new AjaxMenuItemPanelV5<T>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						
					}
					@Override
					public String getLabel() {	
						return new StringResourceModel("remove-favorites", SearcherDetailToolsPanel.this, null).getObject();
					}
					
					public boolean isVisible() {
						return false;
					}
				};	
			}	
		});
		
		
		menu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
					return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<T>(id) {
						@Override 
						public String getLabel() {
								return new StringResourceModel("download", SearcherDetailToolsPanel.this, null).getObject();
						}
						@Override
						public boolean isDeleteFileAfterDownload()  {
							return true;
						}
						@Override
						protected File getFile() {
							File file = getModelObject().getService(ContentExportService.class).getHTMLExport();
							return file;
						}
						@Override 
						public boolean isEnabled() {
							return true;
						}
						@Override
						public boolean isVisible()  {
						
							if (SearcherDetailToolsPanel.this.getSiteModel()!=null)
								return false;
							return true;
						}
					};
			}
		});

		return menu;
	}
	

	
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	}
	protected boolean isInternalInfoReadable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getModelObject(), getSessionUser());
	}
	
	public boolean isReadOnly() {
		
		if (this.readOnly!=null)
			return this.readOnly.booleanValue();
		//  -------------------------------
		//
		// External Files are read-only
		//
		if (getModel().getObject().isExternal()) {
			this.readOnly=Boolean.valueOf(true);
			return this.readOnly.booleanValue();
		}

		//  -------------------------------
		//
		// user does not have permission to write 
		//
		if (!isWriteable( getModel() )) {
			this.readOnly=Boolean.valueOf(true);
			return this.readOnly.booleanValue();
		}

		// -------------------------------
		//
		// Archived can only be moved to the Library
		//
		if (getModelObject().isArchived()) { 
				this.readOnly=Boolean.valueOf(true);
				return this.readOnly.booleanValue();
		}
		
		//  -------------------------------
		//
		// Recycled can be Restored
		// 
		if (getModelObject().isRecycled()) { 
			this.readOnly=Boolean.valueOf(false);
			return this.readOnly.booleanValue();
		}

		// If at least one of the Libraries of the file is not ReadOnly 
		// 
		if (getModelObject().isEnabled()) {
			List<Library> libraries = getModelObject().getDomain().getService(LibraryService.class).getLibraries(getModelObject());
			if (!libraries.isEmpty()) { 
				for (Library li: libraries)
					if (!li.isReadOnly()) {
						this.readOnly=Boolean.valueOf(false);
						return this.readOnly.booleanValue();
					}
			}
		}
		
		this.readOnly = Boolean.valueOf(true);
		return readOnly.booleanValue();
	}

	protected boolean isWriteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}
	
	protected boolean isDeleteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable(model.getObject());
	}
}

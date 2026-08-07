package com.novamens.content.web.content.markup;

import java.io.File;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.web.resource.markup.ResourcesPanel;
import com.novamens.kbee.content.service.datamanagement.KbeeFileSystemExporter;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ContentExportService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;


/** 
 * @param <T>
 */
public class PrivatePanel<T extends Content> extends ModelPanel<T>  {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PrivatePanel.class.getName());
	
	public PrivatePanel(IModel<T> model) {
		this("private-panel", model);
	}

	public PrivatePanel(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
		// add(new PrivateNotesPanel<T>("private-notes"));
	}
	
	boolean edition_enabled = false;
	boolean ispublic_area = false;
	
	public PrivatePanel(String id, IModel<T> model, boolean edition_enabled) {
		super(id, model);
		setOutputMarkupId(true);
		this.edition_enabled=edition_enabled;
	}
	 
	
	public void onInitialize() {
		super.onInitialize();

		ResourcesPanel<T> panel = new ResourcesPanel<T>("private-resources", ispublic_area);
		add(panel);

		/**
		ContextMenuPanel<T> dmenu = new ContextMenuPanel<T>("dmenu", getModel());
		
		dmenu.addItem(new MenuItemFactory<T>() {
			@Override
			public AbstractMenuItemPanelV5<T> getItem(String id) {
				return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<T>(id) {
					private static final long serialVersionUID = 1L;

					@Override
					public String getLabel() {
						 return new StringResourceModel("downloadall", PrivatePanel.this, null).getObject();
					}
					@Override
					public boolean isVisible() {
						return true;
					}
					@Override
					protected File getFile() {
						return getModelObject().getService(ContentExportService.class).getPrivateResourcesExport();
					}
					@Override
					public boolean isEnabled()  {
						try {
							return (isRoot() || !isSupport());
						} catch (Exception e) {
							logger.error(e);
							return false;
						}
					}
				};
			}
		});

		add(dmenu);
		**/

	}
	
	protected boolean isAdmin() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}

	protected boolean isSupport() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(SecurityService.class).isRoot();
	}

}

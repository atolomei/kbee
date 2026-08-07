 package com.novamens.content.web.content.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.content.base.Content;
import com.novamens.content.model.ModelSection;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.kbee.content.model.KbeeModelSection;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.content.panel.ContentLibraryPanel;
import kbee.web.model.util.DefaultSectionModel;

/**
 * Right Panel for Read only pages. 
 * Like Content Detail from Library,
 * including Version Control pages 
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class ContentInfoPanel<T extends Content>  extends ModelPanel<T>  {
	private static final long serialVersionUID = 1L;
	
	private boolean show_only_notempty = false;
	
	
	/**
	 * @param model
	 */
	public ContentInfoPanel(IModel<T> model) {
			this(model, false);
	}
	
	public ContentInfoPanel(IModel<T> model, boolean show_only_notempty) {
		super("content-info", model);
		
		this.show_only_notempty=show_only_notempty;
		
		setOutputMarkupId(true);
		
		add(new AjaxLink<Void>("close-link") {
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
		});
		
		List<ITab> tabs = new ArrayList<ITab>();
		
//		for (ModelSection section : getModelObject().getContentTemplate().getSections()) {
//			if (section!=null) {
//				IModel<String> sectionlabel = section.getName()==null || "".equals(section.getName()) ? new StringResourceModel("editor.info", this, null) : new Model<String>(section.getName());
//				IModel<ModelSection> sectionmodel = getModel(section);
//				tabs.add(new AbstractTab(sectionlabel) {
//					@Override
//					public Panel getPanel(String panelId) {
//						ContentClassificationEditor<T> panel = new ContentClassificationEditor<T>(panelId, sectionmodel, true);
//						panel.setShowOnlyNotEmpty(ContentInfoPanel.this.show_only_notempty);
//						return panel;
//					}
//				});
//			}
//			
//		}
//		
		tabs.add(new AbstractTab(new StringResourceModel("editor.version.control", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new ContentLibraryPanel<T>(panelId, getModel(), getSiteModel(), isConsole());
			}
		});


		/**
		tabs.add(new AbstractTab(new StringResourceModel("editor.version.control", this, null)) {
				@Override
				public Panel getPanel(String panelId) {
					return new HistoryPanel<T>(panelId, getModel());
				}
		});
		**/
			
		
		AjaxTabbedPanel<?> tabbedpanel = new AjaxTabbedPanel<ITab>("tabs",   tabs) {
			@Override
			protected String getNavCss() {
				return "nav nav-pills nav-horizontal nav-justified";
			}
		};
		
		add(tabbedpanel);
	}

	
	protected IModel<Site> getSiteModel() {
		return null;
	}

	protected boolean isConsole() {
		return true;
	}

	public void onClose(AjaxRequestTarget target) {
	}
	
	protected boolean isWriteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}
	
	protected boolean isMonitorable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(model.getObject());
	}
	
	protected boolean isDeleteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable(model.getObject());
	}
	
	protected boolean isDeleteable(T content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable(content);
	}
	
	protected IModel<ModelSection> getModel(ModelSection section) {
		return section instanceof KbeeModelSection && ((KbeeModelSection)section).isDefault() ? 
			new DefaultSectionModel(section) : 
			new ObjectModel<ModelSection>(section);
	}
	
	protected boolean isAdminUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
}
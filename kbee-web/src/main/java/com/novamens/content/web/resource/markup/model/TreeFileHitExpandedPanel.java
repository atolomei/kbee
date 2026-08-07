package com.novamens.content.web.resource.markup.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.document.TreeFile;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.HitExpandedPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.tree.TreeFileProvider;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.tree.TreeNodePanel;

public class TreeFileHitExpandedPanel extends Panel implements HitExpandedPanel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TreeFileHitExpandedPanel.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private IModel<TreeFile> model;

	public TreeFileHitExpandedPanel(String id, IModel<TreeFile> model) {
		super(id, model);
		setOutputMarkupId(true);
		setModel(model);
	}
	

	public IModel<TreeFile> getModel() {
		return model;
	}


	public void setModel(IModel<TreeFile> model) {
		this.model = model;
	}

	public void onDetach() {
		super.onDetach();
		if (model!=null)
			this.model.detach();
	}
	
	@SuppressWarnings("serial")
	public void onInitialize() {
		super.onInitialize();
		
		List<ITab> tabs = new ArrayList<ITab>();

		// Tree 
		//
		tabs.add(new AbstractTab(getLabel("tree")) {
			@Override
			public Panel getPanel(String panelId) {
				try {
					
					// IModel<TreeNode> tree_model = new TreeFileNodeModel(TreeFileHitExpandedPanel.this.getModel());
					// return new TreeNodePanel(panelId, tree_model);
					
					 DefaultNestedTree<TreeFile> tree_panel = new DefaultNestedTree<TreeFile>(panelId, new TreeFileProvider(TreeFileHitExpandedPanel.this.getModel())) {
						 
						 /**
						 * To use a custom component for the representation of a node's content we would
						 * override this method.
						 */
						@Override
						protected org.apache.wicket.Component newContentComponent(String id, IModel<TreeFile> node)
						{
								return new Folder<TreeFile>(id, this, node) {
									@Override
									protected IModel<String> newLabelModel(IModel<TreeFile> model) {
										return new Model<String>(model.getObject().getTitle());
									}		
								};
						};
						 
					 };
					 return tree_panel;
							 
				} 
				catch (Exception e) {
					logger.error(e);
					return (Panel) add(new InvisiblePanel(panelId));
				}
			}
		});


		// Info
		//
		tabs.add(new AbstractTab(getLabel("info")) {
			@Override
			public Panel getPanel(String panelId) {
				try {
					return new InvisiblePanel(panelId);
				} 
				catch (Exception e) {
					logger.error(e);
					return (Panel) add(new InvisiblePanel(panelId));
				}
			}
		});

			
		
		VerticalLayout<ITab> tabbedpanel = new VerticalLayout<ITab>("tabs",  this.getClass().getName(), tabs) {
			private static final long serialVersionUID = 1L;
			protected String getNavCss() {
				return "nav nav-tabs";
			}
		};
		
		add(tabbedpanel);

		
		
	}

	
	
	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model =  new StringResourceModel(key, this, null);
		model.setParameters((Object[])parameter);
		return model;
	}

	protected  User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	

	protected String getUserPreference(String key, String defaultvalue) {
		KbeeUser user = (KbeeUser) getUser();
		if (user!=null)
			return user.getService(PreferencesService.class).getValue("global-consoles", key, defaultvalue);
		return null;
	}	
	
}

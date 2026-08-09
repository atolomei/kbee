package com.novamens.kbee.wicket.markup.html.console.tree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.kbee.wicket.markup.html.console.event.SwitchPanelsEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.tree.TreeNodeSelection;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public  class TreePanel<T extends TreeNode<?>> extends DataViewPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(TreePanel.class.getName());
	
	private TreeProvider<T> provider;
	private AbstractTree<T> treeview;
	private IModel<T> selected;
	
	private class State implements IModel<Set<T>> {
		private Set<T> state;
		public State() {
		//	state = new HashSet<>();

			state = new HashSet<>() {
				@Override
				public boolean contains(Object object) {
					//boolean e = super.contains(object);
					Iterator<T> i = super.iterator();
					//Object o2 = ((T)object).getObject();
					while (i.hasNext()) {
						T o = i.next();
						//o.equals(object);
						//Object o1 = o.getObject();
						if (o.equals(object)) {
//						if (o1.equals(o2)) {
							return true;
						}
					}
					return false;
				}
			    public boolean remove(Object object) {
					Iterator<T> i = super.iterator();
					//Object o2 = ((T)object).getObject();
					while (i.hasNext()) {
						T o = i.next();
						//Object o1 = o.getObject();
						//if (o1.equals(o2)) {
						if (o.equals(object)) {
							return super.remove(o);
						}
					}
					return false;
			    }

			};
		}
		public Set<T> getObject() {
			return state;
		}
		public void setObject(Set<T> ste) {
			state = ste;;
		}
	}
	
	private State state = new State();

	public TreePanel(String id, TreeProvider<T> provider, T node) {
		super(id, null);
		setOutputMarkupId(true);
		this.provider = provider;
		if (node!=null) {
			this.selected = provider.model(node);
			for (com.novamens.content.tree.TreeNode n : node.getTreePath().getNodes()) {
				T t = provider.getNode(n.getObject(), n.getPath());
				state.getObject().add(t);
			}
		}
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("container")==null) {
			initPreferences();
			addContainer();
			addTree();
			
			add(new WicketEventListener<TreeNodeSelection<T>>() {
				@Override
				public void onEvent(TreeNodeSelection<T> event) {
					if (selected==null || event.getObject()==null || !event.getObject().equals(selected.getObject())) {
						if (selected!=null && event.getRequestTarget()!=null) {
				            treeview.updateNode(selected.getObject(), event.getRequestTarget());
						}
						selected = event.getModel();
						if (selected!=null && event.getRequestTarget()!=null) {
							treeview.updateNode(selected.getObject(), event.getRequestTarget());
							T node = selected.getObject();
							for (com.novamens.content.tree.TreeNode n : node.getTreePath().getNodes()) {
								T t = provider.getNode(n.getObject(), n.getPath());
								treeview.expand(t);
								state.getObject().add(t);
							}
						}
			            onSelect(event.getRequestTarget(), event.getNode());
					}
				}
			});
		}
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}	


	@Override
	protected String getContextKey() {
		return "tree/";
	}

	protected void addContainer() {
		WebMarkupContainer container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		add(container);
		
		WebMarkupContainer menu = new WebMarkupContainer("menu-container") {
			public boolean isVisible() {
				return true;
			}
		};
		menu.add(getMenu());
		add(menu);
	}
	
	protected Set<T> getState() {
		Set<T> state = new HashSet<>();
		if (selected!=null) {
			TreeNode<?> node = selected.getObject();
			for (com.novamens.content.tree.TreeNode n : node.getTreePath().getNodes()) {
				T t = provider.getNode(n.getObject(), n.getPath());
				state.add(t);
			}
		}	
		return state;
	}
	
	protected void addTree() {
		try {
			
			treeview = new DefaultNestedTree<T>("tree", getTreeProvider(), state) {
				@Override
				protected Component newContentComponent(String id, IModel<T> node) {
					return new Folder<>(id, this, node) {
						protected Component newLabelComponent(String id, IModel<T> model) {
							return new Label(id, getDisplayName(model));
						}
						@Override
						protected void onClick(Optional<AjaxRequestTarget> targetOptional) {
							T node = getModelObject();
						 	if (selected!=null) {
						 		treeview.updateNode(selected.getObject(), targetOptional.get());
						 	}
							selected = getModel();
				            treeview.updateNode(selected.getObject(), targetOptional.get());
				            fireScanAll(new TreeNodeSelection<T>(targetOptional.get(), getModel()));
							onSelect(targetOptional.get(), node);
						}
						@Override
						protected boolean isClickable() {
							return true;
						}
						@Override
						protected boolean isSelected() {
							return selected!=null && selected.getObject().equals(getModelObject());
						}

						@Override
						protected String getStyleClass() {
							String styleClass;
							T node = getModelObject();
							if (treeview.getState(node) == State.EXPANDED)	{
								styleClass = getOpenStyleClass();
							}
							else {
								styleClass = getClosedStyleClass();
							}
							if (isSelected()) {
								styleClass += " " + getSelectedStyleClass();
							}
							return styleClass;
						}
					};
				}
			};
			
			treeview.setOutputMarkupId(true);
			if (selected!=null) {
	            treeview.updateNode(selected.getObject(), null);
			}   
	        		 
			((WebMarkupContainer) get("container")).add(treeview);
		} 
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e, "The tree panel can not be rendered due to an Application error please call support ");
			showError(new StringResourceModel("error-msg", TreePanel.this, null).getString() + 
				 e.getClass().getCanonicalName() + "<br />" + e.getMessage());
		}	
	}
	
	protected Panel getMenu() {
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (selected!=null)
		            saveState(target, selected.getObject());
				}	
				@Override
				public String getLabel() {	
					return getLabelString("menu.save");
				}
				@Override
				public boolean isEnabled() {	
					return true;
				}
		});
		
	 
		menu.addItem(itemId ->
			new AjaxMenuItemPanelV5<Void>(itemId) {
				@Override
				public void onClick(AjaxRequestTarget target) {
		            fireScanAll(new SwitchPanelsEvent(target));
				}	
				@Override
				public String getLabel() {	
					return getLabelString("switch-sides");
				}
			});
	 
		return menu;
	}
	
	@Override
	public DataView<SearchResult> getDataView() {
		return null;
	}
	
	protected void onSelect(AjaxRequestTarget target, T object) {
		
	}
	
	protected void saveState(AjaxRequestTarget target, T object) {
	}
	
	protected IModel<String> getDisplayName(IModel<T> model) {
		return new Model<String>() {
			public String getObject() {
				return DisplayNameExtractor.get(model.getObject());
			}
		};
	}
	
	protected IModel<T> getModel(T object) {
		return new ObjectModel<T>(object);
	} 
	
	protected TreeProvider<T> getTreeProvider() {
		return provider;
	}
	
	protected Panel getPanel(IModel<T> model, List<String> snippets) {
		return null;
	}
	
	private void initPreferences() {
		setPageSize(getIntUserPreference("page-size", PAGE_SIZE));
	}
 
}
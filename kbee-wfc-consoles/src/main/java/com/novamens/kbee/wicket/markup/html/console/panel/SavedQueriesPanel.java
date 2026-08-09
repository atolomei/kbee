package com.novamens.kbee.wicket.markup.html.console.panel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.userlist.UserListService;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.kbee.content.query.KbeeSavedQuery;
import com.novamens.kbee.security.KbeeUser;

public class SavedQueriesPanel extends KBPanel {
	private static final long serialVersionUID = 1L;

	static final int MAX = 60;
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SavedQueriesPanel.class.getName());

	private String console = null;

	protected final boolean root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	protected final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	private List<SavedQuery> queries;
	private Map<Serializable, Integer> totals = new HashMap<Serializable, Integer>();

	private Query query;
	private Serializable selected = null;
	private ListView<SavedQuery> lview;

	private boolean isActions;
	private boolean isAddToMain;

	private boolean exportSavedQueries = true;
	private boolean isClose = true;

	private IModel<Site> site_model;

	public SavedQueriesPanel(String id, String consoleName, Query query) {
		this(id, consoleName, null, query, true, false, true);
	}

	public SavedQueriesPanel(String id, String consoleName, IModel<Site> site_model, Query query, boolean isGridExport, boolean isAddToMain, boolean isActions) {
		super(id);
		setOutputMarkupId(true);
		this.isAddToMain = isAddToMain;
		this.isActions = isActions;

		this.site_model = site_model;

		add(new InvisiblePanel("savedquery-editor"));

		this.exportSavedQueries = isGridExport;
		this.selected = null;
		this.console = consoleName;
		this.query = query;

		add(new WicketEventListener<FilterSelectorClearAllEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(FilterSelectorClearAllEvent event) {
				selected = null;
				event.getRequestTarget().add(SavedQueriesPanel.this);
			}
		});
		logger.debug("SavedQueriesPanel -> " + consoleName + " " + (site_model != null ? site_model.getObject().getName() : "null"));
	}

	public void setIsClose(boolean b) {
		this.isClose = b;
	}

	public boolean isClose() {
		return this.isClose;
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (lview == null)
			addQueries();
	}

	/**
	 * The browser is not detached here, but in the Console.
	 */
	public void onDetach() {
		super.onDetach();

		this.queries = null;

		if (site_model != null)
			site_model.detach();
	}

	/**
	 * @return
	 */
	public List<SavedQuery> getQueries() {

		if (queries != null)
			return queries;

		if (site_model != null)
			queries = ((KbeeUser) getSessionUser()).getService(UserListService.class).getSavedQueries(this.site_model.getObject());
		else
			queries = ((KbeeUser) getSessionUser()).getService(UserListService.class).getSavedQueries(this.console);

		long start = System.currentTimeMillis();
		try {
			Map<String, Object> m1 = this.query.getParameters();
			Map<String, Object> m2 = new HashMap<String, Object>();
			m2.putAll(m1);

			Query solq = this.query;

			for (SavedQuery q : queries) {
				try {
					solq.setParameters(q.getParameters());
					totals.put(q.getId(), Integer.valueOf(solq.execute().size()));

				} catch (Exception e) {
					logger.error(e);
					totals.put(q.getId(), Integer.valueOf(-1));
				}
			}

			this.query.setParameters(m2);

		} catch (Exception e) {
			logger.error(e);
		} finally {
			logger.debug(String.valueOf(System.currentTimeMillis() - start) + " ms");
		}
		return queries;
	}

	/**
	 * @return
	 */
	protected void addQueries() {

		WorkingIndicatorAjaxLinkV5<Void> ral = new WorkingIndicatorAjaxLinkV5<Void>("remove-all") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				try {
					((KbeeUser) getSessionUser()).getService(UserListService.class).emptySavedQueriesList(console);
				} catch (ContentMgmtException e) {
					logger.error(e);
				}
				SavedQueriesPanel.this.queries = null;
				addQueries();
				target.add(SavedQueriesPanel.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", SavedQueriesPanel.this, null).getObject();
			}

			@Override
			public boolean isVisible() {
				return !getQueries().isEmpty();
			}
		};

		WorkingIndicatorAjaxLinkV5<Void> addmain = new WorkingIndicatorAjaxLinkV5<Void>("add-to-main") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				try {

				} catch (ContentMgmtException e) {
					logger.error(e);
				}
				target.add(SavedQueriesPanel.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", SavedQueriesPanel.this, null).getObject();
			}

			@Override
			public boolean isVisible() {
				return isAddToMain;
			}
		};

		WorkingIndicatorAjaxLinkV5<Void> co = new WorkingIndicatorAjaxLinkV5<Void>("close") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				try {
					close(target);
				} catch (ContentMgmtException e) {
					logger.error(e);
				}
				target.add(SavedQueriesPanel.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", SavedQueriesPanel.this, null).getObject();
			}

			@Override
			public boolean isVisible() {
				return isClose;
			}
		};

		WorkingIndicatorAjaxLinkV5<Void> cal = new WorkingIndicatorAjaxLinkV5<Void>("clear-all") {
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target) {
				try {
					clearAll(target);
					SavedQueriesPanel.this.queries = null;
					selected = null;
					addQueries();
				} catch (ContentMgmtException e) {
					logger.error(e);
				}
				target.add(SavedQueriesPanel.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working", SavedQueriesPanel.this, null).getObject();
			}

			@Override
			public boolean isVisible() {
				return true;
			}
		};

		WebMarkupContainer actions = new WebMarkupContainer("actions") {
			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				return isActions;
			}
		};

		addOrReplace(actions);

		actions.addOrReplace(ral);
		actions.addOrReplace(addmain);
		actions.addOrReplace(co);
		actions.addOrReplace(cal);

		lview = new ListView<SavedQuery>("query", new PropertyModel<List<SavedQuery>>(this, "queries")) {
			public void populateItem(final ListItem<SavedQuery> item) {
				WorkingIndicatorAjaxLinkV5<Void> link = new WorkingIndicatorAjaxLinkV5<Void>("link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						SavedQuery query = item.getModelObject();
						selected = query.getId();
						fireScanAll(new ApplySavedQueryEvent(target, query));
						close(target);
					}

					@Override
					protected String getWorkingLabel() {
						return new StringResourceModel("working", SavedQueriesPanel.this, null).getObject();
					}
				};

				StringBuilder str = new StringBuilder(pad(item.getModelObject().getTitle()));
				link.add((new Label("title", str.toString())).setEscapeModelStrings(false));

				StringBuilder to = new StringBuilder("(" + String.valueOf(totals.get(item.getModelObject().getId())) + ")");

				link.add((new Label("total", to.toString())).setEscapeModelStrings(false));

				item.add(link);

				if (selected != null && selected.toString().equals(item.getModelObject().getId().toString())) {
					item.add(new AttributeModifier("class", "list-group-item selected"));
				}

				item.add(getMenu(item.getModel()));
			};
		};

		lview.setOutputMarkupId(true);

		addOrReplace(lview);
	}

	/**
	 * 
	 * 
	 * @param model
	 * @return
	 */

	protected Panel getMenu(IModel<SavedQuery> model) {
		try {

			ContextMenuPanel<SavedQuery> menu = new ContextMenuPanel<SavedQuery>(model);

			menu.setOutputMarkupId(true);

			menu.addItem(new MenuItemFactory<SavedQuery>() {
				private static final long serialVersionUID = 1L;

				@Override
				public AbstractMenuItemPanelV5<SavedQuery> getItem(String id) {
					return new AjaxMenuItemPanelV5<SavedQuery>(id) {
						private static final long serialVersionUID = 1L;

						@Override
						public String getLabel() {
							return SavedQueriesPanel.this.getLabel("edit").getObject();
						}

						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								SavedQueriesPanel.this.addOrReplace(new SavedQueryEditorFragment("savedquery-editor", new ObjectModel<SavedQuery>(getModel().getObject()), false));
								target.add(SavedQueriesPanel.this);

							} catch (ContentMgmtException e) {
								logger.error(e);
							}
						}
					};
				}
			});

			if (isExportSavedQueries() && getGridExportSavedQueryMenuItem("id", model) != null) {
				menu.addItem(new AbstractMenuItemFactory<SavedQuery>(model) {
					private static final long serialVersionUID = 1L;

					@Override
					public AbstractMenuItemPanelV5<SavedQuery> getItem(String id) {
						try {
							return SavedQueriesPanel.this.getGridExportSavedQueryMenuItem(id, getModel());
						} catch (Exception e) {
							logger.error(e);
							return new SeparatorMenuItemPanelV5<SavedQuery>(id);
						}
					}
				});
			}

			menu.addItem(new MenuItemFactory<SavedQuery>() {
				private static final long serialVersionUID = 1L;

				@Override
				public AbstractMenuItemPanelV5<SavedQuery> getItem(String id) {
					return new AjaxMenuItemPanelV5<SavedQuery>(id) {

						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;

						@Override
						public String getLabel() {
							return SavedQueriesPanel.this.getLabel("delete").getObject();
						}

						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							try {
								((KbeeUser) getSessionUser()).getService(UserListService.class).delete(getModel().getObject());
								queries = null;
								addQueries();
								target.add(SavedQueriesPanel.this);
								fireScanAll(new MyListsDeleteListEvent(target));

							} catch (Exception e) {
								logger.error(e);
							}
						}
					};
				}
			});

			return menu;

		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("menu");
		}
	}

	protected void close(AjaxRequestTarget target) {
	}

	protected void clearAll(AjaxRequestTarget requestTarget) {
		fireScanAll(new FilterSelectorClearAllEvent(requestTarget));
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private String pad(String title) {
		if (title.length() > MAX) {
			return title.substring(0, MAX) + "...";
		}
		return title;
	}

	/**				
	 * 
	 *
	 */
	public class SavedQueryEditorFragment extends Fragment {
		private static final long serialVersionUID = 1L;

		IModel<String> name;
		IModel<SavedQuery> model;
		boolean is_new = false;

		public IModel<String> getName() {
			return this.name;
		}

		public void setName(IModel<String> s) {
			this.name = s;
		}

		public void setModel(IModel<SavedQuery> m) {
			this.model = m;
		}

		public IModel<SavedQuery> getModel() {
			return this.model;
		}

		public void onDetach() {
			super.onDetach();
			this.model.detach();
		}

		public SavedQueryEditorFragment(String id, IModel<SavedQuery> model, boolean is_new) {
			super(id, "savedquery-editor-fragment", SavedQueriesPanel.this);
			this.model = model;
			this.is_new = is_new;
			super.setOutputMarkupId(true);
		}

		/**
		 * 
		 */
		@Override
		public void onInitialize() {
			super.onInitialize();

			final Form<Void> form = new Form<Void>("form");
			add(form);

			String s = SavedQueryEditorFragment.this.getModel().getObject().getTitle();

			SavedQueryEditorFragment.this.setName(new Model<String>(s));

			TextField<String> code = new TextField<String>("name", new Model<String>() {
				public String getObject() {
					return SavedQueryEditorFragment.this.getModel().getObject().getTitle();
				}

				public void setObject(String s) {
					((KbeeSavedQuery) SavedQueryEditorFragment.this.getModel().getObject()).setTitle(s);
				}
			}, true);

			form.add(code);

			add(new AjaxSubmitLink("save", form) {
				@Override
				public void onSubmit(AjaxRequestTarget target) {
					try {
						@SuppressWarnings("unchecked")
						String na = ((TextField<String>) SavedQueryEditorFragment.this.get("form:name")).getValue();
						((KbeeSavedQuery) SavedQueryEditorFragment.this.getModel().getObject()).setTitle(na);
						((KbeeUser) getSessionUser()).getService(UserListService.class).save(SavedQueryEditorFragment.this.getModel().getObject());
						SavedQueryEditorFragment.this.setVisible(false);
					} catch (Exception e) {
						logger.error(e);
					}
					SavedQueriesPanel.this.queries = null;
					target.add(SavedQueriesPanel.this);
				}
			});

			add(new AjaxLink<Void>("cancel") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					SavedQueryEditorFragment.this.setVisible(false);
					target.add(SavedQueriesPanel.this);
				}
			});
		}
	}

	public void setExportSavedQueries(boolean b) {
		this.exportSavedQueries = b;
	}

	public boolean isExportSavedQueries() {
		return this.exportSavedQueries;
	}

	protected DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
		return null;
	}
}

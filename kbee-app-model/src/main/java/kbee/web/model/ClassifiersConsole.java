package kbee.web.model;


import java.io.File;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.entity.Person;
import com.novamens.content.library.Library;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ModelReference;
import com.novamens.content.model.ModelService;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.notes.Billboard;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.GridMenu;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;
import com.novamens.kbee.wicket.markup.html.console.browser.NewButton;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarAlert;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.util.BreadCrumb;
import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.workflow.ErrorPage;


/**
 * For the other components of the Information Model
 * see {@link ContentTemplatesConsoleConsole} {@link DataSetsConsole},  {@link AttributesConsole}, {@link ClassifiersConsole}
 *
 */
@SuppressWarnings("serial")						
public abstract class ClassifiersConsole extends  AbstractFacetedConsole<Classifier> {
			
	private static final long serialVersionUID = 1L;
												
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ClassifiersConsole.class.getName());

	private List<ToolbarItem> items;

	
	private IModel<Classifier> classifiermodel;
	private String console;
	private List<GridColumn<SearchResult,String>> columns;

	
	public ClassifiersConsole(Query query) {
		super("classifiers", query);
		this.is_deleted_visible = getUserPreference("deleted-visible", "no").equals("yes") ? true : false;
		setConsole(getName());
	}

	
	@Override
	protected String getIcon(IModel<Classifier> model) {
		return null;
	}
	

	
	@Override
	 protected  IModel<Classifier> getModel(Classifier object) {
			return new ObjectModel<Classifier>(object, true);
	}
	
	public void setClassifier(IModel<Classifier> model) {
		this.classifiermodel = model;
	}
	
	
	public Classifier getClassifier() {
		return classifiermodel.getObject();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
		if (this.classifiermodel!=null)
			this.classifiermodel.detach();
		this.items=null;
	}

	public String getConsole() {
		return this.console;
	}

	public void setConsole(String console) {
		this.console=console;
	}
	
	
	/**
	 * Check column for multiple selection
	 */
	@Override
	public boolean isSelectionEnabled() {
		return false;
	}
	
	
	@Override
	public Query newQuery() {
		return setUserPreference(new ClassifiersQuery(isDeletedVisible()));
	}
	
	
	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new ClassifiersBC());
	}
	
	@Override
	protected boolean hasExpander() {
		return true;
	}

	
	@Override
	protected Panel getMenu(IModel<Classifier> model) {
		
		ContextMenuPanel<Classifier> menu = new ContextMenuPanel<Classifier>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(new MenuItemFactory<Classifier>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
				return new AjaxMenuItemPanelV5<Classifier>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						getPage().setResponsePage(getClassifierPage(getModel(), 0, false, false));
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("open").getObject();
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Classifier>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
				return new AjaxMenuItemPanelV5<Classifier>(id) {
					public void onClick(AjaxRequestTarget target) {
						try {
							getModel().getObject().setState(ObjectState.ARCHIVED);
							getModel().getObject().getService(DOMObjectService.class).update(ObjectState.ARCHIVED.getLabel());
							FeedbackHelper.showInfoToast(getLabel()+ " <br/>" + getModel().getObject().getDisplayName());
							ClassifiersConsole.this.refresh(target);
						}
						catch (Exception e) {
							logger.error(e);
							throw new KbeeRuntimeException(e);
						}
					}
					
					@Override
					public boolean isVisible() {
						return getModel().getObject().getState()!=ObjectState.ARCHIVED;
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("bc.archive").getObject();
					}
				};
			}
		});
		
		
		
		
		menu.addItem(new MenuItemFactory<Classifier>() {
			@Override
			public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Classifier>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Classifier>() {
			private static final long serialVersionUID = 1L;
			@Override
			public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
				return new AjaxMenuItemPanelV5<Classifier>(id) {
					private static final long serialVersionUID = 1L;
					public void onClick(AjaxRequestTarget target) {
						
						try {
						StringBuilder str = new StringBuilder();
						for (ContentTemplate c:getContentDao().getContentTemplates(getDomain())) {
							
							if (c.getState()!=ObjectState.DELETED) {
								boolean found = false;
								for (ClassifierTemplate ct: c.getClassifiers()) {
									if (ct.getClassifier().equals( getModel().getObject())) {
										found = true;
										break;
									}
								}
								if (!found) {
									 KbeeClassifierTemplate c_te = new KbeeClassifierTemplate(getModel().getObject());						
									 c_te.setMetadataSubtitle(false);
									 c_te.setAccessibility(AccessStrategy.All);
									 c.addClassifier(c_te);
									 c.getService(DOMObjectService.class).update("Add Classifier -> " + getModel().getObject());
									 if (str.length()>0)
										 str.append(", ");
									 str.append(c.getDisplayName());
								}
							}
						}
						FeedbackHelper.showInfoToast( str.toString());
						} catch (Exception e) {
							logger.error(e);
							setResponsePage( new ApplicationErrorPage<>(e));
						}
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("add-to-all-content-templates").getObject();
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Classifier>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Classifier>(id) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<Classifier>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
				return new AjaxMenuItemPanelV5<Classifier>(id) {
					public void onClick(AjaxRequestTarget target) {
						List<ModelReference> references = getReferences(); 
						if (!references.isEmpty()) {
							String message = "The classifier cannot be deleted because it is referenced by</br>";
							for (int i=0; i<4 && i<references.size(); i++) {
								message += "</br><a target=\"_blank\" href=\""+references.get(i).getUrl()+"\">"+references.get(i).getDescription()+"</a>";
							}
							if (references.size()>4) {
								message += "</br></br> and others...";
							}
							InfoDialog infoDialog = (InfoDialog) getInformationModal();
							infoDialog.open(target,() -> {return "References";}, new Model<String>(message));
						}
						else {
							getConfirmationDialog().open(target, getConsoleLabel("deleteconfirmation.message", getModel().getObject().getDisplayName()), Dialog.Delete, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try {
											DOMObjectService objectService = getModel().getObject().getService(DOMObjectService.class);
											objectService.asyncDelete();
											
											FeedbackHelper.showInfoToast(getLabel()+ " <br/>" + getModel().getObject().getDisplayName());
										}
										catch (DataIntegrityViolationException | ConstraintException e) {
											getErrorDialog().open(target, getConsoleLabel("error.constraint"));
										}
										catch (Exception e) {
											getErrorDialog().open(target, new Model<String>(e.getMessage()));
										}
										ClassifiersConsole.this.refresh(target);
									}
								}
							});
						}
						refresh(target);
					}
					@Override 
					public String getLabel() {
						return getConsoleLabel("contextmenu.delete").getObject();
					}
					// Domain Basic no puede borrar Classifiers
					public boolean isEnabled() {
						
						if (getModel().getObject().getState()==ObjectState.DELETED)
							return false;

						
						if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS && !isRoot()) 
							return false;
						
						return !isSupport();
					}
					public List<ModelReference> getReferences() {
						return getDomain().getService(ModelService.class).getReferences(getModelObject());
					}
				};
			}
		});
		
			
		
		return menu;
	}

	
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		this.columns.add(new ObjectStatusColumn<Person>("iconstatus", getName(), new Model<String>("St")));
		
		
		
		{
			LinkPredicateKbeeGridColumn<Classifier> titleColumn = new LinkPredicateKbeeGridColumn<>("title", getLabel("name"), "title", obj -> obj.getDisplayName(), obj -> getModel(obj));
			titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
			titleColumn.setTarget(null);
			columns.add(titleColumn);
		}
    	
		{
			KbeePredicateGridColumn<Classifier> dataSetColumn = new KbeePredicateGridColumn<>("dataset", getLabel("dataset"), "dataset",	obj ->  obj.getDataSet().getDisplayName());
			 dataSetColumn.setHtmlValueResolver( obj -> getLink(obj.getDataSet()) );
			 dataSetColumn.setTextValueResolver( obj -> obj.getDataSet().getDisplayName()  	);
			
			dataSetColumn.setContextKey(this.getName() + dataSetColumn.getContextKey());
			this.columns.add(dataSetColumn);
		}
		
		this.columns.add(new LastModifiedColumn<Classifier>("modified", getLabel("modified"), "modified") {
			private static final long serialVersionUID = 1L;
			@Override
			protected OffsetDateTime getOffsetDateTime(Classifier object) {
					return object.getLastModifiedOffsetDateTime();
			}

			@Override
			protected String getContextKey() {
				return ClassifiersConsole.this.getName() + super.getContextKey();
			}

		});



		{
			KbeePredicateGridColumn<Classifier> statusColumn = new KbeePredicateGridColumn<>("status", getLabel("status"), "status",obj ->  obj.getState() != null ? obj.getState().getLabel(getUser().getLocale()) : "err"   );
			statusColumn.setHtmlValueResolver(obj -> obj.getState() != null ? obj.getState().getHTMLLabel(getUser().getLocale()) : "err");
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			this.columns.add(statusColumn);
		}

		{
			KbeePredicateGridColumn<Classifier> multiplicityColumn = new KbeePredicateGridColumn<>("multiplicity", getLabel("multiplicity"), obj ->  obj.getMultiplicity().getLabel(getUser().getLocale())  );
			multiplicityColumn.setContextKey(this.getName() + multiplicityColumn.getContextKey());
			this.columns.add(multiplicityColumn);
		}

		{
			KbeePredicateGridColumn<Classifier> statusColumn = new KbeePredicateGridColumn<>("contenttemplates", getLabel("contenttemplates"),		obj -> getContentTemplatesHTML(obj) );
			statusColumn.setHtmlValueResolver( obj -> getContentTemplatesHTML(obj) );
			statusColumn.setTextValueResolver( obj -> getContentTemplatesStr(obj) );
			statusColumn.setContextKey(this.getName() + statusColumn.getContextKey());
			statusColumn.setDefaultWidth(380);
			statusColumn.setPreferred(false);
			this.columns.add(statusColumn);
		}

		
		{
			KbeePredicateGridColumn<Classifier> classifierColumn = new KbeePredicateGridColumn<>("rules", getLabel("rules"),
					obj ->  getBooleanYesNoText(obj.isRuleCondition(),false) );
			classifierColumn.setHtmlValueResolver(obj ->getBooleanYesNoText(obj.isRuleCondition(),true));
			classifierColumn.setContextKey(this.getName() + classifierColumn.getContextKey());
			classifierColumn.setHeaderCssClass("centered");
			classifierColumn.setRowCssClass("centered");
			classifierColumn.setPreferred(false);
			this.columns.add(classifierColumn);
		}

		{
			KbeePredicateGridColumn<Classifier> multiplicityColumn = new KbeePredicateGridColumn<>("predicate", getLabel("predicate"),
					obj ->  obj.getPredicate()  );
			multiplicityColumn.setContextKey(this.getName() + multiplicityColumn.getContextKey());
			multiplicityColumn.setPreferred(true);
			this.columns.add(multiplicityColumn);
			
		}

		{
			KbeePredicateGridColumn<Classifier> multiplicityColumn = new KbeePredicateGridColumn<>("gridcolumn", getLabel("gridcolumn"),
			obj ->  getBooleanYesNoText(obj.isDefaultGridColumn(), false));
			multiplicityColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isDefaultGridColumn(), true));
			multiplicityColumn.setContextKey(this.getName() + multiplicityColumn.getContextKey());
			multiplicityColumn.setRowCssClass("centered");
			multiplicityColumn.setHeaderCssClass("centered");
			multiplicityColumn.setPreferred(false);
			
			this.columns.add(multiplicityColumn);
		}

		{
			KbeePredicateGridColumn<Classifier> metasubtitleColumn = new KbeePredicateGridColumn<>("metadatasubtitle", getLabel("metadatasubtitle"),
					obj ->  getBooleanYesNoText(obj.isMetadataSubtitle(), false));
			metasubtitleColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isMetadataSubtitle(), true));
			metasubtitleColumn.setRowCssClass("centered");
			metasubtitleColumn.setHeaderCssClass("centered");
			metasubtitleColumn.setPreferred(false);
			metasubtitleColumn.setContextKey(this.getName() + metasubtitleColumn.getContextKey());
			this.columns.add(metasubtitleColumn);
		}


		{
			KbeePredicateGridColumn<Classifier> metasubtitleColumn = new KbeePredicateGridColumn<>("defaultstructure", getLabel("defaultstructure"),
					obj ->  getBooleanYesNoText(obj.isDefaultStructure(), false));					
			metasubtitleColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isDefaultStructure(), true));
			metasubtitleColumn.setRowCssClass("centered");
			metasubtitleColumn.setHeaderCssClass("centered");
			metasubtitleColumn.setPreferred(true);
			metasubtitleColumn.setContextKey(this.getName() + metasubtitleColumn.getContextKey());
			metasubtitleColumn.setPreferred(false);
			this.columns.add(metasubtitleColumn);
		}

		
		{						
			KbeePredicateGridColumn<Classifier> typeColumn = new KbeePredicateGridColumn<>("alias", getLabel("alias"),	obj ->  obj.getAlias());
			typeColumn.setContextKey(this.getName() + typeColumn.getContextKey());
			typeColumn.setPreferred(true);
			this.columns.add(typeColumn);
		}

		
		{
			KbeePredicateGridColumn<Classifier> contentTypeColumn = new KbeePredicateGridColumn<>("contenttype", getLabel("contenttype"),
					obj ->  getBooleanYesNoText(obj.isContentType(), false));
			contentTypeColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isContentType(), true));
			contentTypeColumn.setContextKey(this.getName() + contentTypeColumn.getContextKey());
			contentTypeColumn.setRowCssClass("centered");
			contentTypeColumn.setHeaderCssClass("centered");
			this.columns.add(contentTypeColumn);
		}


		{
			KbeePredicateGridColumn<Classifier> semanticColumn = new KbeePredicateGridColumn<>("semantic", getLabel("semantic"),
					obj ->  getBooleanYesNoText(obj.isSemantic(), false));
			semanticColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.isSemantic(), true));
			semanticColumn.setContextKey(this.getName() + semanticColumn.getContextKey());
			semanticColumn.setRowCssClass("centered");
			semanticColumn.setHeaderCssClass("centered");
			semanticColumn.setPreferred(false);
			this.columns.add(semanticColumn);
		}

		{
			KbeePredicateGridColumn<Classifier> userColumn = new KbeePredicateGridColumn<>("user", getLabel("username"),
					obj ->  obj.getLastModifiedUser() != null ? obj.getLastModifiedUser().getFirstLastName() : "err");
			userColumn.setContextKey(this.getName() + userColumn.getContextKey());
			this.columns.add(userColumn);
		}
		

		{
			KbeePredicateGridColumn<Classifier> visibilityColumn = new KbeePredicateGridColumn<>("visibility", getLabel("visibility"),
					obj ->  getVisibilityColumnText(obj,false));
			visibilityColumn.setHtmlValueResolver(obj -> getVisibilityColumnText(obj,true));
			visibilityColumn.setContextKey(this.getName() + visibilityColumn.getContextKey());
			visibilityColumn.setPreferred(false);
			this.columns.add(visibilityColumn);
		}


		{
			KbeePredicateGridColumn<Classifier> idColumn = new KbeePredicateGridColumn<>("id", getLabel("id"),
					obj ->  String.valueOf(obj.getId()));
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			this.columns.add(idColumn);
		}


		{
			KbeePredicateGridColumn<Classifier> minisiteColumn = new KbeePredicateGridColumn<>("minisite", getLabel("hashome"),
					obj ->  getBooleanYesNoText(obj.hasHome(), false));
			minisiteColumn.setHtmlValueResolver(obj -> getBooleanYesNoText(obj.hasHome(), true));
			minisiteColumn.setContextKey(this.getName() + minisiteColumn.getContextKey());
			minisiteColumn.setRowCssClass("centered");
			minisiteColumn.setHeaderCssClass("centered");
			minisiteColumn.setPreferred(false);
			this.columns.add(minisiteColumn);
		}

		
		
		
		{										
			KbeePredicateGridColumn<Classifier> uniqueColumn = new KbeePredicateGridColumn<>("uniquename", getLabel("uniquename"), obj ->  obj.getUniqueName());
			uniqueColumn.setPreferred(false);
			uniqueColumn.setContextKey(this.getName() + uniqueColumn.getContextKey());
			this.columns.add(uniqueColumn);
		}
		
		
		return this.columns;
	}

	
	
	
	
	
	
	private String getLink(DataSet t) {
		return "<a  class=\"btn-link\" href=\""+getServerUrl()+"/model/datasets/"+t.getId().toString()+"\">" + t.getName()
		+ "</a> <span class=\"ago\">"+(t.isAggregation()? (" ("+ new StringResourceModel("built-in", this, null).getObject().toLowerCase()+")</span>"):"");
	}




	private String getVisibilityColumnText(Classifier clasi, boolean html) {
		List<Library> list = getRepository(Library.class).findAll(clasi.getDomain() );

		StringBuilder str = new StringBuilder();

		for (Library ca: list) {
			if ( clasi.isVisible(ca.getKey())) {
				if(str.length()>0) {
					if(html)
						str.append("<span class=\"ago\"> | </span>");
					else
						str.append(" | ");
				}
			str.append(ca.getDisplayName());
			}
		}
		return str.toString();
	}


	//private String getStringResourceValue(String resource) {
	//	return new StringResourceModel(resource, ClassifiersConsole.this, null).getObject();
	//}


	private String getBooleanYesNoText(boolean value, boolean html) {
		String strValue = value ?"yes":"no";
		if(html)
			strValue = new StringResourceModel(strValue+"-html", this, null).getObject();

		return strValue;
	}


	
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	
	@Override
	protected Panel getPanel(IModel<Classifier> model, List<String> snippets) {
		return new ExpandedPanel<Classifier>("editor", this, model, snippets);
	}
	
	
	@Override
	protected Panel getPanel(IModel<Classifier> model) {
		return new ExpandedPanel<Classifier>("editor", this, model);
	}
	
	
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(SidePanelEvent event) {
				// event.getRequestTarget().add(get("header"));
			}
		});

		
		add(new WicketEventListener<ClickEvent<Classifier>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ClickEvent<Classifier> event) {
				setResponsePage(getClassifierPage(event.getModel(), event.getIndex(), false, false));
			}
		});
	}


	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	@Override													
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Classifier> browser) {
		
		
		if (items!=null)
			return items;
		
		items = new ArrayList<ToolbarItem>();

		items.add(new ToolbarAlert(browser, Align.TOP_LEFT) {
			protected IModel<String> getLabel() {
				return new StringResourceModel("readonly", this, null);
			}
			@Override
			public boolean isVisible() {
				if (isRoot())
					return false;
				if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS) 
					return true;
				return false;
			}
		});

		
		
		items.add( new NewButton(browser, Align.TOP_LEFT) {

			@Override
			public boolean isEnabled() {
				if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS && !isRoot()) 
					return false;
				return true;
			}
			
			@Override
			public boolean isVisible() {
				if (getSessionUser().getDomain().getDomainType()==DomainType.EXPRESS && !isRoot()) 
					return false;
				return true;
			}

			
			public void onClick() {
				try {
					Object cla = ServiceLocator.getService(ObjectFactoryService.class).createClassifier();
					((Classifier) cla).setName(new StringResourceModel("newelement", ClassifiersConsole.this, null).getObject());
					((KbeeClassifier) cla).setMultiplicity(Multiplicity.M0N);
					Page page = getClassifierPage(ClassifiersConsole.this.getModel((Classifier) cla), 0, true, true);
					setResponsePage(page);
				}
				catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<Void>(e));
				}
			};
		});
		
			
		InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
		private static final long serialVersionUID = 1L;
		@Override
		public void onClick(AjaxRequestTarget target) {
			InfoDialog infoDialog = (InfoDialog) getInformationModal();
			infoDialog.open(target,() -> {return ClassifiersConsole.this.getName();}, new Model<String>(ClassifiersConsole.this.getDescription()));
		}
		
		@Override
		public boolean isVisible() {
			return true;
		}
	};

	this.items.add(infoButton);
		
		return items;
	}
	
	
	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<User>("audit-trail-modal"));
	}
	

	protected Page getClassifierPage(IModel<Classifier> model, int index, final boolean editon, final boolean is_new) {
		return new ClassifierModelPage(model, editon, is_new);
	}
	
	
	@Override
	protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
		try {		
			if (rowmodel.getObject().getObject() instanceof com.novamens.dom.Object) {
				com.novamens.dom.Object object = (com.novamens.dom.Object) rowmodel.getObject().getObject();
				//if (object.getState()==ObjectState.ARCHIVED)				return "archived-state";
				if (object.getState()==ObjectState.DELETED)					return "deleted-state";	
			}
			
			return null;
				
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}
	
	
	/**
	 * 
	 * @param obj
	 * @return
	 */
	protected String getContentTemplatesStr(Classifier obj) {
		StringBuilder str=new StringBuilder();

		if (!this.getAttContentTemplates().containsKey(obj.getId()))
			return str.toString();
		
		for (String s: this.getAttContentTemplates().get(obj.getId())) {
			if (str.length()>0)
				str.append(" | ");
			str.append(s);
		}
		return str.toString(); 
	}
	
	
	protected String getContentTemplatesHTML(Classifier obj) {
		
		StringBuilder str=new StringBuilder();
		
		if (!this.getAttContentTemplatesHTML().containsKey(obj.getId()))
			return str.toString();
		
		for (String s: this.getAttContentTemplatesHTML().get(obj.getId())) {
			if (str.length()>0)
				str.append(" | ");
			str.append(s);
		}
		return str.toString(); 
	}
	
	
	
    /**
     * 
     * 
     */
    @Override
    protected GridMenu getGridToolbarMenuItem() {
        GridMenu gridToolbarMenuItem = super.getGridToolbarMenuItem();

        gridToolbarMenuItem.addItem((itemId) -> new SeparatorMenuItemPanelV5<File>(itemId) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public String getCssClass() {
                return "divider";
            }

            @Override
            public boolean isVisible() {
                return true;
            }
        });

        MenuItemFactory<?> showDeletedUsersItem = (itemId) ->
                
	        new AjaxCheckMenuItemPanelV5<Object>(itemId) {
	                    private static final long serialVersionUID = 1L;
	                 
					    @Override
	                    public String getLabel() {
	                        return new StringResourceModel("show-deleted", ClassifiersConsole.this, null).getObject();
	                    }
	
	                    @Override
	                    public void onClick(AjaxRequestTarget target) throws Exception {
	                    	ClassifiersConsole.this.setDeletedVisible(!ClassifiersConsole.this.isDeletedVisible());
	                    	setResponsePage(new ClassifiersPage());
						}
	
	                    @Override
	                    public boolean isIconVisible() {
	                        return ClassifiersConsole.this.isDeletedVisible();
	                    }
	
	                    @Override
	                    public String getCssClass() {
	                        if (isIconVisible())
	                            return "label-selected";
	                        else
	                            return "label-no-selected";
	                    }
	
	
	                };
					
        gridToolbarMenuItem.addItem(showDeletedUsersItem);
        return gridToolbarMenuItem;
    }

	
    private boolean is_deleted_visible = false;
    protected void setDeletedVisible(boolean b) {
        this.is_deleted_visible = b;
        setUserPreference("deleted-visible", (b ? "yes" : "no"));
    }

    protected boolean isDeletedVisible() {
        return this.is_deleted_visible;
    }

    
    
    
	Map<Serializable, List<String>> mp = null;
	Map<Serializable, List<String>> mp_link = null;
	

	private Map<Serializable, List<String>> getAttContentTemplates() {
		if (mp==null) {
			generateAttContentTemplates();
		}
		return mp;
	}
	
	private Map<Serializable, List<String>> getAttContentTemplatesHTML() {
		if (mp==null) {
			generateAttContentTemplates();
		}
		return mp_link;
	}
	
	private void generateAttContentTemplates() {
			mp = new HashMap<Serializable, List<String>>();
			mp_link =new HashMap<Serializable, List<String>>();
			
			for (ContentTemplate t: getContentDao().getTemplates(getDomain())) {
				for (ClassifierTemplate c: t.getClassifiers()) {
					Classifier a=c.getClassifier();
					
					if (!mp.containsKey(a.getId()))  {
						mp.put(a.getId(), new ArrayList<String>());
						mp_link.put(a.getId(), new ArrayList<String>());
					}
					mp.get(a.getId()).add(t.getName());
					mp_link.get(a.getId()).add( "<a class=\"btn-link\" href=\""+getLink(t)+"\">"+t.getName()+"</a>");
				}
			}
	}
	
	private String getLink(ContentTemplate t) {
		return getServerUrl()+"/model/contentclass/"+t.getId().toString();
	}

	

}

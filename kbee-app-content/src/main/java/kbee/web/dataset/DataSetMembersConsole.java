package kbee.web.dataset;

import java.io.File;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.kbee.wicket.markup.html.console.browser.*;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.ExternalMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.LabelMember;
import com.novamens.content.model.LabelSet;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.PersonSet;
import com.novamens.content.query.SavedQuery;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.DataSetMemberService;
import com.novamens.content.service.DataSetService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.web.user.markup2.DataSetMemberLabelMenuItemFactory;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Proxy;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.content.query.MemberQuery;
import com.novamens.kbee.content.repository.ActionRuleRepository;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.ImageColumnPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.layout.AbstractLayout;
import com.novamens.kbee.wicket.markup.html.console.layout.LayoutPanel;
import com.novamens.kbee.wicket.markup.html.console.list.ListDisplayMode;
import com.novamens.kbee.wicket.markup.html.console.list.ListPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ApplySavedQueryEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.DownloadMenuItemPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsUserListItemUpdateObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.SavedQueriesSidePanel;
import com.novamens.kbee.wicket.markup.html.console.panel.SolrCursorModel;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeNode;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeProvider;
import com.novamens.kbee.wicket.markup.html.event.GeneralAjaxWicketEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.tree.TreeNodeSelection;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrCursor;
import com.novamens.solr.indexer.query.SolrResultSet;
import com.novamens.util.KbeeRuntimeException;

import com.novamens.wicket.markup.html.actions.AbstractLinkMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.markup.html.repeater.util.NavigationOrder;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.BreadCrumb;

import kbee.util.NumberFormatter;
import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.ClassificableNameColumnPanel;
import kbee.web.console.MenuButtonToolbarItem;
import kbee.web.console.TreeBreadcrumbToolbarItem;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.console.grid.LabelSetPanel;
import kbee.web.console.grid.UserRoleColumn;
import kbee.web.dashboard.LabelPanel;
import kbee.web.datamanagement.TagManagementPage;
import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.LabelEvent;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.DataSetBC;
import kbee.web.nav.MembersBC;
import kbee.web.nav.SettingsBC;
import kbee.web.object.BatchDeletePage;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.panel.ListSimpleItemMainPanel;
import kbee.web.query.DataSetMembersQuery;
import kbee.web.query.DataSetMembersTreeQuery;
import kbee.web.query.DatasetMemebersUserListQuery;


/**
 * 
* <p> DataSetMemebers can be internal ({@link DataSetMemenber})
* 
  * or external ({@link ExternalMemenber}). The externals map to a more complex external entity,
  * such as a site ({@link Site}). The management and synchronization of the external are
  * does outside of the DataSets console. </p>
  *
  * <p> To delete Members:
  * <ul>
  * <li> If it does not have referential relations (classified contents) -> it is deleted directly </li>
  * <li> If it has referential relationships, for example to previous versions of content -> it goes to the deleted state {@code ObjectState.DELETED} </li>
  * <li> If it is Security HandleFcr. The {@link Group} and the associated {@link SecurityRule} will be deleted </li>
  * </ul>
 * </p>
 * 
 */

@SuppressWarnings("serial")
public abstract class DataSetMembersConsole extends AbstractFacetedConsole<DataSetMember> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataSetMembersConsole.class.getName());

	private boolean is_send_email;
	private boolean is_basic;

	final boolean root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_admin = root || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_support = 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean role_dataset_members = role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_information_model = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	private IModel<DataSet> datasetmodel;
	private List<DataSet> datasetlist;
	private Map<String, Long> references = new HashMap<String, Long>();

	private List<IModel<LabelMember>> labels = null;

	private List<GridColumn<SearchResult, String>> columns;

	private boolean is_deleted_visible = false;

	private List<ToolbarItem> selection_toolbar;
	private List<ToolbarItem> items;
	private DataSetTreeProvider treeprovider = null;
	
    final boolean IS_SPIN = true;
    final boolean IS_UPDATE_ONCOMPLETE = true;
    
    private transient List<DataSet> homesets =null;

	public class DeleteableModel implements IModel<DataSetMember> {
        private Serializable id;
        private Class<?> clazz;
        private String name;
        private IModel<DataSet> datasetmodel;
        private DataSetMember member = null;
        
        public DeleteableModel(DataSetMember member) {
            id = member.getId();
            name = member.getStrValue();
            datasetmodel = new ObjectModel<DataSet>(member.getDataSet());
            clazz = Proxy.getClass(member);
        }
        @SuppressWarnings("deprecation")
		public DataSetMember getObject() {
            if (member == null) {
                try {
                    member = (DataSetMember) clazz.newInstance();
                } catch (Exception e) {
                    throw new KbeeRuntimeException(e);
                }
                member.setId(id);
                member.setStrValue(name);
                member.setDataSet(datasetmodel.getObject());
            }
            return member;
        }
        public void setObject(DataSetMember member) {
        }
        public void detach() {
            member = null;
            datasetmodel.detach();
        }
    }

	/**
	 * 
	 * 
	 * @param model
	 * @param query
	 */
	public DataSetMembersConsole(IModel<DataSet> model, Query query) {
		super("dataset-" + model.getObject().getName().toLowerCase(), query);
		setDataSet(model);
		setOutputMarkupId(true);
		setListBrowser(true);
		
		this.setDeletedVisible(getUserPreference("deleted-visible", "no").equals("yes"));
	}

	
	@Override
	protected String getIcon(IModel<DataSetMember> model) {
		return null;
	}	
	
	
    public List<DataSet> getDataSets() {
        if (this.datasetlist != null)
            return this.datasetlist;
        this.datasetlist = new ArrayList<DataSet>();
        for (DataSet dataset : getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain())) {
            if (	dataset.getDataSetType() 	== DataSetType.STRING 		||
                    dataset.getDataSetType() 	== DataSetType.EXTERNAL 	||
                    dataset.getDataSetType() 	== DataSetType.ENTITY 		||
                    dataset.getDataSetType() 	== DataSetType.LABEL 		||
                    dataset.getDataSetType() 	== DataSetType.PEOPLE)
            	
                this.datasetlist.add(dataset);
            
        }
        return this.datasetlist;
    }

	protected WebMarkupContainer getMoreInfoPanel(IModel<DataSetMember> modelObject) {
		try {

			@SuppressWarnings("unchecked")
			ListPanel<DataSetMember> panel = (ListPanel<DataSetMember>) getBrowser().getPanel(ListPanel.class);
			
			if (panel==null) 
				return new InvisiblePanel("more-info-container");
			
			ListDisplayMode mode=panel.getListDisplayMode();
			
			if (mode.isCompact())
				return new InvisiblePanel("more-info-container");
			
			
			//String note = modelObject.getObject().getService(WorkflowService.class).getTaskComment();
			//if (note==null)
				return new InvisiblePanel("more-info-container");
			//note=note.replaceAll(TO_ESC,"<br />");
		//return new LabelPanel("more-info-container", getSnippet(note));
				
		}  catch (Exception e) {
			logger.error(e);
			return new LabelPanel("more-info-container",  new Model<String>(e.getClass().getSimpleName()));
		}
	}

	
    @Override
	protected Panel getItemListPanel(IModel<DataSetMember> model , int index) {
    											
		ListSimpleItemMainPanel<DataSetMember> ls= new ListSimpleItemMainPanel<DataSetMember>("item", model, index,false) {

			private static final long serialVersionUID = 1L;
			protected void onClick() {
					fireScanAll(new ClickEvent<DataSetMember>(null, getModel(), 0));
			}
			
			@Override
			protected WebMarkupContainer getItemTags(IModel<DataSetMember> modelObject) {
				return  DataSetMembersConsole.this.getItemTags(modelObject);
			}
			
			protected WebMarkupContainer getMoreInfoPanel(IModel<DataSetMember> modelObject) {
				return  DataSetMembersConsole.this.getMoreInfoPanel(modelObject);
			}
			
			protected IModel<String> getItemLabel(IModel<DataSetMember> modelObject) {
				return  new Model<String>(modelObject.getObject().getDisplayName());
			}

			protected IModel<String> getItemLabelMeta(IModel<DataSetMember> modelObject) {
				return DataSetMembersConsole.this.getItemLabelMeta(modelObject);
			}
		};
		return ls;
    }
    
    
	/**
	 * @param modelObject
	 * @return
	 */
	protected IModel<String> getItemLabelMeta(IModel<DataSetMember> modelObject) {
		
		@SuppressWarnings("unchecked")
		ListPanel<DataSetMember> panel = (ListPanel<DataSetMember>) getBrowser().getPanel(ListPanel.class);
		
		if (panel==null) 
			return null;
		
		ListDisplayMode mode=panel.getListDisplayMode();
		
		if (mode.isCompact())
			return null;
		
		StringBuilder str = new StringBuilder();

		
		try {
			ExtractionRule rule =  modelObject.getObject().getDataSet().getSublineRule();
			if (rule!=null) {
				String label = (String)rule.extract((DataSetMember)modelObject.getObject());
				str.append(label);
			}
		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		
		if (str.length()>0)
			str.append(". ");
		
		/**
		Map<String, List<String>> map = modelObject.getObject().getClassificationAsMapString();
		map.forEach((k,v) -> {
			str.append(k+":");
			v.forEach(x -> str.append(x+" "));
		});
		
		//str.append(modelObject.getObject().getClassificationAsString());
		 * **
		 * */
		
		
		if (modelObject.getObject().getLastModifiedUser()!=null) {
			str.append(modelObject.getObject().getLastModifiedUser().getDisplayName() + ". ");
		}
		
		str.append(modelObject.getObject().getLastModifiedOffsetDateTimeColloquial());
		
		return new Model<String>(str.toString());
	}

	
    
    /**
     * 
     * 
     * @param modelObject
     * @return
     */
    protected WebMarkupContainer getItemTags(IModel<DataSetMember> modelObject) {
		try {
			return new LabelSetPanel<DataSetMember>("labels", modelObject, false, true, false);
		}
		catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("labels", e);
		}
	}
    
    
    public void setDataSet(IModel<DataSet> model) {
        this.datasetmodel = model;
    }

    public void setDataSet(DataSet dataSet) {
        this.datasetmodel = new ObjectModel<DataSet>(dataSet);
    }

    public DataSet getDataSet() {
        return datasetmodel.getObject();
    }

    @Override
    public String getDownloadFileName() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        String name = getDomain().getName().replace(" ", "").toLowerCase() +
                "-" + getDataSet().getName().toLowerCase().replace(" ", "-") +
                "-" + df.format(OffsetDateTime.now());
        return name;
    }

//    public Query getBuiltInQuery(DataSetMember member, Long builtin_id) {
//        DataSet da = null;
//        for (DataSet d : getDataSets()) {
//            if (((Long) d.getId()).equals(builtin_id)) {
//                da = d;
//                return new AggregationQuery(getQueryIndex(), da, member);
//            }
//        }
//        return null;
//    }

    
    /***
     * 
     * 
     */
	@Override
    public List<GridColumn<SearchResult, String>> getColumns() {

        if (this.columns != null)
            return this.columns;

        this.columns = new ArrayList<GridColumn<SearchResult, String>>();

        this.columns.add(new ObjectStatusColumn<Person>("iconstatus", getName(), getLabel("datasetconsole.column.status")));


        if (getDataSet() instanceof LabelSet) {
            this.columns.add(new GridColumn<SearchResult, String>("icon", getLabel("datasetconsole.column.icon")) {
                @Override
                public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {

                    if ((resultmodel.getObject() == null || resultmodel.getObject().getObject() == null) || (!(resultmodel.getObject().getObject() instanceof LabelMember))) {
                        cellItem.add(new Label(componentId, "err"));
                        return;
                    }
                    try {
                        LabelMember m = (LabelMember) resultmodel.getObject().getObject();
                        ImageColumnPanel<LabelMember> icp = new ImageColumnPanel<LabelMember>(componentId, new ObjectModel<LabelMember>(m)) {
                            @Override
                            protected Image getImage(String id) {
                                return null;
                            }
                            protected IModel<String> getAnchorTitle() {
                                return new Model<String>(getModel().getObject().getLabelColor().getLabel(getSessionUser().getLocale()));
                            }
                            protected String getCss() {
                                return "iconcss far fa-tag " + getModel().getObject().getLabelColor().getKey();
                            }
                        };
                        cellItem.add(icp);
                    } catch (Exception e) {
                        logger.error(e);
                        cellItem.add(new Label(componentId, e.getClass().getName()));
                    }
                }
                @Override
                public boolean isExportable() {
                    return false;
                }
                @Override
                public boolean isPreferred() {
                    return true;
                }
                @Override
                public boolean isExpanded() {
                    return false;
                }
                @Override
                public int getDefaultWidth() {
                    return 60;
                }
                @Override
                public String getCssClass() {
                    return "col col-xs-1 col-md-1 col-lg-1";
                }
                @Override
                protected String getContextKey() {
                    return DataSetMembersConsole.this.getName() + super.getContextKey();
                }
            });
        }


        this.columns.add(new GridColumn<SearchResult, String>("mylists", getLabel("mylists")) {
            @Override
            public String getCssClass() {
                return super.getCssClass() + " mylist";
            }
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    List<UserList> list = ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(DataSetMembersConsole.this.getName(), (DataSetMember) object.getObject());
                    if (list == null)
                        return new Model<String>("");
                    StringBuilder str = new StringBuilder();
                    for (UserList u : list) {
                        if (str.length() > 0)
                            str.append(", ");
                        str.append(u.getTitle());
                    }
                    return new Model<String>(str.toString());

                } catch (Exception e) {
                    logger.error(e, getSessionUser().getUserName());
                    return new Model<String>(e.getClass().getSimpleName());
                }
            }
            @Override
            protected String getContextKey() {
                return DataSetMembersConsole.this.getName() + super.getContextKey();
            }
            @Override
            public boolean isPreferred() {
                return false;
            }
        });


        this.columns.add(new GridColumn<SearchResult, String>("title", getLabel("datasetconsole.column.title"), "title_sort") {
            @Override
            public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
                try {
                	if (resultmodel.getObject() == null)
                        cellItem.add(new Label(componentId, "err"));
                	Object object = resultmodel.getObject().getObject();
                    if (object != null) {
                    	if (object  instanceof PersonMember) {
	                        cellItem.add(new Label(componentId, ((PersonMember) object).getLastFirstName()));
                    	}
                    	else {
	                        IModel<DataSetMember> objectmodel = getModel((DataSetMember) object);
	                        cellItem.add(new Label(componentId, objectmodel.getObject().getDisplayName()));
                    	}
                    } 
                    else {
                        cellItem.add(new Label(componentId, "err"));
                    }
                } 
                catch (Exception e) {
                    logger.error(e);
                    cellItem.add(new Label(componentId, e.getClass().getName()));
                }
            }
        	@Override
            public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
                try {
                	if (resultmodel.getObject() == null)
                        cellItem.add(new Label(componentId, "err"));
                    Object object = resultmodel.getObject().getObject();
                    if (object != null) {
                        IModel<DataSetMember> objectmodel = getModel((DataSetMember) object);
                        cellItem.add(new ClassificableNameColumnPanel<DataSetMember>(componentId, objectmodel) {
                            @Override
                            protected String getCss() {
                                return "cell-label btn-link";
                            }
                            @Override
                            protected String getDisplayProperty() {
                            	if (getModel().getObject() instanceof PersonMember) {
                            		return "lastFirstName";
                            	}
                            	else
                            		return "strValue";
                            }
                        });
                    } 
                    else {
                    	cellItem.add(new Label(componentId, "err"));
                    }
                } 
                catch (Exception e) {
                    logger.error(e);
                    cellItem.add(new Label(componentId, e.getClass().getName()));
                }
        	}
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                String strValue = ((DataSetMember) object.getObject()).getStrValue();
                return new Model<>(strValue);
            }
            @Override
            public String getCssClass() {
                return "col title col-xs-1 col-md-1 col-lg-1";
            }
            @Override
            protected String getContextKey() {
                return DataSetMembersConsole.this.getName() + super.getContextKey();
            }
            @Override
            public int getDefaultWidth() {
                return 380;
            }
        });
        

        
        if (getDataSet() instanceof PersonSet) {

        	this.columns.add(new GridColumn<SearchResult, String>("user", getLabel("user"), null) {
	        	@Override
	            public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
	                try {
	                	if (resultmodel.getObject() == null)
	                        cellItem.add(new Label(componentId, "err"));
                    		Object object = resultmodel.getObject().getObject();
	                    if (object == null) { 
	                    	cellItem.add(new Label(componentId, "err"));
	                    	return;
	                    }
	                    if (object != null) {
	                    	UserProfile up = ((PersonMember) object).getProfile(UserProfile.class);
	                    	if (up!=null) {
	                    		User user = up.getUser();
	                    		if (user!=null) {
	                    			String url = "<a clas=\"btn-link\" target=\"_blank\" href=\""+ getServerUrl()+"/security/users/"+ user.getId().toString() +"\">" + user.getUserName() + "</a>";
	                    			cellItem.add((new Label(componentId, url)).setEscapeModelStrings(false));
	                    			return;
	                    		}
	                    	}
	                    }
	            		cellItem.add( new Label(componentId, "")); 
	                } 
	                catch (Exception e) {
	                    logger.error(e);
	                    cellItem.add(new Label(componentId, e.getClass().getName()));
	                }
	            }
	        	
	        	protected IModel<String> getExpandedLabelModel(SearchResult object) {
	        		return getCellAsString(object);
	        	}
	        	
	        	public IModel<String> getCellAsString(SearchResult result) {
	                if (result==null)
	                	return new Model<String>("null");
	                Object object = result.getObject();
	                if (object==null)
	                	return new Model<String>("null");
	                UserProfile up = ((PersonMember) object).getProfile(UserProfile.class);
	                if (up!=null) { 
                		User user = up.getUser();
                		if (user!=null) {
                			return new Model<String>(user.getUserName());
                		}
	                }
	                return new Model<String>("null");
	            }
	        	
	            @Override
	            protected String getContextKey() {
	                return DataSetMembersConsole.this.getName() + super.getContextKey();
	            }
	        });
        }
        

        List<DataSet> aggregations = getDataSet().getService(DataSetService.class).getAggregations();
        
        if (aggregations != null) {
            for (DataSet buildtin : aggregations) {
            	final IModel<DataSet> buildtinmodel = new ObjectModel<DataSet>(buildtin);
            	final Long builtin_id = (Long) buildtin.getId();
            	
//            	KbeePredicateGridColumn<DataSetMember> countColumn = new KbeePredicateGridColumn<>(
//            		"builtincount" + builtin_id.toString(), 
//               		getLabel("datasetconsole.column.builtincount", buildtin.getName()),
//               		//       		new Model<String>(builtin.getName() + "<span class=\"ago\"> (" + la + ")</span>"), 
//            		obj -> NumberFormatter.formatNumber(getTotalBuiltIn(obj, builtin_id)));
//            	countColumn.setContextKey(this.getName() + countColumn.getContextKey());
//                countColumn.setCssValueResolver(obj -> getNumberTotalBuiltInClass(obj, builtin_id));
//                countColumn.setPreferred(true);
//                countColumn.setHeaderCssClass("centered");
//                countColumn.setLabelCss("number-md");
                
                this.columns.add(new GridColumn<SearchResult, String>("builtincount" + builtin_id.toString(), 
                   		getLabel("datasetconsole.column.builtincount", buildtin.getName())) {
                    @Override
                    protected IModel<String> getLabelModel(SearchResult object) {
                    	DataSet aggregationSet = buildtinmodel.getObject();
                    	List<DataSetMember> values = ((DataSetMember) object.getObject())
                    		.getService(DataSetMemberService.class).getAggregations(aggregationSet);
//                    	StringBuffer label = new StringBuffer();
//                    	for (DataSetMember value : values) {
//                    		label.append(value.getDisplayName()+", ");
//                    	}
                    	buildtinmodel.detach();
                    	return new Model<String>(String.valueOf(values.size()));
                    }
                    @Override
                    protected String getContextKey() {
                        return DataSetMembersConsole.this.getName() + super.getContextKey();
                    }
                    @Override
                    public String getHeaderCssClass() {
                        return "centered";
                    }
                    @Override
                	public String getCssClass(SearchResult object) {
                    	return "col number-md info";
                    }
                    @Override
                    public String getCellContainerCss() {
                        return "number-md";
                    }
                    @Override
                    public boolean isPreferred() {
                        return true;
                    }
                });
                
//                this.columns.add(countColumn);
                
                this.columns.add(new GridColumn<SearchResult, String>("builtinelements" + builtin_id.toString(), 
                   		getLabel("datasetconsole.column.builtinelements", buildtin.getName())) {
                    @Override
                    protected IModel<String> getLabelModel(SearchResult object) {
                    	DataSet aggregationSet = buildtinmodel.getObject();
                    	List<DataSetMember> values = ((DataSetMember) object.getObject())
                    		.getService(DataSetMemberService.class).getAggregations(aggregationSet);
                    	StringBuffer label = new StringBuffer();
                    	for (DataSetMember value : values) {
                    		if (label.length()>0) {
                    			label.append(" | ");
                    		}
                    		label.append(value.getDisplayName());
                    	}
                    	buildtinmodel.detach();
                    	return new Model<String>(label.toString());
                    }
                    @Override
                    protected String getContextKey() {
                        return DataSetMembersConsole.this.getName() + super.getContextKey();
                    }
                    @Override
                    public boolean isPreferred() {
                        return false;
                    }
                });
            }
        }

        this.columns.add(new LastModifiedColumn<DataSetMember>("date", getLabel("datasetconsole.column.modified"), "modified") {
        	@Override
        	protected String getContextKey() {
        		return DataSetMembersConsole.this.getName() + super.getContextKey();
        	}
        });

        this.columns.add(new GridColumn<SearchResult, String>("modifieduser", getLabel("datasetconsole.column.modifiedby")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    return new Model<String>(String.valueOf(((DataSetMember) object.getObject())
                    	.getLastModifiedUser()
                    	.getFirstLastName()));
                } 
                catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getSimpleName());
                }
            }
            @Override
            protected String getContextKey() {
                return DataSetMembersConsole.this.getName() + super.getContextKey();
            }
            @Override
            public boolean isPreferred() {
                return false;
            }
        });


        for (ModelElementTemplate template : getDataSet().getStructure()) {
            if (template != null) {
                if (template.getElement() != null && template.getElement() instanceof Classifier) {
                    if (((Classifier) template.getElement()).getState() == ObjectState.ENABLED) {
                        ClassifierColumn<DataSetMember> cc = new ClassifierColumn<DataSetMember>(new ObjectModel<Classifier>((Classifier) template.getElement()), this.getName());
                        if (((Classifier) template.getElement()).isDefaultStructure())
                            cc.setPreferred(true);
                        else if (((Classifier) template.getElement()).isDefaultGridColumn())
                            cc.setPreferred(true);
                        else
                            cc.setPreferred(false);

                        this.columns.add(cc);
                    }
                } else {
                    if (template.getElement() != null && template.getElement() instanceof Attribute) {
                        if (((Attribute) template.getElement()).getState() == ObjectState.ENABLED) {
                            GridColumn<SearchResult, String> gc = new GridColumn<SearchResult, String>(String.valueOf(template.getElement().getId()), new Model<String>(template.getElement().getName())) {
                                @Override
                                protected IModel<String> getLabelModel(SearchResult object) {
                                    StringBuilder str = new StringBuilder();
                                    try {
                                        DataSetMember member = (DataSetMember) object.getObject();
                                        for (ModelElementTemplate template : member.getDataSet().getStructure()) {
                                            if (template != null && String.valueOf(template.getElement().getId()).equals(this.getId())) {
                                                for (String s : member.getAttributeValues((Attribute) template.getElement())) {
                                                    if (str.length() > 0)
                                                        str.append(", ");
                                                    str.append(s);
                                                }
                                                break;
                                            }
                                        }
                                    } catch (Exception e) {
                                        logger.error(e);
                                        str.append(e.getClass().getName() + " | " + e.getMessage());
                                    }
                                    return new Model<String>(str.toString());
                                }

                                @Override
                                protected String getContextKey() {
                                    return DataSetMembersConsole.this.getName() + super.getContextKey();
                                }
                            };

                            gc.setPreferred(false);
                            this.columns.add(gc);
                        }
                    }
                }
            }
        }


        // -------------------------------------

        if (isRoot() || isAdmin()) {
            this.columns.add(new GridColumn<SearchResult, String>("references", new Model<String>(getLabel("datasetconsole.column.references").getObject() + " <span class=\"only-root\"> (admin)</span>")) {
                @Override
                protected IModel<String> getLabelModel(SearchResult object) {
                    try {
                    	Long ref = getReferences((DataSetMember) object.getObject());
                    	String sr = NumberFormatter.formatNumber(ref, getSessionUser().getLocale());
                    	return new Model<String>(sr);
                    } 
                    catch (Exception e) {
                        logger.error(e);
                        return new Model<String>(e.getClass().getSimpleName() + " | " + e.getMessage());
                    }
                }
                @Override
                protected String getContextKey() {
                    return DataSetMembersConsole.this.getName() + super.getContextKey();
                }
                @Override
                public String getCssClass() {
                    return "col col-xs-1 col-md-1 col-lg-1 ui-resizable";
                }
                protected String getLabelCss(IModel<SearchResult> model) {
                    Long ref = getReferences((DataSetMember) model.getObject().getObject());
                    return ref > 0 ? "number-mdx info" : "number-mdx";
                }
                @Override
                protected String getLabelCss() {
                    return "number-mdx";
                }
                @Override
                public String getHeaderCssClass() {
                    return super.getHeaderCssClass() + " centered";
                }
                @Override
                public boolean isPreferred() {
                    return false;
                }
            });
        }
        
        if (getDataSetModel().getObject() instanceof PersonSet) {
            this.columns.add(new GridColumn<SearchResult, String>("email", getLabel("datasetconsole.column.email")) {
                @Override
                protected IModel<String> getLabelModel(SearchResult object) {
                    try {
                        DataSetMember member = (DataSetMember) object.getObject();
                        if (member instanceof PersonMember) {
                        	return new Model<String>(((PersonMember) member).getEmail());
                        }
                        return new Model<String>("");
                    } 
                    catch (Exception e) {
                        logger.error(e);
                        return new Model<String>(e.getClass().getName() + "  " + e.getMessage());
                    }
                }
                @Override
                protected String getContextKey() {
                    return DataSetMembersConsole.this.getName() + super.getContextKey();
                }
                @Override
                public boolean isPreferred() {
                    return true;
                }
            });
            
            
            this.columns.add(new GridColumn<SearchResult, String>("lastname", getLabel("lastname"), "lastname_sort") {
                @Override
                protected IModel<String> getLabelModel(SearchResult object) {
                    try {
                        DataSetMember member = (DataSetMember) object.getObject();
                        if (member instanceof PersonMember) {
                        	return new Model<String>(((PersonMember) member).getLastName());
                        }
                        return new Model<String>("");
                    } 
                    catch (Exception e) {
                        logger.error(e);
                        return new Model<String>(e.getClass().getName() + "  " + e.getMessage());
                    }
                }
                @Override
                protected String getContextKey() {
                    return DataSetMembersConsole.this.getName() + super.getContextKey();
                }
                @Override
                public boolean isPreferred() {
                    return true;
                }
            });
            

            this.columns.add(new GridColumn<SearchResult, String>("firstname", getLabel("firstname")) {
                @Override
                protected IModel<String> getLabelModel(SearchResult object) {
                    try {
                        DataSetMember member = (DataSetMember) object.getObject();
                        if (member instanceof PersonMember) {
                        	return new Model<String>(((PersonMember) member).getFirstName());
                        }
                        return new Model<String>("");
                    } 
                    catch (Exception e) {
                        logger.error(e);
                        return new Model<String>(e.getClass().getName() + "  " + e.getMessage());
                    }
                }
                @Override
                protected String getContextKey() {
                    return DataSetMembersConsole.this.getName() + super.getContextKey();
                }
                @Override
                public boolean isPreferred() {
                    return true;
                }
            });
        }    

        if (getDataSetModel().getObject().isExternal()) {
            this.columns.add(new GridColumn<SearchResult, String>("externaloid", getLabel("datasetconsole.column.externaloid")) {
                @Override
                protected IModel<String> getLabelModel(SearchResult object) {
                    try {
                        if (((DataSetMember) object.getObject()).getDataSet().isExternal()) {
                            DataSetMember member = (DataSetMember) object.getObject();
                            if (member instanceof ExternalMember) {
                                if (((ExternalMember) member).getExternalId() != null)
                                    return new Model<String>(((ExternalMember) member).getExternalId());
                            }
                        }
                        return new Model<String>("");
                    } 
                    catch (Exception e) {
                        logger.error(e);
                        return new Model<String>(e.getClass().getName() + "  " + e.getMessage());
                    }
                }
                @Override
                protected String getContextKey() {
                    return DataSetMembersConsole.this.getName() + super.getContextKey();
                }
                @Override
                public boolean isPreferred() {
                    return false;
                }
            });

            this.columns.add(new GridColumn<SearchResult, String>("externalurl", getLabel("datasetconsole.column.externalurl")) {
                @Override
                protected IModel<String> getLabelModel(SearchResult object) {
                    try {
                        if (((DataSetMember) object.getObject()).getDataSet().isExternal()) {
                            DataSetMember member = (DataSetMember) object.getObject();
                            if (member instanceof ExternalMember) {
                                return new Model<String>(((ExternalMember) member).getExternalUrl());
                            }
                        }
                        return new Model<String>("");
                    } 
                    catch (Exception e) {
                        logger.error(e);
                        return new Model<String>(e.getClass().getName() + "  " + e.getMessage());
                    }

                }
                @Override
                protected String getContextKey() {
                    return DataSetMembersConsole.this.getName() + super.getContextKey();
                }
                @Override
                public boolean isPreferred() {
                    return false;
                }
            });
        }

        if (getDataSet() instanceof EntitySet) {
           
        	for (Role role : getContentSecurityDao().getRolesByEntitySet((EntitySet) getDataSet())) {
                this.columns.add(new UserRoleColumn("role" + role.getId().toString(), 
                	
                		new ObjectModel<Role>(role), 
                		new Model<String>(role.getDisplayName() + " <span class=\"ago\">( " + new StringResourceModel("role", this, null).getString() + " )</span>")) {
                    
                	@Override
                    protected String getContextKey() {
                        return DataSetMembersConsole.this.getName() + super.getContextKey();
                    }

                    @Override
                    public boolean isPreferred() {
                        return false;
                    }

                    @Override
                    public int getDefaultWidth() {
                        return 380;
                    }
                });
        	}
       }
        
		{
			if (getDataSet().getDataSetType()==DataSetType.ENTITY) {
				KbeePredicateGridColumn<DataSetMember> elementsColumn = new KbeePredicateGridColumn<>("recurrent-rules", 
						getLabel("recurrent-rules"),obj ->  getRules((EntityMember) obj, false)
						);  // String.format("%10d", getTotalEntityRules(obj)
				elementsColumn.setHtmlValueResolver(obj -> getRules((EntityMember) obj, true));
				elementsColumn.setContextKey(this.getName() + elementsColumn.getContextKey());
				this.columns.add(elementsColumn);
			}
		}

        
        // -------------------------------------
        // id
        //
        this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("datasetconsole.column.id")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    return new Model<String>(String.valueOf(((DataSetMember) object.getObject()).getId()));
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName() + "  " + e.getMessage());
                }

            }

            @Override
            protected String getContextKey() {
                return DataSetMembersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }

        });


        // -------------------------------------
        // external id
        //
        this.columns.add(new GridColumn<SearchResult, String>("externalid", getLabel("externalid")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    String id = ((DataSetMember) object.getObject()).getExternalId();
                    if (id == null) id = "";
                    return new Model<String>(id);
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getName() + "  " + e.getMessage());
                }
            }

            @Override
            protected String getContextKey() {
                return DataSetMembersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });

        return this.columns;
    }
    
    @Override
    public Query newQuery() {
		if ("tree".equals(getBrowserType()) || "treelist".equals(getBrowserType())) {
			return setUserPreference(new DataSetMembersTreeQuery(getQueryIndex(), getDataSet(), isDeletedVisible()));
		}
		else {
			return setUserPreference(new DataSetMembersQuery(getQueryIndex(), getDataSet(), isDeletedVisible()));
		}
    }
    
    @Override
	public boolean isTreeBrowser() {
		return getDataSet().isHierachical();
	}

    @Override
    public void onInitialize() {
        super.onInitialize();
        try {
        	if (getQuery().getParameters().get("text")!=null) {
    			setBrowserType("grid");
    			String text = (String)getQuery().getParameters().get("text");
    			setQuery(newQuery());
    			getQuery().getParameters().put("text", text);
        		addOrReplace(newGridBrowser());
        	}
            this.is_basic = getDomain().getDomainType() == DomainType.EXPRESS;
            this.is_send_email = (root || role_admin) || getPerson().getProfile(UserProfile.class).isSendFilesEmail();
        } 
        catch (Exception e) {
            logger.error(e);
            this.is_send_email = true;
            this.is_basic = false;
        }
    }
    
    @Override
    public void onDetach() {
        this.datasetmodel.detach();
        this.datasetlist = null;
        this.references.clear();
        this.columns = null;
        if (this.items != null) {
            for (ToolbarItem item : items) {
                item.detach();
            }
        }
        if (this.labels != null) {
            for (IModel<LabelMember> m : this.labels)
                m.detach();
        }
        if (this.selection_toolbar != null) {
            for (ToolbarItem item : selection_toolbar) {
                item.detach();
            }
        }
        super.onDetach();
    }

    protected BreadCrumb getBreadCrumb() {
        return new BreadCrumb(new DataSetBC(getDataSet()));
    }

    @Override
    protected Panel getMenu(IModel<DataSetMember> model) {

        ContextMenuPanel<DataSetMember> menu = new ContextMenuPanel<DataSetMember>(model);

        try {
            menu.addItem(id ->
            	new AjaxMenuItemPanelV5<DataSetMember>(id) {
            		public void onClick(AjaxRequestTarget target) {
            			MemberPage page = (MemberPage) DataSetMembersConsole.this.getPage(getModel(), DataSetMembersConsole.this.getIndex(getModel().getObject()));
            			page.setEditon(false);
                        page.setNew(false);
                        setResponsePage(page);
                    }
            		@Override
                    public String getLabel() {
            			return getLabelString("datasetconsole.contextmenu.open");
                    }
            });
            
            if (hasHome(model.getObject())) {
	            menu.addItem(id ->
		        	new AjaxMenuItemPanelV5<DataSetMember>(id) {
		        		public void onClick(AjaxRequestTarget target) {
		                    setResponsePage(new RedirectPage(getUrlHome(getModel().getObject())));
		                 }
		        		@Override
		                public String getLabel() {
		        			return getLabelString("datasetconsole.contextmenu.home");
		                }
	        	});
            }

            menu.addItem(id ->
            	new SubMenuAjaxUserListItemPanel<DataSetMember>(id, model, DataSetMembersConsole.this.getName(), UserListItem.NEWEST)
            );

            menu.addItem(id ->
            	new AjaxMenuItemPanelV5<DataSetMember>(id) {
            		public void onClick(AjaxRequestTarget target) {
            			setResponsePage(DataSetMembersConsole.this.getPage(getModel(), DataSetMembersConsole.this.getIndex(getModel().getObject())));
                    }
            		@Override
            		public String getLabel() {
            			return getLabelString("datasetconsole.contextmenu.edit");
            		}
            	}
            );

            menu.addItem(id ->
            	new AjaxMenuItemPanelV5<DataSetMember>(id) {
            		@SuppressWarnings("unchecked")
                    public void onClick(AjaxRequestTarget target) {
            			Modal modal = DataSetMembersConsole.this.getAuditTrailModal();
            			((ObjectAuditModal<DataSetMember>) modal).open(target, getModel(), true);
            		}
            		@Override
            		public String getLabel() {
            			return getLabelString("datasetconsole.contextmenu.audittrail");
            		}
            	}
            );

            menu.addItem(new MenuItemFactory<DataSetMember>() {
                @Override
                public AbstractMenuItemPanelV5<DataSetMember> getItem(String id) {
                    SubmenuAjaxItemPanelV5<DataSetMember> submenu = new SubmenuAjaxItemPanelV5<DataSetMember>(id, model) {
                        @Override
                        public String getLabel() {
                            return  DataSetMembersConsole.this.getLabel("labels").getObject();
                        }
                    };
                    for (IModel<LabelMember> label : getLabelMembers()) {
                        submenu.addItem(new DataSetMemberLabelMenuItemFactory(label, model) {
                            @Override
                            public void onUpdate(AjaxRequestTarget target) {
                                fire(new LabelEvent(target));
                            }
                        });
                    }
                    return submenu;
                }
            });

            menu.addItem(id ->
            	new SeparatorMenuItemPanelV5<DataSetMember>(id) {
            		@Override
            		public String getCssClass() {
            			return "divider";
            		}
                    @Override
                    public boolean isVisible() {
                        if (getDataSet().isExternal())
                            return false;
                        if (isSupportUser() && !isRoot())
                            return false;
                        if (getDataSet().isReadonly() && !isRoot())
                            return false;
                       if (getModel().getObject().getState().equals(ObjectState.DELETED))
                    	   return false;
        				if (role_dataset_members)
        					return true;
        				if (!isWriteable(getModelObject()))
        					return false;
        				return true;
                    }
            	}
            );

            menu.addItem(new MenuItemFactory<DataSetMember>() {
                @Override
                public AbstractMenuItemPanelV5<DataSetMember> getItem(String id) {
                    return new AjaxMenuItemPanelV5<DataSetMember>(id) {
                        public void onClick(AjaxRequestTarget target) {
                            IModel<String> msg;
                            msg = getConsoleLabel("datasetconsole.deleteconfirmation.message", getModel().getObject().getDisplayName());
                            getConfirmationDialog().open(target, msg, Dialog.Delete, new Dialog.Handler() {
                                @Override
                                public void onClick(AjaxRequestTarget target, Button button) {
                                    if (button.key().equals(Dialog.Delete.key())) {
                                        executeDelete(target);
                                        DataSetMembersConsole.this.refresh(target);
                                    }
                                }
                            });
                            DataSetMembersConsole.this.refresh(target);
                        }

                        @Override
                        public String getLabel() {
                            return getConsoleLabel("datasetconsole.contextmenu.delete").getObject();
                        }
                        @Override
                        public boolean isVisible() {
                            if (getDataSet().isExternal())
                                return false;
                            if (isSupportUser() && !isRoot())
                                return false;
                            if (getDataSet().isReadonly() && !isRoot())
                                return false;
                           if (getModel().getObject().getState().equals(ObjectState.DELETED))
                        	   return false;
            				if (role_dataset_members)
            					return true;
            				if (!isWriteable(getModelObject()))
            					return false;
            				return true;
                        }
                        @Override
                        public boolean isEnabled() {
                            if (getDataSet().isExternal())
                                return false;
                            if (isSupportUser() && !isRoot())
                                return false;
                            if (getDataSet().isReadonly() && !isRoot())
                                return false;
                            if (role_dataset_members)
                                return true;
            				if (!isWriteable(getModelObject()))
            					return false;
                            return true;
                        }
                        protected void executeDelete(AjaxRequestTarget target) {
                        	IModel<DataSetMember> model = new ObjectModel<DataSetMember>(getModel().getObject());
                            try {
                               model.getObject().getService(DOMObjectService.class).delete();
                            } 
                            catch (Exception e1) {
                                try {
                                    logger.error(e1);
                                    model.detach();
                                    model.getObject().getService(DOMObjectService.class).markAsDeleted();
                                } 
                                catch (Exception e2) {
                                    logger.error(e2);
                                }
                            }
                        }
                    };
                }
            });

            menu.addItem(new MenuItemFactory<DataSetMember>() {
                @Override
                public AbstractMenuItemPanelV5<DataSetMember> getItem(String id) {
                    return new AjaxMenuItemPanelV5<DataSetMember>(id) {
                        public void onClick(AjaxRequestTarget target) {
                            getModel().getObject().getService(DOMObjectService.class).restore();
                            DataSetMembersConsole.this.refresh(target);
                        }
                        @Override
                        public String getLabel() {
                            return getConsoleLabel("datasetconsole.contextmenu.restore").getObject();
                        }
                        @Override
                        public boolean isVisible() {
                            return getModel().getObject().getState() == ObjectState.DELETED;
                        }
                        @Override
                        public boolean isEnabled() {
                            if (isSupportUser() && !isRoot())
                                return false;
                            if (!role_dataset_members)
                                return false;
                            return true;
                        }
                    };
                }
            });
        } 
        catch (Exception e) {

            logger.error(e);
            final String err = e.getClass().getSimpleName() + " | " + e.getMessage();
            menu.addItem(new MenuItemFactory<DataSetMember>() {
                @Override
                public AbstractMenuItemPanelV5<DataSetMember> getItem(String id) {
                    return new AjaxMenuItemPanelV5<DataSetMember>(id) {
                        public void onClick(AjaxRequestTarget target) {

                        }

                        @Override
                        public String getLabel() {
                            return err;
                        }

                        @Override
                        public boolean isEnabled() {
                            return false;
                        }
                    };
                }
            });
        }

        return menu;
    }
    
	protected BaseBrowser<DataSetMember> newTreeBrowser() {
		
		AbstractBrowser<DataSetMember> br = new AbstractTreeBrowser<DataSetMember, TreeNode<DataSetMember>>("browser", getName(), getQuery()) {
			@Override
			public String getBrowserType() { 
				return DataSetMembersConsole.this.getBrowserType();
			}
			@Override
			protected TreeProvider<TreeNode<DataSetMember>> getTreeProvider() {
				return DataSetMembersConsole.this.getTreeProvider();
			}
			@Override
			protected boolean isSavedQueriesEnabled() {
				return DataSetMembersConsole.this.isSavedQueriesEnabled();
			}
			@Override
			protected boolean isFiltersEnabled() {
				return DataSetMembersConsole.this.isFiltersEnabled();
			}
			@Override
			protected boolean isDefaultTopPanelVisible() {
				return DataSetMembersConsole.this.isDefaultTopPanelVisible();
			}
			protected String getDefaultUserPreference(String key) {
				return DataSetMembersConsole.this.getDefaultUserPreference(key);
			}
			@Override
			public List<NavigationOrder> getOrders() {
				return DataSetMembersConsole.this.getOrders();
			}
			@Override
			public Searcher getSearcher() {
				return DataSetMembersConsole.this.getSearcher();
			}
			@Override
			protected String getContextKey() {
				return DataSetMembersConsole.this.getName() + super.getContextKey();
			}
			@Override
			protected IModel<DataSetMember> getModel(DataSetMember object) {
				return DataSetMembersConsole.this.getModel(object);
			}
			@Override
			protected Panel getPanel(IModel<DataSetMember> model) {
				return DataSetMembersConsole.this.getPanel(model);
			}
			@Override
			protected Panel getPanel(IModel<DataSetMember> model, List<String> snippets) {
				return DataSetMembersConsole.this.getPanel(model, snippets);
			}
			@Override
			protected List<GridColumn<SearchResult, String>> getColumns() {
				return DataSetMembersConsole.this.getColumns();
			}
			@Override
			protected Panel getMenu(IModel<DataSetMember> model) {
				return DataSetMembersConsole.this.getMenu(model);
			}
			@Override
			public DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
				return DataSetMembersConsole.this.getGridExportSavedQueryMenuItem(id, model);
			}
			@Override
			protected Panel getTopPanel() {
				if (DataSetMembersConsole.this.hasTopPanel())
					return DataSetMembersConsole.this.getTopPanel();
				return new InvisiblePanel("top");
			}
			@Override
			protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
				return DataSetMembersConsole.this.getRowContainerCss(rowmodel);
			}
			@Override
			protected List<ToolbarItem> getToolbarItems() {
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				List<ToolbarItem> items_console = DataSetMembersConsole.this.getToolbarItems(this);
				List<ToolbarItem> items_super = super.getToolbarItems();
				items.addAll(items_super);
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_LEFT) items.add(v);});
				items_console.forEach(v -> { if (v.getJustify() == ToolbarItem.JUSTIFY_RIGHT) items.add(v);}); 
				items.add(DataSetMembersConsole.this.getGridToolbarMenuItem());
				return items;
			}
			@Override
			public List<ToolbarItem> getSelectionToolbarItems() {
				List<ToolbarItem> items = super.getSelectionToolbarItems();
				List<ToolbarItem> items2 = new ArrayList<ToolbarItem>();
				items2.addAll(items);
				items2.addAll(DataSetMembersConsole.this.getSelectionToolbarItems(this));
				return items2;
			}
			@Override
			protected boolean hasExpander() {
				return DataSetMembersConsole.this.hasExpander();
			}
			@Override
			public boolean isMyListsEnabled() {
				return DataSetMembersConsole.this.isMyListsEnabled();
			}
			@Override
			protected List<LayoutPanel> getPanels() {

				List<LayoutPanel> panels = super.getPanels();

				panels.add(new LayoutPanel("side", AbstractLayout.SIDE_DISPOSITION) {
					protected WebMarkupContainer getPanel(String id) {
						SavedQueriesSidePanel sq = new SavedQueriesSidePanel(id, getBrowser()) {
							@Override
							public void onClose(AjaxRequestTarget target) {
								onClosePanel(this, target);
							}

							@Override
							public void onFilters(AjaxRequestTarget target) {
								DataSetMembersConsole.this.onFilters(target);
							}
						};
						return sq;
					}
				});

				add(new WicketEventListener<ApplySavedQueryEvent>() {
					@Override
					public void onEvent(ApplySavedQueryEvent event) {
						FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
						if (panel != null) {
							panel.setParameters(event.getQuery().getParameters());
							getBrowser().getQuery().setParameters(event.getQuery().getParameters());
							getBrowser().refresh(event.getRequestTarget());
						}
					}
				});

				return panels;
			}
			@Override
			protected boolean isSelectionEnabled() {
				return DataSetMembersConsole.this.isSelectionEnabled();
			}
			@Override
			protected boolean isMenuEnabled() {
				return DataSetMembersConsole.this.isMenuEnabled();
			}
			@Override
			protected boolean isVisible(Facet facet) {
				return DataSetMembersConsole.this.isVisible(facet);
			}
			@Override
			public Query getQuery() {
				return DataSetMembersConsole.this.getQuery();
			}
			@Override
			public boolean isRememberQuery() {
				return DataSetMembersConsole.this.isRememberQuery();
			}
			@Override
			protected void onUpdateQuery(AjaxRequestTarget target) {
				DataSetMembersConsole.this.onUpdateQuery(target);
			}
		};
		
		return br;
	}

    protected Page getPage(IModel<DataSetMember> model, long index) {
        Searcher searcher = getSearcher();
        SolrCursor soc = new SolrCursor((SolrResultSet) searcher.getResultSet(), index);
        MemberPage page = new MemberPage(model, new SolrCursorModel(soc));
        return page;
    }

    protected Panel getPanel(IModel<DataSetMember> model, List<String> snippets) {
        return new DataSetMemberHitExpandedPanel("editor", this, model, snippets);
    }

    protected Panel getPanel(IModel<DataSetMember> model) {
        return new DataSetMemberHitExpandedPanel("editor", this, model);
    }

    @Override
    protected void addListeners() {
        super.addListeners();

        /**
         * apply list
         */

        add(new WicketEventListener<MyListsApplyUserListEvent>() {
            @Override
            public void onEvent(MyListsApplyUserListEvent event) {
                IModel<UserList> list = event.getUserList();
                setQuery(new DatasetMemebersUserListQuery(list.getObject(), getQueryIndex(), getDataSet()));
                FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
                if (panel!=null && panel.getParameters()!=null) {
                	panel.getParameters().put("userlist", new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName()));
                	panel.setParameters(panel.getParameters());
                }
                getBrowser().setQuery(getQuery());
                panel.setQuery(getQuery());
                getBrowser().refresh(event.getRequestTarget());
                refresh(event.getRequestTarget());
                list.detach();
            }

            @Override
            public boolean handle(com.novamens.event.Event event) {
                return event instanceof MyListsApplyUserListEvent;
            }
        });

        /**
         * add/remove object to List
         */
        add(new WicketEventListener<MyListsUserListItemUpdateObjectEvent<DataSetMember>>() {
            @Override
            public void onEvent(MyListsUserListItemUpdateObjectEvent<DataSetMember> event) {
                FeedbackHelper.showInfoToast(event.getListModel().getObject().getName(), event.getModel().getObject().getDisplayName());
                DataSetMembersConsole.this.refresh(event.getRequestTarget());
            }

            @Override
            public boolean handle(com.novamens.event.Event event) {
                return event instanceof MyListsUserListItemUpdateObjectEvent;
            }
        });

        add(new WicketEventListener<ClickEvent<DataSetMember>>() {
            @Override
            public void onEvent(ClickEvent<DataSetMember> event) {
                if (event.getModel().getObject().getDataSet().isExternal()) {
                    DataSetMember member = (DataSetMember) event.getModel().getObject();
                    if (member instanceof ExternalMember) {
                        setResponsePage(new RedirectPage(((ExternalMember) member).getExternalUrl()));
                    } 
                    else {
                        logger.error("Not External Member " + member.getDisplayName());
                    }
                } else
                    setResponsePage(DataSetMembersConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject())));
            }
        });

        add(new WicketEventListener<LabelEvent>() {
            @Override
            public void onEvent(LabelEvent event) {
                DataSetMembersConsole.this.refresh(event.getRequestTarget());
            }
        });

    }

    @Override
	protected void handle(GeneralAjaxWicketEvent event) {
    	if ("grid-browser".equals(event.getName())) {
			setBrowserType("grid");
			setQuery(newQuery());
	        fireScanAll(new TreeNodeSelection<DataSetMember>(event.getRequestTarget(), null));
            refresh(event.getRequestTarget());
    	}
    	if ("tree-browser".equals(event.getName())) {
    		if (!"tree".equals(getBrowserType()) && !"treelist".equals(getBrowserType())) {
    			setBrowserType("tree");
	    		FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
	    		if (panel!=null && panel.isFiltersApplied())
	    			panel.clearAll();
				setQuery(newQuery());
				panel.setQuery(getQuery());
		        fireScanAll(new TreeNodeSelection<DataSetMember>(event.getRequestTarget(), null));
	            refresh(event.getRequestTarget());
    		}
    	}
    	super.handle(event);
	}

    /**
     * Selected elements
     */
    @Override
    protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<DataSetMember> browser) {

        if (this.selection_toolbar != null)
            return this.selection_toolbar;

        this.selection_toolbar = new ArrayList<ToolbarItem>();

        // Delete
        //
        this.selection_toolbar.add(new DeleteButton(browser, ToolbarItem.Align.TOP_LEFT, true) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                List<BCElement> list = new ArrayList<BCElement>();
                list.add(new SettingsBC());
                list.add(new BCElement("bc.dataset.members"));
                list.add(new MembersBC(DataSetMembersConsole.this.getDataSetModel().getObject()));
                list.add(new BCElement(new Model<String>("delete")));

                BatchDeletePage<DataSetMember> page = new BatchDeletePage<DataSetMember>(getDataSet().getName(), getDeleteableSelection(), list) {
                    @Override
                    protected void onClose() {
                        setResponsePage(new DataSetMembersPage(getDataSetModel()));
                    }

                    @Override
                    protected String executeDelete(IModel<DataSetMember> model) {
                        try {
                            DataSetMember member = getContentDao().findMemberById(model.getObject().getId());
                            member.getService(DOMObjectService.class).delete();
                            return null;
                        } catch (DataIntegrityViolationException | ConstraintException e) {
                            logger.error(e);
                            return e.getClass().getSimpleName();
                        } catch (Exception e) {
                            logger.error(e);
                            return e.getClass().getSimpleName();
                        }
                    }
                };
                setResponsePage(page);
            }

            @Override
            public boolean isEnabled() {
                if (getBrowser().getSelection().isEmpty())
                    return false;
                return !getDataSet().isExternal() && hasWritePermissions();
            }
        });


        /**
         * 	TAG TOOL
         */
        this.selection_toolbar.add(new ToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {
            @Override
            public void onClick() {
                List<DataSetMember> list = new ArrayList<DataSetMember>();
                for (IModel<DataSetMember> mod : DataSetMembersConsole.this.getBrowser().getSelection()) {
                    list.add(mod.getObject());
                }
                TagManagementPage page = new TagManagementPage();
                page.setDataSetMemberSelection(DataSetMembersConsole.this.getBrowser().getSelection());
                setResponsePage(page);
            }
            @Override
            public boolean isEnabled() {
                return isAdmin() || isRoot() || isSupport();
            }
            @Override
            public boolean isVisible() {
                return isAdmin() || isRoot() || isSupport();
            }
            protected String getLabelStr() {
                return getLabel("open-tag-tool").getObject();
            }
        });
    
        return this.selection_toolbar;
    }

    @SuppressWarnings("unchecked")
	protected void delete(List<?> selection) {
        List<IModel<DataSetMember>> list = (List<IModel<DataSetMember>>) selection;
        for (IModel<DataSetMember> c : list) {
            try {
				// must be clean up with an Async Command
				//
            	c.getObject().getService(DOMObjectService.class).markAsDeleted();
            } 
            catch (Exception e) {
                logger.error(e);
            }
        }
    }

    /**
     *
     */
    @Override
    protected List<ToolbarItem> getToolbarItems(BaseBrowser<DataSetMember> browser) {

        if (this.items != null)
            return this.items;

        this.items = super.getToolbarItems(browser);

        this.items.add(new ToolbarAlert(browser, Align.TOP_LEFT) {
            protected IModel<String> getLabel() {
                DataSet dataset = getDataSet();
                if (dataset.isExternal()) {
                    return new Model<String>(new StringResourceModel("external", this, null).getObject().replace("{0}", dataset.getName()));
                }
                if (dataset.isAggregation()) {
                    DataSet containerDataset = getContainerDataSet(dataset);
                    String container_name = containerDataset != null ? containerDataset.getName() : "";
                    String containerLink = "<a class=\"btn-link\"  href=\"  " + (containerDataset != null ? ("/dataset/" + containerDataset.getId().toString()) : "#") + "  \">" + container_name + "</a>";
                    String addFromXlsxLink = "<a class=\"btn-link\"  href=\"  " + (containerDataset != null ? ("/dataset/bulkcreationt/" + dataset.getId().toString()) : "#") + "  \">"
                            + new StringResourceModel("add-xlsx", DataSetMembersConsole.this, null).getObject()
                            + "</a>";
                    return new Model<String>(new StringResourceModel("builtin-nocreate", this, null).getObject()
                            .replace("{0}", containerLink)
                            .replace("{1}", addFromXlsxLink)
                    );
                }
                return new StringResourceModel("readonly", this, null);
            }

            @Override
            public boolean isVisible() {
                if (getDataSet().isExternal())
                    return true;
                if (getDataSet().isReadonly() && !isRoot())
                    return true;
                if (getDataSet().isAggregation())
                    return true;
                return false;
            }
        });


        // New
        //
        this.items.add(new NewButton(browser, Align.TOP_LEFT) {
            protected String getButtonCss() {
                return "btn btn-primary btn-md";
            }
            public void onClick() {
                try {
                    Object member = ServiceLocator.getService(ObjectFactoryService.class).createMember(getDataSet());
                    ((DataSetMember) member).setStrValue(getConsoleLabel("new", getDataSet().getName()).getObject());
                    Page page = DataSetMembersConsole.this.getPage(DataSetMembersConsole.this.getModel((DataSetMember) member), 0);
                    ((MemberPage) page).setNew(true);
                    ((MemberPage) page).setEditon(true);
                    setResponsePage(page);
                } 
                catch (ContentCreationException e) {
                    logger.error(e);
                    throw new KbeeRuntimeException(e);
                }
            }
            @Override
            public boolean isVisible() {
                if (getDataSet().isAggregation())
                    return false;
                if (getDataSet().isExternal())
                    return false;
                if (getDataSet().isReadonly() && !isRoot())
                    return false;
                return hasWritePermissions();
            }
            @Override
            public boolean isEnabled() {
                if (getDataSet().isReadonly() && !isRoot())
                    return false;
                if (getDataSet().isExternal())
                    return false;
                return hasWritePermissions();
            }
            protected IModel<String> getLabel() {
                StringResourceModel model = new StringResourceModel("new", this);
                return model;
            }
        });

        // Bulk creation
        MenuButtonToolbarItem<DataSet> mn = new MenuButtonToolbarItem<DataSet>(browser, ToolbarItem.Align.TOP_LEFT) {
            @Override
            public boolean isVisible() {
                if (getDataSet().isAggregation() || getDataSet().isExternal() || getDataSet().isHierachical())
                    return false;
                if (getDataSet().isReadonly() && !isRoot())
                    return false;
                
                if (getDataSet().getDataSetType()==DataSetType.LABEL)
                    return false;
                
                return (role_admin || role_support) && !(isFreeVersion());
            }
            @Override
            public String getAddCss() {
                return "btn btn-default btn-md atright";
            }
        };

        mn.setTitle(getLabel("bulk-creation"));

        ContextMenuPanel<DataSet> mp = new ContextMenuPanel<DataSet>(null);

        mp.addItem(new MenuItemFactory<DataSet>() {
            @Override
            public AbstractMenuItemPanelV5<DataSet> getItem(String id) {
                return new MenuItemPanelV5<DataSet>(id) {
                    @Override
                    public void onClick() {
                        setResponsePage(new RedirectPage("/dataset/bulkcreationt/" + getDataSet().getId().toString()));
                    }

                    @Override
                    public String getLabel() {
                        return new StringResourceModel("add-xlsx", DataSetMembersConsole.this, null).getObject();
                    }
                };
            }
        });

        mp.addItem(new MenuItemFactory<DataSet>() {
            @Override
            public AbstractMenuItemPanelV5<DataSet> getItem(String id) {
                return new MenuItemPanelV5<DataSet>(id) {
                    @Override
                    public void onClick() {
                        setResponsePage(new MemberBatchCreationPageV5(new ObjectModel<DataSet>(getDataSet())));
                    }
                    @Override
                    public String getLabel() {
                        return getLabelString("add-list");
                    }
                };
            }
        });

        mn.setMenuPanel(mp);


        this.items.add(mn);

        InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                InfoDialog infoDialog = (InfoDialog) getInformationModal();
                infoDialog.open(target, () -> {
                    return DataSetMembersConsole.this.getDataSet().getName();
                }, new Model<String>(DataSetMembersConsole.this.getDescription()));
            }
        };
        
        if (getDataSet().isHierachical()) {
        	this.items.add(new GridButton(this, ToolbarItem.Align.TOP_RIGHT));
        	this.items.add(new TreeButton(this, ToolbarItem.Align.TOP_RIGHT));
        }
		
        this.items.add(infoButton);

        this.items.add(new TreeBreadcrumbToolbarItem(browser, ToolbarItem.Align.BOTTOM_LEFT, null) {
    		@Override
    		public String getRootDisplayName() {
    			return getDataSet().getDisplayName();
    		}
    		@Override
        	public boolean isVisible() {
        		return "tree".equals(getBrowserType()) || "treelist".equals(getBrowserType());
        	}
        });
		
        return this.items;
    }

    protected DataSet getContainerDataSet(DataSet dataSet) {
        DataSet da = dataSet.getService(DataSetService.class).getAggregatorDataSet();
        return da;
    }

    protected String getDescription() {
        StringBuilder str = new StringBuilder();

        if (getDataSet().getDescription() != null) {
            str.append("<section>");

            str.append("<h3>" + getLabel("description").getObject() + "</h3><p>" + getDataSet().getDescription() + "</p>");
            str.append("</section>");
        }
        int n = 0;
        str.append("<section>");
        for (Classifier c : getContentDao().getClassifiers(getDomain())) {
            if (c.getDataSet().getId().equals(getDataSet().getId())) {
                if (n++ == 0)
                    str.append("<h3>" + getLabel("used-by-classifier").getObject() + "</h3>");

                str.append("<a href=" + "/model/classifiers/" + c.getId().toString().trim() + " target=\"_blank\">" + c.getName() + "</a><br />");
            }
        }
        str.append("</section>");
        str.append("<section>");
        str.append("<h3>" + getLabel("settings-title").getObject() + "</h3>");
        str.append("<p>" + getLabel("open-dataset").getObject() + " <a href= \"/model/datasets/" + getDataSet().getId().toString() + "\" target=\"_blank\">" + getDataSet().getName() + "</a></p>");
        str.append("</section>");


        return str.toString();
    }

    @Override
    protected GridMenu getGridToolbarMenuItem() {
        GridMenu menu = super.getGridToolbarMenuItem();

        menu.addItem(itemId ->
        	new SeparatorMenuItemPanelV5<File>(itemId) {
        		@Override
                public String getCssClass() {
        			return "divider";
        		}
        	});

        menu.addItem(itemId ->
        	new AjaxCheckMenuItemPanelV5<Void>(itemId) {
        		@Override
        		public String getLabel() {
        			return getLabelString("show-deleted");
        		}
        		@Override
        		public void onClick(AjaxRequestTarget target) throws Exception {
        			DataSetMembersConsole.this.setDeletedVisible(!DataSetMembersConsole.this.isDeletedVisible());
        			boolean deleted_visible = DataSetMembersConsole.this.isDeletedVisible();
        			String states = "[" + String.valueOf(ObjectState.ENABLED.getId()) +
        					", " + String.valueOf(ObjectState.ARCHIVED.getId()) +
        					(deleted_visible ? (", " + String.valueOf(ObjectState.DELETED.getId())) : "") + "]";
                        DataSetMembersConsole.this.getSearcher().getQuery().getParameters().put("state", states);
                        DataSetMembersConsole.this.refresh(target);
        		}
        		@Override
        		public String getIconCssClass() {
        			return DataSetMembersConsole.this.isDeletedVisible() ? (AbstractLinkMenuItemPanelV5.CHECK + " fa-fw atright") : "";
        		}
        		@Override
        		public String getCssClass() {
        			if (DataSetMembersConsole.this.isDeletedVisible())
                            return "label-selected";
                        else
                            return "label-no-selected";
                    }
                });

        return menu;
    }

    protected List<IModel<DataSetMember>> getDeleteableSelection() {
        List<IModel<DataSetMember>> selection = new ArrayList<IModel<DataSetMember>>();
        for (IModel<DataSetMember> model : getBrowser().getSelection()) {
            try {
                selection.add(new DeleteableModel(model.getObject()));
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return selection;
    }
    
   protected boolean hasHome(DataSetMember member) {
	   if (!(member instanceof EntityMember)) return false;
	   if (homesets==null) {
		   homesets = new ArrayList<DataSet>();
		   for (Classifier classifier : getContentDao().getClassifiers(member.getDomain())) {
			   if (classifier.hasHome()) {
				   homesets.add(classifier.getDataSet());
			   }
		   }
	   }
	   if (homesets.contains(member.getDataSet()))
		   return true;
	   return false;
   }
   
   protected String getUrlHome(DataSetMember member) {
	   if (!(member instanceof EntityMember)) return null;
	   String url = "/entityhome/" + String.valueOf(member.getId()) + "/";
	   for (Classifier classifier : getContentDao().getClassifiers(member.getDomain())) {
		   if (classifier.hasHome()) {
			   url += String.valueOf(classifier.getId());
			   break;
		   }
	   }
	   return url;
   }

   protected Long getReferences(DataSetMember member) {
        String id = String.valueOf(member.getId());
        Long references = this.references.get(id);
        if (references == null) {
            try {
                Query query = new MemberQuery(getQueryIndex(), member);
                references = Long.valueOf(query.execute().size());
                this.references.put(id, references);
            } 
            catch (Exception e) {
                logger.error(e);
                references = (long) -1;
            }
        }
        return references;
    }

    protected List<String> getReferencesList(DataSetMember member, int max) {
        try {
            Query query = new MemberQuery(getQueryIndex(), member);
            ResultSet results = query.execute();
            int n = 0;
            List<String> li = new ArrayList<String>();
            while (n < max && n < results.size()) {
                SearchResult re = results.next();
                if (re.getObject() instanceof Identifiable) {
                    String cla = re.getObject().getClass().getSimpleName();
                    if (re.getObject() instanceof Content) {
                        li.add((((Content) re.getObject()).getDisplayName() + "    <span class=\"ago\" style=\"float:right;\">[ " + cla + " - OId/Id: " +
                                String.valueOf(((Content) re.getObject()).getOId()) + " / " +
                                String.valueOf(((Content) re.getObject()).getId()) + ". v" + String.valueOf(((Content) re.getObject()).getVersion()) + " ]</span> "));
                    } 
                    else
                        li.add((((Identifiable) re.getObject()).getDisplayName() + "    <span class=\"ago\" style=\"float:right;\">[ " + cla + " - id: " + String.valueOf(((Identifiable) re.getObject()).getId()) + " ]</span> "));
                } 
                else
                    li.add(re.getObject().getClass().getName());
                n++;
            }
            Collections.sort(li);
            return li;
        } 
        catch (Exception e) {
            logger.error(e);
            return new ArrayList<String>();
        }
    }

    @Override
    protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
        try {
            if (((DataSetMember) rowmodel.getObject().getObject()).getState() == ObjectState.ARCHIVED) return "archived-state";
            if (!isDeletedVisible())
                return null;
            if (((DataSetMember) rowmodel.getObject().getObject()).getState() == ObjectState.DELETED) return "deleted-state";
            	return null;

        } 
        catch (Exception e) {
            logger.error(e);
            return null;
        }
    }


    /**
     * @param model
     * @return
     */
    protected List<IModel<LabelMember>> getLabelMembers() {

        if (this.labels != null)
            return this.labels;

        this.labels = new ArrayList<IModel<LabelMember>>();

        List<Classifier> list = getDataSet().getClassifiers();

        for (Classifier ca : list) {
            if (ca.getState() == ObjectState.ENABLED && (ca.getDataSet() instanceof LabelSet)) {
                for (DataSetMember dm : getContentDao().getMembers(ca.getDataSet(), "strvalue")) {
                    if (dm.getState() == ObjectState.ENABLED)
                        this.labels.add(new ObjectModel<LabelMember>((LabelMember) dm));
                }
            }
        }

        Collections.sort(this.labels, new Comparator<IModel<LabelMember>>() {
            @Override
            public int compare(IModel<LabelMember> a, IModel<LabelMember> b) {
                try {
                    return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
                } catch (Exception e) {
                    logger.error(e);
                    return 0;
                }
            }
        });

        return this.labels;
    }
    
    @Override
    protected void addModals() {
        super.addModals();
        replace(new ObjectAuditModal<DataSetMember>("audit-trail-modal"));
    }
    
    protected boolean hasWritePermissions() {
        if (role_admin || role_dataset_members || role_information_model) {
        	return true;
        } 
        else {
        	// la relacion entre rol y deataset deberia definir el permiso. por ahora se lo niego
        	return ServiceLocator.getService(UserService.class).isWriteable(getDataSet());
        	//return false;
        }
    }
    
	protected boolean isWriteable(DataSetMember member) {
		//return hasWritePermissions();
		return ServiceLocator.getService(UserService.class).isWriteable(member);
	}

    protected IModel<DataSet> getDataSetModel() {
        return this.datasetmodel;
    }

    @Override
    protected boolean isMyListsEnabled() {
        return true;
    }
    
    protected boolean isDownload() {
        return is_send_email;
    }

    protected boolean isSendByEmail() {
        return is_send_email;
    }

    protected boolean isFreeVersion() {
        return is_basic;
    }
    
    protected boolean isSupportUser() {
        return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
    }

    protected void setDeletedVisible(boolean b) {
        this.is_deleted_visible = b;
        setUserPreference("deleted-visible", (b ? "yes" : "no"));
    }

    protected boolean isDeletedVisible() {
        return this.is_deleted_visible;
    }
    
    @Override
    protected boolean hasExpander() {
        return true;
    }
    
    @Override
	protected TreeProvider<TreeNode<DataSetMember>> getTreeProvider() {
		if (treeprovider==null) {
			treeprovider = new DataSetTreeProvider(getDataSet());
		}
		return treeprovider;
	}

//    protected long getTotalBuiltIn(DataSetMember obj, Long builtin_id) {
//        try {
//            Query query = getBuiltInQuery(obj, builtin_id);
//            if (query == null)
//                return 0;
//            ResultSet res = query.execute();
//            return (long) res.size();
//        } 
//        catch (Exception e) {
//            logger.error(e);
//            return 0;
//        }
//    }
//
//    protected String getNumberTotalBuiltInClass(DataSetMember obj, Long builtin_id) {
//        try {
//            long ref = getTotalBuiltIn(obj, builtin_id);
//            return ref > 0 ? "col number-md info" : "col number-md";
//        } 
//        catch (Exception e) {
//            logger.error(e);
//            return "number-md";
//        }
//    }

    @Override
    protected  IModel<DataSetMember> getModel(DataSetMember object) {
    	return new ObjectModel<DataSetMember>(object, true);
	}
    
    private ContentSecurityDao getContentSecurityDao() {
    	return (ContentSecurityDao) ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
    }

    private String getRules(EntityMember member, boolean isHTML) {
    	List<ActionRule> list = ((ActionRuleRepository)getRepository(ActionRule.class)).findByEntity(member);
    	if (list.size()==0)
    		return "";
    	StringBuilder str  = new StringBuilder();
    	list.forEach( item -> {
    		if (item.getState()==ObjectState.ENABLED) {
    			if (isHTML)
    				str.append(str.length()>0 ? "<br/> ": "");
    			else
    				str.append(str.length()>0 ? " - ": "");
    			if (isHTML)
    				str.append("<a href=\"" + getServerUrl()+"/dataset/members/recurrent/"+ String.valueOf(item.getId()) + "\" class=\"btn-link\">"+ item.getDisplayName() +"</a>");
    			else
    				str.append(item.getDisplayName());
    		}
    	});
    	return str.toString();
	}
}

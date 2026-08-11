package kbee.web.security.user;



import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneId;
 
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.GridMenu;
import com.novamens.kbee.wicket.markup.html.console.browser.InfoButton;

import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import com.novamens.kbee.wicket.util.FeedbackHelper;
import com.novamens.service.WebSessionService;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.LabelMember;
import com.novamens.content.model.LabelSet;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.UserSet;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.DomainRole;
import com.novamens.content.security.EntityRole;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.content.service.DomainService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserProfileType;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserSelfService;
import com.novamens.content.user.UserService;
import com.novamens.content.userlist.UserList;
import com.novamens.content.userlist.UserListItem;
import com.novamens.content.userlist.UserListService;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.content.web.security.markup.UsersBatchPasswordChangeButton;
import com.novamens.content.web.security.markup.UsersBatchPasswordChangePanel;
import com.novamens.content.web.security.markup.UsersBatchSetGlobalPermissionPanel;
import com.novamens.content.web.security.markup.UsersBatchSetGlobalRolePanel;
import com.novamens.content.web.user.markup2.PersonLabelMenuItemFactory;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailData;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;

import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.content.command.RemoveOrphansCommand;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem.Align;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.console.event.GridPanelNullObjectEvent;
 
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;

import com.novamens.kbee.wicket.markup.html.console.grid.LastModifiedColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.SimpleDateColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsUserListItemUpdateObjectEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.SolrCursorModel;
import com.novamens.kbee.wicket.markup.html.console.panel.SubMenuAjaxUserListItemPanel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;

import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.security.ReservedUsername;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrCursor;
import com.novamens.solr.indexer.query.SolrResultSet;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SubmenuAjaxItemPanelV5;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.email.EmailSendServiceRequest;
import kbee.util.NumberFormatter;
import kbee.web.console.AbstractFacetedConsole;
import kbee.web.console.BaseBrowser;
import kbee.web.console.BulkCreationButton;
import kbee.web.console.ClassificableNameColumnPanel;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.NameColumnPanel;
import kbee.web.console.grid.AttributeColumn;
import kbee.web.console.grid.ClassifierColumn;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.event.wicket.ClickResetPasswordEvent;
import kbee.web.event.wicket.ClickSetGroupEvent;
import kbee.web.event.wicket.LabelEvent;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.nav.DataSetBC;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.security.UsersQuery;
import kbee.web.security.UsersUserListQuery;
import kbee.web.service.PortalPanelService;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class UsersConsole extends AbstractFacetedConsole<Person> {
    private static final long serialVersionUID = 1L;

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UsersConsole.class.getName());

    private IModel<UserSet> datasetmodel;

    final boolean is_support = ServiceLocator
    	.getService(SecurityService.class)
    	.isMember(KbeeGlobalRole.SUPPORT.getId());
    final boolean is_root = ServiceLocator
    	.getService(SecurityService.class)
    	.isRoot();
    final boolean is_admin = is_root || 
    	ServiceLocator.getService(SecurityService.class)
    	.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    final boolean is_security = ServiceLocator
    	.getService(SecurityService.class)
    	.isMember(KbeeGlobalRole.SECURITY.getId());
    final boolean is_federated_security = ServiceLocator
        	.getService(SecurityService.class)
        	.isMember(KbeeGlobalRole.FEDERATED_SECURITY.getId());
    final boolean is_su = ServiceLocator
    	.getService(SecurityService.class)
    	.isMember(KbeeGlobalRole.SU.getId());

    public static String KEY = "users";

    private boolean is_deleted_visible = false;

    private List<GridColumn<SearchResult, String>> columns;

    private List<IModel<LabelMember>> labels = null;
    private List<ToolbarItem> items;
    private List<ToolbarItem> selection_toolbar;

    private Locale user_locale;
    private ZoneId user_zoneid;

    /**
     * @param datasetmodel
     * @param query
     */
    public UsersConsole(IModel<UserSet> datasetmodel, Query query) {
        super(KEY, query);
        this.is_deleted_visible = getUserPreference("deleted-visible", "no").equals("yes") ? true : false;
        setDataSet(datasetmodel);
        this.setOutputMarkupId(true);
		super.setListBrowser(true);
    }

	
    public void setDataSet(IModel<UserSet> model) {
        this.datasetmodel = model;
    }


    @Override
    public boolean hasBillboardPanel() {
        return true;
    }


    public UserSet getDataSet() {
        return datasetmodel.getObject();
    }

    public List<ENotiRule> getEmailRules(User user) {
        return ServiceLocator.getService(ENotiRuleService.class).getEmailRules(user);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            this.columns = null;
            this.items = null;

            this.user_locale = null;
            this.user_zoneid = null;

            if (this.labels != null)
                this.labels.forEach(item -> item.detach());

            if (this.selection_toolbar != null)
                selection_toolbar.forEach(item -> item.detach());

            if (this.datasetmodel != null)
                this.datasetmodel.detach();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public boolean isSelectionEnabled() {
        return true;
    }


    @Override
    public Query newQuery() {
        return setUserPreference(new UsersQuery(getQueryIndex(), getDataSet(), isDeletedVisible()));
    }

    public Page getConsolePage(Query query) {
        return getConsolePage(query, -1);
    }

    
    @Override
    public void onBeforeRender() {
        super.onBeforeRender();
        try {
            this.user_zoneid = ZoneId.of(getSessionUser().getTimeZone());
            if (this.user_zoneid == null)
                this.user_zoneid = ZoneId.systemDefault();
        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            this.user_zoneid = ZoneId.systemDefault();
        }
        this.user_locale = getSessionUser().getLocale();
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
    protected Panel getMenu(IModel<Person> model) {

        ContextMenuPanel<Person> menu = new ContextMenuPanel<Person>(model);
        menu.setOutputMarkupId(true);
        
        menu.addItem(new MenuItemFactory<Person>() {
        	@Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new com.novamens.wicket.markup.html.actions.MenuItemPanelV5<Person>(id) {
                     public void onClick() {
                        setResponsePage(UsersConsole.this.getPage(getModel(), UsersConsole.this.getIndex(getModel().getObject()), false));
                    }
                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.open").getObject();
                    }
                    //@Override
                    //public String getTarget() {
                    //    return "_blank";
                    //}
                };
            }
        });

        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new SubMenuAjaxUserListItemPanel<Person>(id, model, UsersConsole.this.getName(), UserListItem.NEWEST);
            }
        });

        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new AjaxMenuItemPanelV5<Person>(id) {
                    public void onClick(AjaxRequestTarget target) {
                    	try {
                        
                    		// person is the user who sends the link by email
                    		((KbeeUser) getUser()).getService(UserSelfService.class).sendLinkToResetPassword( getPerson() );
                        
                        FeedbackHelper.showInfoToast(getConsoleLabel("userconsole.resetpassword.title").getObject(),getConsoleLabel("userconsole.resetpassword.done", getModel().getObject().getEmail()).getObject());
                    	} catch (Exception e) {
                    		logger.error(e);
                    		FeedbackHelper.showErrorToast(e.getClass().getName(), e.getMessage());
                    	}
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.resetpassword").getObject();
                    }

                    public User getUser() {
                        return (getModel().getObject().getProfile(UserProfile.class)).getUser();
                    }
                };
            }
        });


        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new AjaxMenuItemPanelV5<Person>(id) {
                    @SuppressWarnings("unchecked")
                    public void onClick(AjaxRequestTarget target) {
                        if (getUser() != null) {
                            IModel<User> usermodel = new ObjectModel<User>(getUser());
                            Modal modal = UsersConsole.this.getAuditTrailModal();
                            boolean adjust_height_to_window = true;
                            ((ObjectAuditModal<User>) modal).open(target, usermodel, adjust_height_to_window);
                        }
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.audittrail").getObject();
                    }

                    public User getUser() {
                        return (getModel().getObject().getProfile(UserProfile.class)).getUser();
                    }
                };
            }
        });


        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new AjaxMenuItemPanelV5<Person>(id) {
                    public void onClick(AjaxRequestTarget target) {
                    	try {
                        ServiceLocator.getService(SecurityContentMgmtService.class).enable(getUser());
                        FeedbackHelper.showInfoToast(getConsoleLabel("usersconsole.contextmenu.enable").getObject() +  " ok");
                        //refresh(target);
                    	} catch (Exception e) {
                    		logger.error(e);
                    		FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
                            //refresh(target);
                    	}
                    }

                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.enable").getObject();
                    }

                    @Override
                    public boolean isVisible() {
                        try {
                            return getUser() != null && !getUser().isEnabled() && !getUser().getName().startsWith("root@") && !isSupport();
                        } catch (Exception e) {
                            logger.error(e);
                            return false;
                        }
                    }

                    public User getUser() {
                        return (getModel().getObject().getProfile(UserProfile.class)).getUser();
                    }

                    @Override
                    public String getWorkingLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.enabling").getObject();
                    }

                    @Override
                    public boolean isEnabled() {
                        if (is_support && !is_root)
                            return false;
                        return true;
                    }
                };
            }
        });


        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new AjaxMenuItemPanelV5<Person>(id) {
                    @Override
                    public void onClick(AjaxRequestTarget target) throws Exception {

                    	try {	
	                    	// ver el disable -> Archiva el usuario ?
	                        ServiceLocator.getService(SecurityContentMgmtService.class).disable(getModel().getObject());
	                        FeedbackHelper.showInfoToast(getConsoleLabel("usersconsole.contextmenu.disable").getObject() + " ok");
	                        refresh(target);
                    	} catch (Exception e) {
                    		logger.error(e);
                    		FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
                    		refresh(target);
                    	}
                    }

                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.disable").getObject();
                    }

                    @Override
                    public boolean isVisible() {
                        try {
                            return getUser() != null && getUser().isEnabled() && !getUser().getName().startsWith("root@") && !isSupport();
                        } catch (Exception e) {
                            logger.error(e);
                            return false;
                        }
                    }

                    public User getUser() {
                        return (getModel().getObject().getProfile(UserProfile.class)).getUser();
                    }

                    @Override
                    public String getWorkingLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.disabling").getObject();
                    }

                    @Override
                    public String getCssClass() {
                        return "label-warning";
                    }

                    @Override
                    public boolean isEnabled() {
                        if (is_support && !is_root)
                            return false;
                        return true;
                    }
                };
            }
        });


        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                SubmenuAjaxItemPanelV5<Person> submenu = new SubmenuAjaxItemPanelV5<Person>(id, model) {
                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        attributes.setEventPropagation(EventPropagation.STOP);
                    }

                    @Override
                    public String getLabel() {
                        return UsersConsole.this.getLabel("labels").getObject();
                    }

                    @Override
                    protected void addItems() {
                        for (IModel<LabelMember> label : getLabelMembers()) {

                        	addItem(new PersonLabelMenuItemFactory(label, model, new ObjectModel<DataSetMember>((DataSetMember) model.getObject())) {
                                @Override
                                public void onUpdate(AjaxRequestTarget target) {
                                    fire(new LabelEvent(target));
                                }
                            });

                        	/**
                        	addItem(new PersonLabelMenuItemFactory(label, model, new ObjectModel<DataSetMember>((DataSetMember) model.getObject())) {
                                @Override
                                public void onUpdate(AjaxRequestTarget target) {
                                    fire(new LabelEvent(target));
                                }
                            });
                            **/
                        	
                        	
                        }
                    }
                };
                return submenu;
            }
        });

        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new SeparatorMenuItemPanelV5<Person>(id) {
                    @Override
                    public String getCssClass() {
                        return "divider";
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new AjaxMenuItemPanelV5<Person>(id) {
                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        attributes.setEventPropagation(EventPropagation.STOP);
                    }

                    public void onClick(AjaxRequestTarget target) {
                    	try {
                    		((KbeeUser) getUser()).getService(UserSelfService.class).resetPreferences();
                    		FeedbackHelper.showInfoToast( getConsoleLabel("usersconsole.contextmenu.resetpreferences").getObject() + " ok");
                    		refresh(target);

                    	} catch (Exception e) {
	                		logger.error(e);
	                		FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
	                		refresh(target);
                    	}


                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.resetpreferences").getObject();
                    }

                    @Override
                    public String getWorkingLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.resetingpreferences").getObject();
                    }

                    public User getUser() {
                        return (getModel().getObject().getProfile(UserProfile.class)).getUser();
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new AjaxMenuItemPanelV5<Person>(id) {
                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        attributes.setEventPropagation(EventPropagation.STOP);
                    }

                    public void onClick(AjaxRequestTarget target) {
                        try {
                            ((KbeeUser) getUser()).getService(UserSelfService.class).sessionFlush();
                            ((KbeeUser) getUser()).getService(UserSelfService.class).reindex();
                            RemoveOrphansCommand ro = new RemoveOrphansCommand(((KbeeUser) getUser()).getId());
                            
                    		FeedbackHelper.showInfoToast(getConsoleLabel("usersconsole.contextmenu.reindex").getObject() +" ok");
                    		refresh(target);
                            
                    		ro.execute();
                            
                        } catch (Exception e) {
                        	logger.error(e);
	                		FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
	                		refresh(target);
                        }
                    }
                    @Override
                    public boolean isVisible() {
                        return is_root || is_support;
                    }
                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.reindex").getObject();
                    }
                    @Override
                    public String getWorkingLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.reindexing").getObject();
                    }
                    public User getUser() {
                        return (getModel().getObject().getProfile(UserProfile.class)).getUser();
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new com.novamens.wicket.markup.html.actions.LinkMenuItemPanel<Person>(id) {
                    @SuppressWarnings("rawtypes")
					public void onClick() {
                        try {
                        	if (getUserProfile().getUser().getUserName().startsWith("root@kbee")) {
                        		throw new IllegalArgumentException("Can not impersonate root@kbee");
                        	}
                        	
                        	ServiceLocator.getService(UserService.class).impersonate(getUserProfile().getUser());
                            WebPage page = ServiceLocator.getService(PortalPanelService.class).getStartPage(getUserProfile());
                            page.getSession().setLocale(getUserProfile().getUser().getLocale());
                            setResponsePage(page);
                        	
                        } catch (Exception e) {
                            logger.error(e, getSessionUserName());
                            setResponsePage( new ApplicationErrorPage(e));
                        }
                    }

                    @Override
                    public boolean isEnabled() {
                        
                    	if (is_root)
                            return true;
                        
                        if (getUserProfile().getUser().getUserName().startsWith("root@"))
                            return false;
                        
                        return is_su || is_root;
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.impersonate").getObject();
                    }

                    public UserProfile getUserProfile() {
                        return (getModel().getObject().getProfile(UserProfile.class));
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new SeparatorMenuItemPanelV5<Person>(id) {
                    @Override
                    public String getCssClass() {
                        return "divider";
                    }
                };
            }
        });


        /**
         * Create from Existing User (assign the same Roles)
         */

        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new com.novamens.wicket.markup.html.actions.LinkMenuItemPanel<Person>(id) {
                    public void onClick() {
                        try {
                            int max_users = getDomain().getMaxUsers();
                            long total_users = getDomainMetricsServices().getUsers(getDomain());
                            if (max_users < 1 || max_users > total_users) {
                                try {
                                    setResponsePage(new NewUserPage2(new Model<NewUserData>(new NewUserData()),
                                            new ObjectModel<User>(getModel().getObject().getProfile(UserProfile.class).getUser())));
                                } catch (Exception e) {
                                    logger.error(e);
                                    setResponsePage(new ApplicationErrorPage<User>(new Model<String>(e.getClass().getSimpleName()), new Model<String>(getBrowser().getConsoleKey())));
                                }
                            } else {
                                StringResourceModel str = new StringResourceModel("quotalimit", UsersConsole.this, null);
                                setResponsePage(new ApplicationErrorPage<User>(str, new Model<String>(getBrowser().getConsoleKey())));
                            }
                        } catch (Exception e) {
                            logger.error(e, getSessionUserName());
                        }
                    }

                    @Override
                    public boolean isEnabled() {
                        return is_root || is_admin || is_security;
                    }

                    @Override
                    public boolean isVisible() {
                        return is_root || is_admin || is_security || is_support;
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.createfrom").getObject();
                    }
                };
            }
        });


        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5<Person>(id) {

                    @Override
                    public void onClick(AjaxRequestTarget target) throws Exception {
                        try {
                            User user = this.getModel().getObject().getProfile(UserProfile.class).getUser();
                            ServiceLocator.getService(WebSessionService.class).expireUserSessions(user.getUserName());

                            String msg= String.format(getConsoleLabel("usersconsole.contextmenu.forceLogOut.sessionsKilled").getObject(), user.getUserName());
                            FeedbackHelper.showInfoToast(msg);
                            
                        } catch (Exception e) {
	                		FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
                            logger.error(e, getSessionUserName());
                        }
                    }

                    @Override
                    public boolean isEnabled() {
                        
                    	if (is_root)
                    		return true;
                    			
                    	// Can not log out root user unless you are root
                    	if (getModel().getObject().getProfile(UserProfile.class).getUser().getUserName().startsWith("root@"))
                    		return false;
                    	
                    	return is_admin || is_security || isUserAdmin();
                    }

                    @Override
                    public boolean isVisible() {
                        return is_root || is_admin || is_security || isUserAdmin() || is_support;
                    }

                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.forceLogOut").getObject();
                    }
                };
            }
        });


        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new AjaxMenuItemPanelV5<Person>(id) {
                    public void onClick(AjaxRequestTarget target) {
                        getConfirmationDialog().open(
                        		target, 
                        		getConsoleLabel("usersconsole.deleteconfirmation.message", getModel().getObject().getFirstLastName()), 
                        		Dialog.Delete, 
                        		new Dialog.Handler() {
                            @Override
                            public void onClick(AjaxRequestTarget target, Button button) {
                                if (button.key().equals(Dialog.Delete.key())) {
                                    try {
                                        executeDelete(target);
                                        UsersConsole.this.refresh(target);
                                    } 
                                    catch (Exception e) {
                                        logger.error(e);
                                        FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
                                    }
                                }
                            }
                        });
                        refresh(target);
                    }
                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.delete").getObject();
                    }
                    @Override
                    public boolean isEnabled() {
                        if (is_support && !is_root)
                            return false;
                        return ((getTargetUser() != null && 
                        	!getTargetUser().getName().startsWith("root@") && 
                        	!getTargetUser().getName().startsWith(DomainService.WORKFLOW_USER+"@") &&
                        	!getTargetUser().getName().startsWith(ReservedUsername.PUBLICRESOURCES.getUserName() + "@" ) &&
                        	!getTargetUser().isCanonical()) || getTargetUser() == null) && !isSupport();
                    }
                    public User getTargetUser() {
                        return (getModel().getObject().getProfile(UserProfile.class)).getUser();
                    }
                    @Override
                    public boolean isVisible() {
                        try {
                        	if (is_admin || is_security || is_federated_security) {
                        		return getModel().getObject().getState() != ObjectState.DELETED;
                        	}
                        	else {
                        		return false;
                        	}
                        } 
                        catch (Exception e) {
                            logger.error(e);
                            return false;
                        }
                    }
                    protected void executeDelete(AjaxRequestTarget target) {
                        try {
                            ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModel().getObject());
                        } 
                        catch (Exception e1) {
                            try {
                                logger.error(e1);
                                getModel().detach();
                                ServiceLocator.getService(SecurityContentMgmtService.class).markAsDeleted(getModel().getObject());
                            } 
                            catch (Exception e2) {
                                logger.error(e2, getSessionUserName());
                                setResponsePage(new ApplicationErrorPage<Void>(e2));
                            }
                        }
                    }
                };
            }
        });

        menu.addItem(new MenuItemFactory<Person>() {
            @Override
            public AbstractMenuItemPanelV5<Person> getItem(String id) {
                return new AjaxMenuItemPanelV5<Person>(id) {
                    public void onClick(AjaxRequestTarget target) {
                        try {
                            ServiceLocator.getService(SecurityContentMgmtService.class).restore(getModel().getObject());
                            FeedbackHelper.showInfoToast(getConsoleLabel("usersconsole.contextmenu.restore").getObject() + " ok");
                        } 
                        catch (Exception e) {
                            logger.error(e, getSessionUserName());
                            FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
                        }
                        UsersConsole.this.refresh(target);
                    }
                    @Override
                    public String getLabel() {
                        return getConsoleLabel("usersconsole.contextmenu.restore").getObject();
                    }
                    @Override
                    public boolean isVisible() {
                        try {
                            return getModel().getObject().getState() == ObjectState.DELETED;
                        } 
                        catch (Exception e) {
                            logger.error(e);
                            return false;
                        }
                    }
                };
            }
        });
        

        return menu;
    }


    protected BreadCrumb getBreadCrumb() {
        return new BreadCrumb(new DataSetBC(getDataSet()));
    }

    @Override
	protected String getIcon(IModel<Person> model) {
		return null;
	}
	
    protected  IModel<Person> getModel(Person object) {
		return new ObjectModel<Person>(object, true);
	}
    

    
	protected DateTimeService getDateTimeService() {
		return ServiceLocator.getService(DateTimeService.class);			
	}

    @Override
    protected boolean hasExpander() {
        return true;
    }

    
	public List<GridColumn<SearchResult, String>> getColumns() {

        if (this.columns != null)
            return this.columns;

        this.columns = new ArrayList<GridColumn<SearchResult, String>>();

 
        this.columns.add(new ObjectStatusColumn<Person>("iconstatus", 
        	getName(), 
        	getLabel("usersconsole.column.status-shrot")));

        GridColumn<SearchResult, String> iconc = new GridColumn<SearchResult, String>("userphoto", getLabel("usersconsole.column.photo")) {
        	@Override
        	public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, 
        			String componentId, 
        			IModel<SearchResult> resultmodel) {
        		populateItem(cellItem, componentId, resultmodel);
        	}
        	@Override
            public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, 
            		String componentId, 
            		IModel<SearchResult> resultmodel) {
                if (resultmodel.getObject() == null) {
                    cellItem.add(new Label(componentId, "null"));
                    return;
                }
                Object object = resultmodel.getObject().getObject();
                IModel<Person> objectmodel = getModel((Person) object);
                cellItem.add(new UserAvatarColumnPanel(componentId, objectmodel));
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
            public int getDefaultWidth() {
                return 72;
            }
            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }
        };

        iconc.setPreferred(true);
        this.columns.add(iconc);


        this.columns.add(new GridColumn<SearchResult, String>("mylists", getLabel("mylists")) {

            @Override
            public String getCssClass() {
                return super.getCssClass() + " mylist";
            }

            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    List<UserList> list = ((KbeeUser) getSessionUser()).getService(UserListService.class).getUserLists(UsersConsole.this.getName(), (Person) object.getObject());
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
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }

        });


        this.columns.add(new GridColumn<SearchResult, String>("lastname", getLabel("usersconsole.column.lastname"), "lastname_sort") {
        	@Override
            public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
                if (resultmodel.getObject() == null) {
                    cellItem.add(new Label(componentId, "null"));
                    return;
                }
                try {
                    PersonMember member = (PersonMember) resultmodel.getObject().getObject();
                    boolean can=((Person) member.getPerson()).getProfile(UserProfile.class).getUser().isCanonical();
                    cellItem.add(new Label(componentId, member.getLastName() +  (can? (" <span class=\"ago\"> (" + new StringResourceModel("system", UsersConsole.this, null).getObject()+" ) </span>" ): "") ));
                    
                } catch (Exception e) {
                    logger.error(e, resultmodel.getObject().toString());
                    cellItem.add(new Label(componentId, e.getClass().getName()));
                }
            }

        	
        	
        	
        	@Override
            public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
                if (resultmodel.getObject() == null) {
                    cellItem.add(new Label(componentId, "null"));
                    return;
                }
                try {
                    PersonMember member = (PersonMember) resultmodel.getObject().getObject();
                    cellItem.add(new ClassificableNameColumnPanel<DataSetMember>(componentId, new ObjectModel<DataSetMember>(member)) {
                        @Override
                        protected String getCss() {
                            return "cell-label btn-link";
                        }

                        @Override
                        protected String getDisplayProperty() {
                            return "lastName";
                        }
                    });
                } catch (Exception e) {
                    logger.error(e, resultmodel.getObject().toString());
                    cellItem.add(new Label(componentId, e.getClass().getName()));
                }
            }

            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                Person person = (Person) object.getObject();
                return () -> person.getLastName();
            }

            @Override
            public String getCssClass() {
                return "col title col-xs-1 col-md-1 col-lg-1";
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public int getDefaultWidth() {
                return 380;
            }
        });


        this.columns.add(new GridColumn<SearchResult, String>("firstname", getLabel("usersconsole.column.firstname")) {
            
        	@Override
        	public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
        		populateItem(cellItem, componentId, resultmodel);
        	}

        	
        	@Override
            public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {

                if (resultmodel.getObject() == null || resultmodel.getObject().getObject() == null) {
                    cellItem.add(new Label(componentId, "null"));
                    return;
                }

                Object object = resultmodel.getObject().getObject();
                IModel<Person> objectmodel = getModel((Person) object);
                cellItem.add(new NameColumnPanel<Person>(componentId, objectmodel) {
                    @Override
                    protected String getCss() {
                        return "cell-label btn-link";
                    }

                    @Override
                    protected String getDisplayProperty() {
                        return "firstName";
                    }
                });
            }

            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                Person person = (Person) object.getObject();
                return () -> person.getFirstName();
            }

            @Override
            public String getCssClass() {
                return "col title col-xs-1 col-md-1 col-lg-1";
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }

        });


        this.columns.add(new LastModifiedColumn<Person>("modified", getLabel("usersconsole.column.modified"), "modified") {
                             private static final long serialVersionUID = 1L;

                             @Override
                             protected String getContextKey() {
                                 return UsersConsole.this.getName() + super.getContextKey();
                             }
                         }
        );

        this.columns.add(new GridColumn<SearchResult, String>("modifiedby", getLabel("usersconsole.column.modifieduser")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    return new Model<String>(String.valueOf(((Person) object.getObject()).getLastModifiedUser().getFirstLastName()));
                } catch (Exception e) {
                    logger.error(e, getSessionUserName());

                    return new Model<String>(e.getClass().getSimpleName());
                }
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });

        
        this.columns.add(new GridColumn<SearchResult, String>("device", getLabel("usersconsole.column.device")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                	if (object==null || object.getObject()==null)
                		return new Model<String>("null");
                	Person person = (Person) object.getObject();
                	List<IModel<UserDevice>> list = getDevices(person.getProfile(UserProfile.class));
                	StringBuilder str = new StringBuilder();
                	list.forEach( item -> { 
                		if (item.getObject().getState()==ObjectState.ENABLED) {
	                		if (item.getObject().getDescription()!=null)
	                			str.append( (str.length()>0?" - " : "" ) + item.getObject().getDescription());
	                		if (item.getObject().getNumber()!=null)
	                			str.append( (str.length()>0?" - " : "" ) + item.getObject().getNumber());
                		}
                	});
                    return new Model<String>(str.toString());
                    
                } catch (Exception e) {
                    logger.error(e, getSessionUserName());
                    return new Model<String>(e.getClass().getSimpleName());
                }
            }
            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }
            @Override
            public boolean isPreferred() {
                return false;
            }
        });
        
        

        this.columns.add(new GridColumn<SearchResult, String>("user", getLabel("usersconsole.column.username"), "username") {
            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                try {
                    return new Model<String>(((Person) result.getObject()).getProfile(UserProfile.class).getUser().getUserName());
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getSimpleName());
                }
            }
        	@Override
        	public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
        		populateItem(cellItem, componentId, resultmodel);
        	}
            @Override
            public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
                if (resultmodel.getObject() == null || resultmodel.getObject().getObject() == null) {
                    cellItem.add(new Label(componentId, "null"));
                    return;
                }
                Object object = resultmodel.getObject().getObject();
                IModel<Person> objectmodel = getModel((Person) object);
                cellItem.add(new NameColumnPanel<Person>(componentId, objectmodel) {
                    @Override
                    protected String getCss() {
                        return "cell-label btn-link";
                    }

                    @Override
                    protected IModel<String> getStringResourceModelName() {
                        UserProfile userProfile = getModelObject().getProfile(UserProfile.class);
                        User user = userProfile.getUser();
                        if (user == null)
                            return new Model<String>("err");
                        return new Model<String>(user.getUserName());
                    }
                });
            }
            @Override
            public String getCssClass() {
                return "col title col-xs-1 col-md-1 col-lg-1";
            }
            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }
        });


        for (Classifier classifier : getDataSet().getClassifiers()) {
            ClassifierColumn<PersonMember> co = new ClassifierColumn<PersonMember>(new ObjectModel<Classifier>(classifier), this.getName());
            co.setPreferred(!(classifier.getDataSet().getDataSetType() == DataSetType.EXTERNAL || classifier.getDataSet().getDataSetType() == DataSetType.LABEL));
            this.columns.add(co);
        }
        
        for (AttributeTemplate template : getDataSet().getAttributes()) {
            AttributeColumn co = new AttributeColumn(new ObjectModel<Attribute>(template.getAttribute()), this.getName());
            co.setPreferred(false);
            this.columns.add(co);
        }
        
        
        this.columns.add(new GridColumn<SearchResult, String>("profileType", getLabel("usersconsole.column.profiletype")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                	if (object==null || object.getObject()==null)
                		return new Model<String>("null");
                	Person person = (Person) object.getObject();
                	UserProfileType type = person.getProfile(UserProfile.class).getType();
                	String label = type!=null
                		? type.getLabel(getSessionUser().getLocale())
                		: null;
                    return new Model<String>(label);
                } 
                catch (Exception e) {
                    logger.error(e, getSessionUserName());
                    return new Model<String>(e.getClass().getSimpleName());
                }
            }
            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }
            @Override
            public boolean isPreferred() {
                return false;
            }
        });

        this.columns.add(new GridColumn<SearchResult, String>("status", getLabel("usersconsole.column.status"), "statename_sort") {

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                String status = "err";
                UserProfile userprofile = ((Person) result.getObject()).getProfile(UserProfile.class);
                if (userprofile != null && userprofile.getUser() != null) {
                    KbeeUser user = (KbeeUser) userprofile.getUser();
                    status = user.getState().getLabel(getSessionUser().getLocale());
                }
                return new Model<String>(status);
            }

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                UserProfile userprofile = ((Person) result.getObject()).getProfile(UserProfile.class);
                if (userprofile != null && userprofile.getUser() != null) {
                    KbeeUser user = (KbeeUser) userprofile.getUser();
                    String css = "";
                    String status = getCellAsString(result).getObject();
                    if (ObjectState.DELETED.equals(user.getState())) {
                        css = user.getState().getCss();
                    } else {
                        if (user.isEnabled()) {
                            css = ObjectState.ENABLED.getCss();
                        } else {
                            css = ObjectState.ARCHIVED.getCss();
                        }
                    }

                    return new Model<String>(
                            "<span class=\"" + css + "\">" +
                                    status +
                                    "</span>");
                } else {
                    return new Model<String>("err");
                }
            }

            @Override
            protected String getLabelCss() {
                return null;
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isEscapeModelString() {
                return false;
            }
        });


        this.columns.add(new GridColumn<SearchResult, String>("email", getLabel("usersconsole.column.email"), "email") {
            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                return new Model<String>(((Person) result.getObject()).getEmail());
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }
        });


        this.columns.add(new GridColumn<SearchResult, String>("phone", getLabel("usersconsole.column.phone")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    return new Model<String>(((Person) result.getObject()).getPhone());
                } catch (Exception e) {
                    return new Model<String>(e.getClass().getSimpleName());
                }


            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }

        });


        this.columns.add(new GridColumn<SearchResult, String>("emailnotistatus", getLabel("usersconsole.column.emailnotistatus")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    return new StringResourceModel((((Person) result.getObject()).getProfile(UserProfile.class).isEmailNotifications() ? "yes" : "no"), UsersConsole.this, null);
                } catch (Exception e) {
                    return new Model<String>(e.getClass().getSimpleName());
                }

            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                Person person = (Person) result.getObject();
                return new Model<String>(person.getProfile(UserProfile.class).isEmailNotifications() ? "yes" : "no");
            }

            @Override
            protected String getLabelCss(IModel<SearchResult> model) {
                return "centered";
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return true;
            }

            @Override
            public String getRowCssClass() {
                return "centered";
            }

            @Override
            public String getHeaderCssClass() {
                return "centered";
            }

        });


        this.columns.add(new GridColumn<SearchResult, String>("email-pending", getLabel("usersconsole.column.email-pending")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    return new StringResourceModel((((Person) result.getObject()).getProfile(UserProfile.class).isEmailPendingNotifications() ? "yes" : "no"), UsersConsole.this, null);
                } catch (Exception e) {
                    return new Model<String>(e.getClass().getSimpleName());
                }

            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                Person person = (Person) result.getObject();
                return new Model<String>(person.getProfile(UserProfile.class).isEmailPendingNotifications() ? "yes" : "no");
            }

            @Override
            protected String getLabelCss(IModel<SearchResult> model) {
                return "centered";
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return true;
            }

            @Override
            public String getRowCssClass() {
                return "centered";
            }

            @Override
            public String getHeaderCssClass() {
                return "centered";
            }

        });


        this.columns.add(new GridColumn<SearchResult, String>("emailnoti", getLabel("usersconsole.column.emailnoti")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    List<ENotiRule> list = getEmailRules(((Person) result.getObject()).getProfile(UserProfile.class).getUser());
                    StringBuilder str = new StringBuilder();
                    for (ENotiRule rule : list) {
                        if (str.length() > 0)
                            str.append(" <span class=\"ago\"> | </span>");
                        str.append(rule.getEventTypeStr(getSessionUser().getLocale()) + " < " + rule.getDescription());
                    }
                    return new Model<String>(str.toString());

                } catch (Exception e) {
                    logger.error(e, getSessionUserName());

                    return new Model<String>(e.getClass().getSimpleName());
                }
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });


        this.columns.add(new GridColumn<SearchResult, String>("timezone", getLabel("usersconsole.column.timezone")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    return new Model<String>(((Person) result.getObject()).getProfile(UserProfile.class).getUser().getTimeZone());
                } catch (Exception e) {
                    logger.error(e, getSessionUserName());

                    return new Model<String>(e.getClass().getSimpleName());
                }
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }
        });

       
        this.columns.add(new GridColumn<SearchResult, String>("accessvaliditydate", getLabel("accessvaliditydate")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                try {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    
                    User user = ((Person) result.getObject()).getProfile(UserProfile.class).getUser();
                    
                    if (user==null)
						return new Model<String>("err");
                    
                    
                    OffsetDateTime date = user.getValidityAccessDate();
                    
                    if (date==null)
                    	return new Model<String>("");
                    
                    	
                    String zid=getSessionUser().getZoneId().getId();
                    String s= getDateTimeService().format(date, zid, getSessionUser().getLocale(), DateTimeService.Month_Day_Year);
                    
                     String sty="color: #31708f; background-color: #d9edf7; border-color: #bce8f1; padding: 5px 10px;";
                    return new Model<String>("<span style=\"" +sty+"\">" + s + "</span>");
                    
                } catch (Exception e) {
                    logger.error(e, getSessionUserName());

                    return new Model<String>(e.getClass().getSimpleName());
                }
            }

            
            public IModel<String> getCellAsString(SearchResult result) {
            	 if (result.getObject() == null)
                     return new Model<String>("err");
                 
                 User user = ((Person) result.getObject()).getProfile(UserProfile.class).getUser();
                 
                 if (user==null)
						return new Model<String>("err");
                 
                 
                 OffsetDateTime date = user.getValidityAccessDate();
                 
                 if (date==null)
                 	return new Model<String>("");
                 	
                 String zid=getSessionUser().getZoneId().getId();
                 String s= getDateTimeService().format(date, zid, getSessionUser().getLocale(), DateTimeService.Month_Day_Year);
                 return new Model<String>(s);
        	}
            
            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }
        });
        
        
        

        if (isRoot()) {
            this.columns.add(new GridColumn<SearchResult, String>("generalpermissions", new Model<String>(getLabel("usersconsole.column.generalpermissions").getObject() + " <span class=\"only-root\">(root)</span>")) {
                @Override
                protected IModel<String> getLabelModel(SearchResult result) {
                    return getStringIModel(result, true);

                }

                @Override
                public IModel<String> getCellAsString(SearchResult result) {
                    return getStringIModel(result, false);
                }


                private IModel<String> getStringIModel(SearchResult result, boolean html) {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    try {
                        Set<Group> set = ((Person) result.getObject()).getProfile(UserProfile.class).getUser().getGroups();

                        StringBuilder str = new StringBuilder();

                        for (Group g : set) {
                            if (g.isCanonical()) {
                                if (str.length() > 0) {
                                    if (html)
                                        str.append("<span class=\"separator\"> | </span>");
                                    else
                                        str.append(" | ");
                                }
                                str.append(g.getDisplayName());
                            }
                        }
                        return new Model<String>(str.toString());
                    } catch (Exception e) {
                        logger.error(e, getSessionUserName());

                        return new Model<String>(e.getClass().getName());
                    }
                }

                @Override
                protected String getContextKey() {
                    return UsersConsole.this.getName() + super.getContextKey();
                }

                @Override
                public boolean isPreferred() {
                    return false;
                }
            });
        }


        /**
         * ROLES
         *
         */
        this.columns.add(new GridColumn<SearchResult, String>("roles", getLabel("usersconsole.column.roles")) {

            @Override
            protected IModel<String> getLabelModel(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    List<UserRole> set = ((Person) result.getObject()).getProfile(UserProfile.class).getRoles();
                    StringBuilder str = new StringBuilder();

                    for (UserRole g : set) {
                        String href = "/security/roles/" + g.getRole().getId().toString();
                        String name = g.getRole().getName();
                        String li;

                        if ((g.getRole() instanceof KbeeAbstractRole) && ((KbeeAbstractRole) g.getRole()).getType() == DomainRole.TYPE) {
                            li = "<span><a class=\"btn-link\" target=\"_blank\"  href=\"" + href + "\">" + name + "</a></span>";

                        } else if ((g.getRole() instanceof KbeeAbstractRole) && g.getRole().getType() == EntityRole.TYPE) {
                            String enty = g.getEntity() != null ? g.getEntity().getDisplayName() : "";
                            li = "<span><a class=\"btn-link\" target=\"_blank\"  href=\"" + href + "\">" + name + "</a><span class=\"ago\"> (" + enty + ") </span></span>";
                        } else
                            li = g.getClass().getSimpleName() + " not supported | role: " + g.getRole().getClass().getName();

                        if (str.length() > 0)
                            str.append("<span class=\"separator\">|</span>");
                        str.append(li);
                    }

                    return new Model<String>(str.toString());
                } catch (Exception e) {
                    logger.error(e, getSessionUserName());
                    return new Model<String>(e.getClass().getName());
                }
            }


            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    List<UserRole> set = ((Person) result.getObject()).getProfile(UserProfile.class).getRoles();
                    StringBuilder str = new StringBuilder();
                    for (UserRole g : set) {
                        String name = g.getRole().getName();
                        if (str.length() > 0)
                            str.append(" | ");

                        String description;
                        if (g.getRole() instanceof KbeeAbstractRole && ((KbeeAbstractRole) g.getRole()).getType() == DomainRole.TYPE) {
                            description = name;
                        } else if (g.getRole() instanceof KbeeAbstractRole && g.getRole().getType() == EntityRole.TYPE) {
                            String enty = g.getEntity() != null ? g.getEntity().getDisplayName() : "";
                            description = name + " (" + enty + ")";
                        } else
                            description = g.getClass().getSimpleName() + " not supported";

                        str.append(description);
                    }
                    return new Model<String>(str.toString());
                } catch (Exception e) {
                    logger.error(e, result.toString());
                    return new Model<String>(e.getClass().getName());
                }
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });

        if (isRoot()) {
            this.columns.add(new GridColumn<SearchResult, String>("rules", new Model<String>(getLabel("usersconsole.column.rules").getObject() + " <span class=\"only-root\">(root)</span>")) {
                @Override
                protected IModel<String> getLabelModel(SearchResult result) {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    try {
                        User user = ((Person) result.getObject()).getProfile(UserProfile.class).getUser();
                        List<SecurityRule> list = getContentSecurityDao().getRules(user);
                        StringBuilder str = new StringBuilder();
                        for (SecurityRule rule : list) {
                            String href = "/security/rules/" + String.valueOf(rule.getId());
                            String name = rule.getDisplayName();
                            String li = "<span><a class=\"btn-link\" target=\"_blank\"  href=\"" + href + "\">" + name + "</a></span>";
                            if (str.length() > 0)
                                str.append("<span class=\"separator\">|</span>");
                            str.append(li);
                        }
                        return new Model<String>(str.toString());

                    } catch (Exception e) {
                        return new Model<String>(e.getClass().getSimpleName());
                    }
                }

                @Override
                public IModel<String> getCellAsString(SearchResult result) {
                    if (result.getObject() == null)
                        return new Model<String>("err");
                    try {
                        User user = ((Person) result.getObject()).getProfile(UserProfile.class).getUser();
                        List<SecurityRule> list = getContentSecurityDao().getRules(user);
                        StringBuilder str = new StringBuilder();
                        for (SecurityRule rule : list) {
                            String name = rule.getDisplayName();
                            if (str.length() > 0)
                                str.append(" | ");
                            str.append(name);
                        }
                        return new Model<String>(str.toString());
                    } catch (Exception e) {
                        return new Model<String>(e.getClass().getSimpleName());
                    }

                }

                @Override
                protected String getContextKey() {
                    return UsersConsole.this.getName() + super.getContextKey();
                }

                @Override
                public boolean isPreferred() {
                    return false;
                }

                // only in HitPanel
                @Override
                public boolean isOnlyForExpandedHitPanel() {
                    return true;
                }

            });
        }
        {
            
        	final KbeePredicateGridColumn<Person> activeSessionsColumn = new KbeePredicateGridColumn<>("activeSessions", 
        			getLabel("usersconsole.column.activeSessions"),
                    person -> NumberFormatter.formatNumber(
                    	ServiceLocator.getService(WebSessionService.class)
                    	.countUserActiveSessions(person
                    			.getProfile(UserProfile.class)
                    			.getUser()
                    			.getUserName()), 
                    			getSessionUser().getLocale()) 
            );

        	activeSessionsColumn.setCssValueResolver(p -> 
            ServiceLocator.getService(WebSessionService.class).countUserActiveSessions(p.getProfile(UserProfile.class).getUser().getUserName()) > 0 ? "col col-xs-1 col-md-1 col-lg-1 number-md info" : "col col-xs-1 col-md-1 col-lg-1 number-md");
            this.columns.add(activeSessionsColumn);
        }
        
        

        this.columns.add(new SimpleDateColumn<Person>("lastlogin", getLabel("usersconsole.column.lastlogin"), null) {
            @Override
            protected OffsetDateTime getOffsetDateTime(Person person) {
                OffsetDateTime dt = person.getProfile(UserProfile.class).getLastLoginDate();
                return dt;
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {


                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    Person person = (Person) result.getObject();
                    OffsetDateTime dt = getOffsetDateTime(person);
                    if (dt != null) {
                        return getStringDateModel(dt, false);
                    }
                    return new Model<>("");
                } catch (Exception e) {
                    return new Model<String>(e.getClass().getSimpleName());
                }

            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return true;
            }

        });


        this.columns.add(new SimpleDateColumn<Person>("lastpasswrodchange", getLabel("usersconsole.column.lastpasswordchange"), null) {
            @Override
            protected OffsetDateTime getOffsetDateTime(Person person) {
                OffsetDateTime dt = person.getProfile(UserProfile.class).getUser().getPasswordLastModifiedDate();
                return dt;
            }

            @Override
            public IModel<String> getCellAsString(SearchResult result) {
                if (result.getObject() == null)
                    return new Model<String>("err");
                try {
                    Person person = (Person) result.getObject();
                    OffsetDateTime dt = getOffsetDateTime(person);
                    if (dt != null) {
                        return getStringDateModel(dt, false);
                    }
                    return new Model<>("");
                } catch (Exception e) {
                    return new Model<String>(e.getClass().getSimpleName());
                }

            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });


        this.columns.add(new GridColumn<SearchResult, String>("id", getLabel("usersconsole.column.id")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                try {
                    return new Model<String>(String.valueOf(((Person) object.getObject()).getProfile(UserProfile.class).getUser().getId()));
                } catch (Exception e) {
                    logger.error(e);
                    return new Model<String>(e.getClass().getSimpleName());
                }
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return true;
            }
        });

        // -------------------------------------
        // external id
        this.columns.add(new GridColumn<SearchResult, String>("externalid", getLabel("usersconsole.column.externalid")) {
            @Override
            protected IModel<String> getLabelModel(SearchResult object) {
                String id = ((DataSetMember) object.getObject()).getExternalId();
                if (id == null) id = "-";
                return new Model<String>(id);
            }

            @Override
            protected String getContextKey() {
                return UsersConsole.this.getName() + super.getContextKey();
            }

            @Override
            public boolean isPreferred() {
                return false;
            }
        });

        return this.columns;
    }


    protected IModel<String> getStringDateModel(OffsetDateTime dt) {
        if (dt == null)
            return new Model<String>("err");
        DateTimeService service = ServiceLocator.getService(DateTimeService.class);
        ZonedDateTime zd = ZonedDateTime.ofInstant(dt.toInstant(), user_zoneid);
        return new Model<String>(service.timeElapsed(zd, user_zoneid, user_locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago"));
    }


    protected Page getPage(IModel<Person> model, long index, boolean edition) {
    	try {
	        Searcher searcher = getSearcher();
	        SolrCursor soc = new SolrCursor((SolrResultSet) searcher.getResultSet(), index);
	        UserPage page = new UserPage(model, new SolrCursorModel(soc));
	        page.setEditionEnabled(edition);
        return page;
    	} catch (Exception e) {
    		logger.error(e);
    		return new ApplicationErrorPage<Person>(e);
    	}
        

    }


    @SuppressWarnings("serial")
    protected Panel getNavigationPanel(long index) {

        GlobalNavigationBar<Person> navigationbar = new GlobalNavigationBar<Person>("navigation", getDisplayName().getObject()) {
            @Override
            public void onNavigate(Person person) {
                throw new KbeeRuntimeException("onNavigate(Person person) deprecated");
                //setResponsePage(new UserPage(getModel(person), this, false));
            }

            @Override
            public void onReturn() {
                setResponsePage(getConsolePage(getQuery(), -1));
            }

            @Override
            protected void onSearch(AjaxRequestTarget target, String text) {
                getQuery().getParameters().put("text", text);
                getQuery().getParameters().put("sort", "relevance");
                setResponsePage(getConsolePage(getQuery(), -1));
            }
        };
        navigationbar.setHasSearchPanel(false);
        navigationbar.setSearchPlaceHolder(new StringResourceModel("searchplaceholder", UsersConsole.this, null).getString());
        return navigationbar;
    }


    @Override
    protected Panel getPanel(IModel<Person> model, List<String> snippets) {
    		return new ExpandedPanel<Person>("editor", this, model, null);
    }

    @Override
    protected Panel getPanel(IModel<Person> model) {
        return new ExpandedPanel<Person>("editor", this, model);
    }

    @SuppressWarnings("serial")
    @Override
    protected void addListeners() {
        super.addListeners();

		add(new WicketEventListener<GridPanelNullObjectEvent<?>>() {
			@Override
			public void onEvent(GridPanelNullObjectEvent<?> event) {
				try {
					ServiceLocator.getService(AppMonitoringService.class).attempToFixSecurityIndex();
				} catch (Exception e) {
					logger.error(e);
				}
			}
		});

        
        /**
         * apply list
         */

        add(new WicketEventListener<MyListsApplyUserListEvent>() {
            @Override
            public void onEvent(MyListsApplyUserListEvent event) {
                IModel<UserList> list = event.getUserList();
                setQuery(new UsersUserListQuery(list.getObject(), getQueryIndex(), getDataSet(), isDeletedVisible()));

                FiltersPanel panel = getBrowser().getPanel(FiltersPanel.class);
                panel.getParameters().put("userlist", new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName()));
                panel.setParameters(panel.getParameters());
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
         * add object to List
         */
        add(new WicketEventListener<MyListsUserListItemUpdateObjectEvent<Person>>() {
            @Override
            public void onEvent(MyListsUserListItemUpdateObjectEvent<Person> event) {
				FeedbackHelper.showInfoToast(event.getListModel().getObject().getName(),  event.getModel().getObject().getDisplayName());
            	UsersConsole.this.refresh(event.getRequestTarget());
            }

            @Override
            public boolean handle(com.novamens.event.Event event) {
                return event instanceof MyListsUserListItemUpdateObjectEvent;
            }
        });


        add(new WicketEventListener<com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onEvent(SidePanelEvent event) {
                // event.getRequestTarget().add(get("header"));
            }
        });


        add(new WicketEventListener<ClickEvent<Person>>() {
            @Override
            public void onEvent(ClickEvent<Person> event) {
                setResponsePage(UsersConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false));
            }
        });

        add(new WicketEventListener<UsersBatchSetRoleButtonEvent>() {
            @Override
            public void onEvent(UsersBatchSetRoleButtonEvent event) {
                UsersConsole.this.onOpenSetRoles(event.getRequestTarget());
            }
        });

        add(new WicketEventListener<ClickResetPasswordEvent>() {
            @Override
            public void onEvent(ClickResetPasswordEvent event) {
                UsersConsole.this.onOpenResetPassword(event.getRequestTarget());
            }
        });

        add(new WicketEventListener<ClickSetGroupEvent>() {
            @Override
            public void onEvent(ClickSetGroupEvent event) {
                UsersConsole.this.onOpenSetGroups(event.getRequestTarget());
            }
        });


        add(new WicketEventListener<LabelEvent>() {
            @Override
            public void onEvent(LabelEvent event) {
            	FeedbackHelper.showInfoToast("Label");
            	UsersConsole.this.refresh(event.getRequestTarget());
                
            }

            @Override
            public boolean handle(com.novamens.event.Event event) {
                return event instanceof LabelEvent;
            }
        });


    }


    /**
     * Selected Users
     * <p>
     * Bulk Actions
     * ------------
     */
    @Override
    protected List<ToolbarItem> getSelectionToolbarItems(BaseBrowser<Person> browser) {

        if (this.selection_toolbar != null)
            return this.selection_toolbar;

        this.selection_toolbar = new ArrayList<ToolbarItem>();

        this.selection_toolbar.add(new UsersBatchPasswordChangeButton(getBrowser(), Align.TOP_LEFT, true));
        this.selection_toolbar.add(new UsersBatchSetGlobalRoleButton(getBrowser(), Align.TOP_LEFT, true));
        
        /**
         * 	Delete
         */
        this.selection_toolbar.add(new AjaxToolbarButton(browser, ToolbarItem.Align.TOP_LEFT) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public boolean isVisible() {
                return true;
            }

            protected String getLabelStr() {
                return new StringResourceModel("delete", UsersConsole.this).getObject();
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    UsersConsole.this.delete(getBrowser().getSelection());
                    UsersConsole.this.resetSelection();
                    FeedbackHelper.showInfoToast(new StringResourceModel("delete", UsersConsole.this).getObject());
                    UsersConsole.this.refresh(target);

                } catch (Exception e) {
                	FeedbackHelper.showErrorToast(e.getClass().getSimpleName(), e.getMessage());
                    logger.error(e);
                }
            }
        });

        return this.selection_toolbar;
    }

    
    
    /**
     * 
     * 
     * Toolbar Actions
     */
    @Override
    protected List<ToolbarItem> getToolbarItems(BaseBrowser<Person> browser) {

        if (items != null)
            return items;

        this.items = super.getToolbarItems(browser);

        items.add(new NewUserButton(browser, ToolbarItem.Align.TOP_LEFT) {
        	@Override
            public void onClick(AjaxRequestTarget target) {
                int max_users = getDomain().getMaxUsers();
                long total_users = getDomainMetricsServices().getUsers(getDomain());
                if (max_users < 1 || max_users > total_users) {
                    Page page = new NewUserPage(new Model<NewUserData>(new NewUserData()), null);
                    setResponsePage(page);
                } else {
                    StringResourceModel str = new StringResourceModel("quotalimit", UsersConsole.this, null);
                    getErrorDialog().open(target, str);
                    refresh(target);
                }
            }
        });

        items.add(new BulkCreationButton(browser, ToolbarItem.Align.TOP_LEFT) {
        	@Override
            public void onClick() {
                Page page = new UserBulkCreationPage();
                setResponsePage(page);
            }
            @Override
            public boolean isVisible() {
                return isRoot() && !isFreeVersion();
            }

        });


        InfoButton infoButton = new InfoButton(browser, ToolbarItem.Align.TOP_RIGHT) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                InfoDialog infoDialog = (InfoDialog) getInformationModal();
                infoDialog.open(target, () -> {
                    return UsersConsole.this.getName();
                }, new Model<String>(UsersConsole.this.getDescription()));
            }

            @Override
            public boolean isVisible() {
                return true;
            }
        };

        items.add(infoButton);

        return items;
    }

    /**
     * Users
     * Groups
     * Rules
     * Content
     * Monitor
     * Email
     * Activity
     * Resources
     */
    @Override
    protected Panel getTopPanel() {
        return new AdvancedSearchUserSelectorPanel("top");
    }

    @Override
    protected boolean hasTopPanel() {
        return true;
    }


    @Override
    protected void addModals() {
        super.addModals();
        replace(new ObjectAuditModal<User>("audit-trail-modal"));

        // reset password
        Modal modal = new Modal("reset-password-modal");
        modal.setTitle("reset-password-modal.title");
        modal.setOutputMarkupId(true);
        modal.setModalType(Modal.MODAL_CENTER);
        add(modal);


        // set groups
        Modal gmodal = new Modal("set-groups-modal");
        gmodal.setTitle("set-groups-modal.title");
        gmodal.setOutputMarkupId(true);
        gmodal.setModalType(Modal.MODAL_CENTER);
        add(gmodal);
    }


    /**
     * we show deleted users in yellow bck
     */
    @Override
    protected String getRowContainerCss(IModel<SearchResult> rowmodel) {
        try {
            UserProfile userprofile = ((Person) rowmodel.getObject().getObject()).getProfile(UserProfile.class);
            if (userprofile != null && userprofile.getUser() != null) {
                KbeeUser user = (KbeeUser) userprofile.getUser();
                if (user.getState() == ObjectState.DELETED) return "deleted-state";
                if (user.getState() == ObjectState.ARCHIVED) return "archived-state";
            }
            return null;

        } catch (Exception e) {
            logger.error(e, getSessionUserName());
            return null;
        }
    }

    protected void setDeletedVisible(boolean b) {
        this.is_deleted_visible = b;
        setUserPreference("deleted-visible", (b ? "yes" : "no"));
    }

    protected boolean isDeletedVisible() {
        return this.is_deleted_visible;
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
                    /**
                     *
                     */
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getLabel() {
                        return new StringResourceModel("show-deleted", UsersConsole.this, null).getObject();
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target) throws Exception {
                        UsersConsole.this.setDeletedVisible(!UsersConsole.this.isDeletedVisible());
                        boolean deleted_visible = UsersConsole.this.isDeletedVisible();
                        String states = "[" + String.valueOf(ObjectState.ENABLED.getId()) +
                                ", " + String.valueOf(ObjectState.ARCHIVED.getId()) +
                                (deleted_visible ? (", " + String.valueOf(ObjectState.DELETED.getId())) : "") + "]";
                        UsersConsole.this.getSearcher().getQuery().getParameters().put("state", states);
                        UsersConsole.this.refresh(target);
                    }

                    @Override
                    public boolean isIconVisible() {
                        return UsersConsole.this.isDeletedVisible();
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


    private void onOpenSetGroups(AjaxRequestTarget target) {
        UsersBatchSetGlobalPermissionPanel panel = new UsersBatchSetGlobalPermissionPanel("body", new ObjectModel<Domain>(getDomain()), getBrowser()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void close(AjaxRequestTarget target) {
                UsersConsole.this.refresh(target);
                target.add(getPage());
            }
        };

        ((Modal) get("set-groups-modal")).open(target, panel, new Modal.Handler() {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unused")
            public void onClick(AjaxRequestTarget target, Button button) {
                UsersConsole.this.refresh(target);
                target.add(getPage());
            }
        });
    }

    private void onOpenSetRoles(AjaxRequestTarget target) {
        UsersBatchSetGlobalRolePanel panel = new UsersBatchSetGlobalRolePanel("body", new ObjectModel<Domain>(getDomain()), getBrowser()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void close(AjaxRequestTarget target) {
                UsersConsole.this.refresh(target);
                target.add(getPage());
                // refresh(target);
            }

            public void cancel(AjaxRequestTarget target) {
                target.add(getPage());

            }
        };

        ((Modal) get("set-groups-modal")).open(target, panel, new Modal.Handler() {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unused")
            public void onClick(AjaxRequestTarget target, Button button) {
                UsersConsole.this.refresh(target);
                refresh(target);
            }
        });
    }

    private void onOpenResetPassword(AjaxRequestTarget target) {
        UsersBatchPasswordChangePanel panel = new UsersBatchPasswordChangePanel("body", new ObjectModel<Domain>(getDomain())) {
            private static final long serialVersionUID = 1L;

            public void update(AjaxRequestTarget target) {
                executeResetPwd(getPassword());
                UsersConsole.this.refresh(target);
                target.add(getPage());

            }

            public void cancel(AjaxRequestTarget target) {
                target.add(getPage());

            }
        };


        ((Modal) get("reset-password-modal")).open(target, panel, new Modal.Handler() {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unused")
            public void onClick(AjaxRequestTarget target, Button button) {
                target.add(getPage());
            }
        });
    }

    private void executeResetPwd(String pwd) {

        @SuppressWarnings({"rawtypes", "unchecked"})
        List<IModel<Person>> list = (List) getBrowser().getSelection();

        List<String> strupdated = new ArrayList<String>();
        strupdated.add("Bulk reset password");

        int counter = 0;
        StringBuilder str = new StringBuilder();
        str.append("New Password is: " + pwd + "<br/>");
        str.append("Total: " + String.valueOf(list.size()) + " User" + (list.size() > 1 ? "s" : "") + " <br/><br/>");

        logger.debug(str.toString());

        boolean overf = false;

        if (list.size() > 0) {
            for (IModel<Person> itm : list) {

                Person person = (Person) itm.getObject();
                KbeeUser user = (KbeeUser) person.getProfile(UserProfile.class).getUser();


                if (!user.getUserName().startsWith("root@")) {
                    user.setPassword(pwd);
                    if (counter < 1000) {
                        str.append(person.getLastFirstName() + "  (" + user.getUserName() + ")<br/>");
                    } else {
                        if (!overf) {
                            str.append("... and more ...");
                            overf = true;
                        }
                    }
                    try {
                        ServiceLocator.getService(SecurityContentMgmtService.class).update(person.getProfile(UserProfile.class), strupdated);
                        counter++;
                    } catch (ContentMgmtException e) {
                        logger.error(e);
                    }
                } else {
                    str.append(person.getLastFirstName() + "  (" + user.getUserName() + ") | Error: It is not allowed to change root password here<br/>");
                }
            }
        }

        String fromemail = getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_NO_REPLY);
        String toemail = getPerson().getEmail();


        /** we can not use the Scheduler here because this is not a transactional method */
        EmailData ed = new EmailData(fromemail, toemail, "Bulk Password Reset - " + (getDomain().getOrganization() != null ? getDomain().getOrganization() : getDomain().getName()), str.toString(), null, "Bulk Password Reset");

        EmailSendServiceRequest req = new EmailSendServiceRequest(ed, getDomain());
        logger.debug(ed);
        req.execute();

    }

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
                    if (a.getObject() != null && b.getObject().getDisplayName() == null)
                        return -1;
                    if (b.getObject() != null && a.getObject().getDisplayName() == null)
                        return -1;
                    return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
                } 
                catch (Exception e) {
                    return 0;
                }

            }
        });
        return this.labels;
    }

    @Override
    protected boolean isMyListsEnabled() {
        return true;
    }	
    
    protected boolean isDefaultTopPanelVisible() {
		return true;
	}
    
	protected boolean isVisible(Facet facet) {
		Facet realfacet;
		
		if (facet instanceof FacetWrapper) {
			boolean visible = ((FacetWrapper)facet).isVisible(getName());
			if (!visible) return false;
			realfacet = ((FacetWrapper)facet).getFacet();
		}
		else
			realfacet = facet;
		
		return !realfacet.getName().equals("state");	
	}
	
	@SuppressWarnings("unchecked")
	protected void delete(List<?> selection) {
        List<IModel<Person>> list = (List<IModel<Person>>) selection;
        for (IModel<Person> c : list) {
            try {
                delete(c);
            } 
            catch (Exception e) {
                logger.error(e);
            }
        }
    }

    protected void delete(IModel<Person> m) {
        try {
            // Delete Person
            ServiceLocator.getService(SecurityContentMgmtService.class).delete(m.getObject());
        } 
        catch (Exception e1) {
            try {
                logger.error(e1);
                m.detach();
                ServiceLocator.getService(SecurityContentMgmtService.class).markAsDeleted(m.getObject());
            } catch (Exception e2) {
                logger.error(e2, getSessionUserName());
            }
        }
    }


    private boolean isUserAdmin() {
        return ServiceLocator.getService(UserService.class).isUserAdmin();
    }
    
    private DomainMetricsService getDomainMetricsServices() {
        return ServiceLocator.getService(DomainMetricsService.class);
    }

    private ContentSecurityDao getContentSecurityDao() {
        return (ContentSecurityDao) ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
    }
    
    protected List<IModel<UserDevice>> getDevices(UserProfile um) {
		List<IModel<UserDevice>> devices = new ArrayList<IModel<UserDevice>>();
		for (UserDevice device : um.getDevices()) {
			if (device.getState()==ObjectState.ENABLED)
				devices.add(new ObjectModel<UserDevice>(device));
		}
		return devices;
	}
}   
package kbee.web.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.EntityRole;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserRole;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;

import kbee.email.EmailBuilderWorkflowTaskAssigned;
import kbee.util.logging.Logger;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.nav.DataSetMembersBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.MemberBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.page.ApplicationMenuSection;
import kbee.web.workflow.task.TaskPage;

@SuppressWarnings("serial")
public class DashboardEntityPage extends DashboardPage<Person> {
	private static final long serialVersionUID = 1L;

	static final public String PROPERTY_UNREAD = "unread";

	
	static final String KEY = "entity-home";

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardEntityPage.class.getName());

	
	private IModel<EntityMember> entitymodel;
	private IModel<Classifier> classifiermodel;

	public DashboardEntityPage() {
		add(new RefreshBehavior());
	}
	
	public DashboardEntityPage(PageParameters parameters) {
		add(new RefreshBehavior());
		setEntity(getEntity(parameters));
		setClassifier(getClassifier(parameters));
	}
	
	
	
	public void onDetach() {
		super.onDetach();
		
		if (entitymodel!=null)
			entitymodel.detach();
		
		if (classifiermodel!=null)
			classifiermodel.detach();
	}
	
	/**
	 * 
	 * @param entitymodel
	 * @param classifiermodel
	 * 
	 */
	public DashboardEntityPage(IModel<EntityMember> entitymodel, IModel<Classifier> classifiermodel) {
		super.setOutputMarkupId(true);

		getPageParameters().add("entity_id", entitymodel.getObject().getId().toString());
		getPageParameters().add("classifier_id", classifiermodel.getObject().getId().toString());

		setEntity(entitymodel.getObject());
		setClassifier(classifiermodel.getObject());
		
		add(new RefreshBehavior());
		
		
	}
	
	
	
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.MINI_SITE;
	}
	
	public EntityMember getEntity() {
		return entitymodel!=null ? entitymodel.getObject() : null;
	}
	
	public void setEntity(EntityMember entity) {
		entitymodel = entity!=null ? new ObjectModel<EntityMember>(entity) : null;
	}
	
	public IModel<EntityMember> getEntityModel() {
		return entitymodel;
	}
	
	public Classifier getClassifier() {
		return classifiermodel!=null ? classifiermodel.getObject() : null;
	}
	
	public void setClassifier(Classifier classifier) {
		classifiermodel = classifier!=null ? new ObjectModel<Classifier>(classifier) : null;
	}
	
	@Override
	public IModel<String> getTitle() {
		return getEntity()!=null ? new Model<String>(getClassifier().getDisplayName() + " "+ getEntity().getDisplayName()) : null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		getModalContainerMarkupContainer().add(new InvisiblePanel("audit-trail-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("send-email-modal"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("error-dialog"));
		getModalContainerMarkupContainer().add(new InvisiblePanel("confirmation-dialog"));

		
	}
	
	@Override
	protected boolean hasPermissions() {
		return getDomain()!=null && (role_admin || hasAnyRole());
	}
	
	@Override
	protected boolean hasError() {
		return getEntity()==null || getClassifier()==null;
	}
	
	protected boolean hasAnyRole() {
		for (UserRole userRole :  getUserProfile().getRoles()) {
			if (userRole.getRole().isEntity()) {
				EntityRole role = (EntityRole)getContentDao().unproxy(userRole.getRole());
				if (getClassifier().equals(role.getClassifier())) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected void addWidgets() {
		addWidget(new ListView<WidgetFactory>("widget-left", getLeftSectionsPanels()) {
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});	
		addWidget(new ListView<WidgetFactory>("widget-center", getCenterSectionsPanels()) {
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});	
		addWidget(new ListView<WidgetFactory>("widget-right", getRightSectionsPanels()) {
			protected void populateItem(ListItem<WidgetFactory> item){
				item.addOrReplace(getWidget(item.getModelObject()));
				item.detach();
			}
		});	
	}

	protected void onSiteClick(IModel<Site> modelObject) {
	}
	
	@SuppressWarnings("unchecked")
	protected void onClick(IModel<Content> model) {
		WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
		try {
			TaskPage<Content> page = null;
			if (workflowService.getTask()!=null && workflowService.getContext().getProcess().isRunning()) {
					Task task = workflowService.getTask();
					page = (TaskPage<Content>)((WebTask)task).getPage(workflowService.getContext());
					if (model.getObject().getWorkspace()>0) {
						if (getSessionUser().getId().toString().equals(model.getObject().getWorkspace().toString())) {
							
							page.setEditionEnabled(true);
							page.setReadOnly(false);
						}
						else {
							page.setEditionEnabled(false);
							page.setReadOnly(true);
						}
					}
					else {
						page.setEditionEnabled(false);
						page.setReadOnly(true);
					}
			}
			if (page==null)
				throw new IllegalArgumentException("page is null");
			setResponsePage(page);
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
			
		}
	}
	
	@Override
	protected Panel getBreadcrumbPanel() {
		MenuBreadCrumbPanel<Void>  bc = new MenuBreadCrumbPanel<Void>("breadcrumb");
		bc.addElement( new HomeBC());
		bc.addElement(new BCElement(new StringResourceModel("entity-mini-site", this, null)));
		DropDownMenuBC<?> dd = new DropDownMenuBC<>();
		dd.addElement( new BCElement(new Model<String>(getEntity().getDisplayName())));
		for (EntityMember entity : getEntities()) {
			dd.addElement(new MemberBC(entity) {
				public void onClick() {
					setResponsePage(new RedirectPage("/entityhome/"+String.valueOf(getMember().getId())+"/"+String.valueOf(getClassifier().getId())));
				}
			});
		};
		dd.addElement(new SeparatorBC());
		IModel<DataSet> m=new ObjectModel<DataSet>(getEntity().getDataSet());
		dd.addElement( new DataSetMembersBC(m,new Model<String>(new StringResourceModel("bc.datasetmembers",  DashboardEntityPage.this, null).getObject() + "  " + getEntity().getDataSet().getDisplayName())	)); 
		bc.addElement(dd);
		return bc;
	}
	
	@Override
	protected boolean isSectionHome() {
		return true;
	}
	
	/** -----------------------------------------------
	 * 
	 * 1st LEFT
	 * 
	 */
	private List<WidgetFactory> getLeftSectionsPanels() {

		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();
		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardMyEntityTasksWidgetPanel(id, getEntityModel(), DashboardHomePage.KEY);
			}
			public IModel<String> getLabel() {
				return DashboardEntityPage.this.getLabel("mytasks");
			}
		});
		
		/**
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardPendingEntityTasksWidgetPanel(id, getEntityModel(), DashboardHomePage.KEY);
			}
			public IModel<String> getLabel() {
				return DashboardEntityPage.this.getLabel("pending");
			}
		});
		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardMonitorEntityTasksWidgetPanel(id, getEntityModel(), DashboardHomePage.KEY);
			}
			public IModel<String> getLabel() {
				return DashboardEntityPage.this.getLabel("monitor");
			}
		});
		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardEntityLibraryWidgetPanel(id, getEntityModel(), DashboardHomePage.KEY);
			}
			public IModel<String> getLabel() {
				return DashboardEntityPage.this.getLabel("library");
			}
		});
		**/
		return widgets;
	}
	

	/**
 	 * 1st CENTER
	 * @return
	 */
	private List<WidgetFactory> getCenterSectionsPanels() {

		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();

		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardWidgetFileFactoryPanel(id, DashboardHomePage.KEY) {
					@Override
					protected boolean isEnabled(ProcessLauncher launcher) {
						if (!super.isEnabled(launcher)) return false;
						for (ClassifierTemplate template : launcher.getContentTemplate().getClassifiers()) {
							if (getClassifier().equals(template.getClassifier())) return true;
						}
						return false; 
					}
					@Override
					protected void onStart(Process process) {
						Content content = ((KbeeContext)process.getContext()).getContent();
						setEntity(content);
						setResponsePage( new RedirectPage(content.getService(UrlService.class).getUrl()));
					}
				};
			}	
			public IModel<String> getLabel() {
				return DashboardEntityPage.this.getLabel("factory");
			}
		});
		
		//if (role_dataset_members) {
			widgets.add(new WidgetFactory() {
				public MarkupContainer getWidget(String id) {
					return new DashboardRoles(id, getEntity(), getClassifier());
				}	
				public IModel<String> getLabel() {
					return DashboardEntityPage.this.getLabel("roles");
				}
			});
		//}
			
		return widgets;
	}
	
	/**
	 * 
 	 * 1st RIGHT

	 * @return
	 */
	private List<WidgetFactory> getRightSectionsPanels() {

		List<WidgetFactory> widgets = new ArrayList<WidgetFactory>();
		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardEntityWidgetPanel(id, getEntityModel());
			}	
			public IModel<String> getLabel() {
				return DashboardEntityPage.this.getLabel("datasetmembers");
			}
		});
		
		widgets.add(new WidgetFactory() {
			public MarkupContainer getWidget(String id) {
				return new DashboardEntityIndicatorsWidgetPanel(id, getEntityModel());
			}	
			public IModel<String> getLabel() {
				return DashboardEntityPage.this.getLabel("indicators");
			}
		});
		
//		widgets.add(new WidgetFactory() {
//			public MarkupContainer getWidget(String id) {
//				DashboardWidgetSimpleWrapperPanel<Person> wr = new DashboardWidgetSimpleWrapperPanel<Person>("panel", new ObjectModel<Person>(getPerson()), DashboardHomePage.KEY);
//				wr.setSimplePayloadPanel(new UserNotesPanel("payload", true));
//				wr.setHelpKey(InlineHelpWebService.HOME_NOTES);
//				wr.setTitle(DashboardEntityPage.this.getLabel("mainmenu.mynotes"));
//				LinkCellItem<Person> notes_l=new LinkCellItem<Person>("item", new ObjectModel<Person>(getPerson()), DashboardEntityPage.this.getLabel("mainmenu.mynotes")) {
//					@Override
//					public void onClick() {
//						setResponsePage(new UserNotesPage());
//					}
//				};
//				List<Panel> l_p =new ArrayList<Panel>();
//				l_p.add(notes_l);
//				DashboardSimpleBottomPanel db =new DashboardSimpleBottomPanel("base-bottom", l_p); 	
//				wr.setBottomPanel(db);
//				return wr;
//			}	
//			public IModel<String> getLabel() {
//				return DashboardEntityPage.this.getLabel("mainmenu.mynotes");
//			}
//		});
		
		return widgets;
	}
	
	private void setEntity(Content content) {
		content.setClassification(getClassifier(), getEntity());
		content.getService(ContentService.class).update();
	}
	
	private List<EntityMember> getEntities() {
		List<EntityMember> entities = new ArrayList<EntityMember>();
		for (DataSetMember member : getContentDao().getMembers(getEntity().getDataSet(), "lastmodifieddate desc", 10)) {
			entities.add((EntityMember)member);
		}
		return entities;
	}
	
	private EntityMember getEntity(PageParameters parameters) {
		
		
		
		
		DataSetMember member = null;
		EntityMember entity = null;
		StringValue id = parameters.get("entity_id");
		if (!id.isNull() && !id.isEmpty()) {
			member = getContentDao().findMemberById(id.toLong());
			if (member!=null && (!(member instanceof EntityMember) || !member.getDomain().equals(getDomain()))) {
				member = null;
			}
			else {
				entity = (EntityMember)member;
			}
		}	
		return entity;
	}
	
	private Classifier getClassifier(PageParameters parameters) {
		Classifier classifier = null;
		StringValue id = parameters.get("classifier_id");
		if (!id.isNull() && !id.isEmpty()) {
			classifier = (Classifier)getContentDao().findModelObjectById(Classifier.class, id.toLong());
			if (classifier!=null && !classifier.getDomain().equals(getDomain())) {
				classifier = null;
			}
		}	
		return classifier;
	}
}
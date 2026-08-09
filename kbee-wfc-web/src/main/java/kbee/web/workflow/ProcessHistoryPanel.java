package kbee.web.workflow;


import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;


import com.novamens.beans.BeansService;
import com.novamens.calendar.CalendarService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EFormData;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.datetime.DateTimeService;
import com.novamens.event.LogEvent;
import com.novamens.kbee.content.form.KbeeEFormActivityData;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.AuditTrailEvent;

import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.kbee.wicket.util.InvisiblePhoto;
import com.novamens.logging.UpdateAddResourceEvent;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;

import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.WorkflowContext;

import kbee.util.NumberFormatter;
import kbee.web.error.ErrorPanel;
import kbee.web.panel.AlertPanel;
import kbee.web.resource.ResourceLink;
import kbee.web.resource.ResourceThumbnailImage;
import kbee.web.user.UserAvatarPanel;

import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;
import com.novamens.workflow.Task;

@SuppressWarnings("serial")


/**
 * 
 *  WARNING:
 *  getModel() returns null when the Panel is created from the Library.
 *  
 *  getContentModel() is used when the Panel is created from the Library
 *  setHistory()
 *
 * @param <T>
 */
public class ProcessHistoryPanel<T extends Content> extends ModelPanel<WorkflowContext>  {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ProcessHistoryPanel.class.getName());

	private List<IModel<Activity>> history;

	final private boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final private boolean role_support = role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private boolean show_audit_button = true;
	private IModel<User> session_user = null;
	
	private IModel<T> content_model;

	
	public ProcessHistoryPanel(String id, IModel<WorkflowContext> model, IModel<T> content_model) {
		super(id);
		setModel(model);
		this.content_model=content_model;
		setOutputMarkupId(true);
	}

	
	public ProcessHistoryPanel(IModel<WorkflowContext> model, IModel<T> content_model) {
		super("process-history");
		setModel(model);
		this.content_model=content_model;
		setOutputMarkupId(true);
	}
		
	
	public ProcessHistoryPanel(String panelId, IModel<T> content_model, List<IModel<Activity>> activities) {
		super(panelId);
		this.content_model=content_model;
		setHistory(activities);
		setOutputMarkupId(true);
	}

	
	public IModel<T> getContentModel() {
		return this.content_model;
	}

	@SuppressWarnings("unchecked")
	public T getContent() {
		if (getModel()==null) {
			if (getContentModel()==null)
				return null;
			return getContentModel().getObject();
		}
		return (T)((KbeeContext)getModel().getObject()).getContent();
	}
	
	
	public void setHistory(List<IModel<Activity>> list) {
		this.history=list;
	}
	
	
	/**
	 * @return
	 */
	public List<IModel<Activity>> getHistory() {
		if (this.history!=null)
			return this.history;
		this.history = new ArrayList<IModel<Activity>>();
		if (getModel()==null) {
			logger.error(Thread.currentThread().getStackTrace()[1].getMethodName(), "this should never happend ProcessHistory Model is null and Histoty is null");
			return this.history;
		}
		
		try {
			for (Activity activity: getModel().getObject().getProcess().getActivities()) {
				if (!activity.isRunning()) {
					this.history.add(new ObjectModel<Activity>(activity));
				}
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
		return this.history;
	}
	
	
	@Override
	public boolean isVisible() {
		return true; // !getHistory().isEmpty();
	}
	
	

	@Override	
	 public void onDetach() {
		 super.onDetach();
		 
		 if (this.session_user!=null)
			 this.session_user.detach();

		 if (this.getContentModel()!=null)
			 this.getContentModel().detach();
		 
		 if (this.history!=null) {
			 for (IModel<Activity> model: this.history) {
				 model.detach();
			 }
		 }
		 
	 }
		
	
	@Override
	public void onInitialize() {
		super.onInitialize();

 			//WebMarkupContainer ccc = new WebMarkupContainer("notasks");
			//add(ccc);
			//ccc.setVisible(isFirstTask());

			
			AlertPanel<Void> pa=new AlertPanel<Void>("notasks",AlertPanel.INFO,  null, 
					null, 
					getLabel("first-task"));
			pa.setIcon(AlertPanel.HELP_INFO);
			pa.setVisible(isFirstTask());
			add(pa);

			
			
			
			
			
			
			
			
			WebMarkupContainer hc = new WebMarkupContainer("history-container");
			add(hc);
			hc.setVisible(!getHistory().isEmpty());
	
			
			
			
			
			
			
			/**
			 * <p>Audit Trail is enabled only to Support and Users that have write permission on the Content<p>
			 */
			
			WebMarkupContainer au = new WebMarkupContainer("audit-container") {
				public boolean isVisible() {
					return false;
				}
			};

			add(au);
					
			au.add(new AjaxLink<Void>("audit") {
				public void onClick(AjaxRequestTarget target) {
					fire(new AuditTrailEvent(target));
				}
				@Override
				public boolean isEnabled() {
					return true;
				}
				
				@Override
				public boolean isVisible() {
					return true;
				}
			});

			
			/** 
			 * if the procedure has only 1 task, the label is omitted
			 */
			
			hc.add(new ListView<IModel<Activity>>("history", getHistory()) {
				public void populateItem(final ListItem<IModel<Activity>> item) {
					
					Activity activity = item.getModelObject().getObject();
					
					/** In Library there is 1 more element in the list than in Monitor
					 *  this means that  Monitor[i] <- is -> Library[i-1]
					 * */
					int previos_task_index = (ProcessHistoryPanel.this.getModel()!=null) ? item.getIndex() : (item.getIndex()-1);
					
					item.add(getResourcesPanel(activity));
					item.add(getEFormsPanel(activity));
					item.add(getProgressNotesPanel(item.getModelObject()));
					
					
					
					item.add( new UserAvatarPanel("photo", new ObjectModel<User>(activity.getUser())));
					
					Person person = getContentDao().findUserProfileByUser(activity.getUser()).getPerson();					

					item.add(new Label("user", person!=null?person.getFirstLastName():""));
					
					Label bti=new Label("btitle", person!=null?person.getBusinessTitle():"");
					bti.setVisible((person!=null) && (person.getBusinessTitle()!=null));
					item.add(bti);
					
					
					boolean has_full_audit = hasFullAudit(activity);
					
					item.add((new Label("not-allowed", "Not allowed to view Notes")).setVisible(!has_full_audit));
					
					
 					// ------------- Note -------------------------------------------------
					
					String str = activity.getNote();
					if (str!=null) {
						str=str.replace("\r\n", "<br />");
						str=str.replace("\n", "<br />");
					}
					Label la = new Label("notes", str!=null?str:"");
					la.setEscapeModelStrings(false);
					la.setVisible(str!=null && has_full_audit && !activity.getStatus().equals(Activity.Status.REASSIGNED));
					item.add(la);
					
 					// ------------- TASK -------------------------------------------------
					
					Task task = getTask(activity);
					//String tri;
					String task_action = "";

					if (activity.getEvent()!=null) {
						String label = ((com.novamens.kbee.content.workflow.KbeeProcedure) activity.getProcess().getProcedure()).getLabel(activity.getEvent());
						//tri=" > <span class=\"workflow-action\">" + label +"</span>";
						task_action = label;
					}
					//else
					//	tri="";
						

					
					
					//String task_proc_name;
					//try {
					//	task_proc_name=activity.getProcess().getProcedure().getName();
					//
					//} catch (Exception e) {
					//	logger.error(e);
					//	task_proc_name = e.getClass().getSimpleName();
					//}
					
					// Label tas = new Label("task", proc_name + ". " +  task.getName()+ tri);
					// tas.setEscapeModelStrings(false);
					// item.add(tas);
					
					//Label l_task_proc       =   new Label("task-procedure", task_proc_name);
					Label l_task_taskname   =   new Label("task-taskname", task.getName());
					Label l_task_action     =    new Label("task-action", task_action);

					
					Label l_task_start      =    new Label("task-start", ServiceLocator.getService(DateTimeService.class).format(activity.getStartTime(), getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_Year_hh_mm_z));
					Label l_task_end     	=    new Label("task-end", ServiceLocator.getService(DateTimeService.class).format(activity.getEndTime(), getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_Year_hh_mm_z));
 					
					OffsetDateTime of = activity.getDueDate();
					
					String s_d_date; 
					
					if (of==null)
						s_d_date = "";
					else
						s_d_date=ServiceLocator.getService(DateTimeService.class).format(of, getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_Year_hh_mm_z);
					
					Label l_task_due     	=    new Label("task-due", s_d_date);

 					//item.add(l_task_proc);
					item.add(l_task_taskname);
					item.add(l_task_action);
					item.add(l_task_start);
					item.add(l_task_end);

					
					WebMarkupContainer ddc = new WebMarkupContainer("duedate-container");
					ddc.setVisible(of!=null);
					ddc.add(l_task_due );
					item.add(ddc);
					
					// ----------- duration -------------
					//
					double net_dur   = person.getDomain().getService(CalendarService.class).getBusinessHoursDuration(activity.getStartTime(), activity.getEndTime());
					double gross_dur = 0.0;
					
					if (activity.getEndTime() !=null && activity.getStartTime()!=null) {
						gross_dur =  ChronoUnit.SECONDS.between(activity.getStartTime(), activity.getEndTime()) / 3600.0;
					}
					
					String net_dur_s   = NumberFormatter.formatNumber(net_dur   , getSessionUser().getLocale());
					
					String ds = "";
					String de = "";
					
					if (activity.getStartTime()!=null)
						ds = ServiceLocator.getService(DateTimeService.class).format(activity.getStartTime(), getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_Year_hh_mm_z);

					if (activity.getEndTime()!=null)
						de = ServiceLocator.getService(DateTimeService.class).format(activity.getEndTime(), getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_Year_hh_mm_z);
					
					String s_d;
					
					if (!role_admin || logger.isDebugEnabled()) {
						if (gross_dur>0.01) {
							s_d = getLabel("start").getObject() + ". " + ds + "  <br />" + getLabel("end").getObject() + ". "+ de +"  <br />Total. "  + net_dur_s + " " + getLabel("business-hours").getObject();
						}
						else
							s_d = getLabel("start").getObject() +  ". " + ds + "  <br />"+  getLabel("start").getObject() + ". "+ de;
					}
					else
						s_d = getLabel("start").getObject()+ ". " + ds + "  <br />"+ getLabel("end").getObject() + ". "+ de;
					Label lab_d=new Label("duration", s_d);
					lab_d.setEscapeModelStrings(false);
					//item.add(lab_d); 
					// ----------- duration -------------
					
					
					if (activity.getStatus().equals(Activity.Status.REASSIGNED)) {
						
						/**
						 * if the File is in the Library the first Activity in the List is the "Send to Library"
						 * if not, the first activity is omited because it is running, so 
						 * in this case the previous task changnes.
						 */
						Activity previousactivity = activity.getProcess().getActivities().get(previos_task_index);
						
						if (previos_task_index<0 || previousactivity==null) {
							WebMarkupContainer rcon = new WebMarkupContainer("reassigned-container");
							rcon.add((new Label("reassigned", "Software Error. Please contact Support")));
							item.add(rcon);
							logger.error("previos_task_index: "+ String.valueOf(previos_task_index));
						}
						else {
							String reassignedbyuser = activity.getAssignedBy().getFirstLastName();
							String reassignedtouser = previousactivity.getUser().getFirstLastName();
							WebMarkupContainer rcon = new WebMarkupContainer("reassigned-container");
							StringResourceModel labmodel = new StringResourceModel("process.history.message.reassignedTo", ProcessHistoryPanel.this);
							labmodel.setParameters(reassignedtouser, reassignedbyuser);
							
							
							Label lab =	new Label("reassigned", labmodel);
							lab.setEscapeModelStrings(false);		
							lab.add(new AttributeModifier("class", "reassign"));	
							rcon.add(lab);
							item.add(rcon);
						}
					}
					else {
						WebMarkupContainer rcon = new WebMarkupContainer("reassigned-container");
						rcon.add((new Label("reassigned", "")).setVisible(false));
						item.add(rcon);
					}
					item.getModelObject().detach();
					
					/**
					  Notes  are visible to users that:
					  - can access private area
					  - the receiver of the task is the user
					  - Resolution only
					**/
					WebMarkupContainer resolution = new WebMarkupContainer("resolution");
					resolution.setVisible(activity.getResolution()!=null && has_full_audit);

					/**
					AjaxLink<Void> rl = new AjaxLink<Void>("link") {
						public void onClick(AjaxRequestTarget target) {
							Activity activity = item.getModelObject().getObject();
							ResolutionModal modal = (ResolutionModal)ProcessHistoryPanel.this.get("resolution-modal");
							modal.open(target, activity.getResolution(), new Modal.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
								}
							}, ((KbeeWorkflowActivity)activity).getTaskName() + ". " + (activity.getResolutionTitle()!=null?activity.getResolutionTitle():" Resolution"));
							
						}
					};
					Label rtit = new Label("resolution-title", (activity.getResolutionTitle()!=null?activity.getResolutionTitle():" Letter"));
					rl.add(rtit);
					resolution.add(rl);
					*/
								
					Link<Void> rl2 = new Link<Void>("resolution-page-link") {
						public void onClick() {
							setResponsePage(new ResolutionLetterViewPage(item.getModelObject() ));
						}
					};
					Label rtit2 = new Label("r-title", (activity.getResolutionTitle()!=null?activity.getResolutionTitle():" Letter"));
					rl2.add(rtit2);
					resolution.add(rl2);
					
					
					item.add(resolution);
					
				}
			});
		
			 
			
			 
			add(new ResolutionModal());
			add(new EFormModal());
	}

	
	private boolean isFirstTask() {

		if (getModel()==null || getModel().getObject()==null) 
			return false;
		
			
		if (getHistory().isEmpty())
			return true;
		
		
		return false;
		 
 	}


	/**
	 * 
	 * @param user
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Image getPhoto(User user) {
		try {
		Person person = getContentDao().findUserProfileByUser(user).getPerson();
		
		if (person!=null && person.getPhoto()!=null) 
			return  new ResourceThumbnailImage("photo", new ObjectModel<Resource>( (Resource) person.getPhoto()), ThumbnailSize.MINI);
		
		return new Image("photo", ServiceLocator.getService(BrandingWebService.class).getUserAvatarResourceReference(person));
		
		//return ServiceLocator.getService(BrandingWebService.class).getUserAvatarPhoto("photo", person);
				
		
		} catch (Exception e) {
			logger.error(e);
			return new InvisiblePhoto("photo");
		}
	}

	/**
	 * 
	 */
	protected Task getTask(Activity activity) {
		for (Task task : activity.getProcess().getProcedure().getTasks()) {
			if (task.getId().equals(((KbeeWorkflowActivity)activity).getTaskName()))
				return task;
		}
		return null;
	}
	
	
	/**
	 * 
	 */
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	/** 
	 *  if user has access to private area
	 *  if user [executed] or is [recepient] of the task 
	 *  
	 */
	private boolean hasFullAudit(Activity activity) {

		
		if(activity.getUser()==null)
			return true;

		if (role_support)
			return true;


		// if user has access to Private Notes
		//
		if (isAuthorizedPrivateNotes())
			return true;

		
		// if user HAS DONE Activity
		// 
		if (activity.getUser().getId().equals(getSessionUserModel().getObject().getId()))
				return true;
		

		// if file is still in Workflow
		//
		if (!getHistory().isEmpty() && (getContentModel().getObject().getWorkspace()!=null)) {
			Activity am = getHistory().get(0).getObject();
			if (am.getId().toString().equals(activity.getId().toString()))
				return true;
		}
		
		

		
		//Activity previous_ac = null;
		Activity next_ac = null;;
		
		Activity current_ac = null;;
		
		
		boolean last_was_activity	= false;
		
		//boolean prev_done	= false;
		boolean next_done 	= false;
		

		for (IModel<Activity> am: getHistory()) {

			current_ac = (KbeeWorkflowActivity)am.getObject();
			
			if (last_was_activity && !next_done) {
				next_ac=current_ac; 
				next_done = true;
			}
				
			if (current_ac.getId().toString().equals(activity.getId().toString())) {
				//prev_done = true;
				last_was_activity = true;
			}
			else  {
				/*
				 * if (!prev_done) previous_ac = current_ac;
				 */
				last_was_activity = false;
			}
		}
		

		/**
		 * if previous was
		 */
		//if (previous_ac!=null) {
		//	
		//	
		//}
		
		// if next activity is mine, 
		if (next_ac!=null) {
			
			if (    next_ac.getUser() !=null && 
					next_ac.getUser().getId().equals(getSessionUserModel().getObject().getId()))
				return true;
		}
		
		return false;
		
	}

	public IModel<User> getSessionUserModel() {
			if (this.session_user!=null)
				return this.session_user; 
			this.session_user = new ObjectModel<User>(getSessionUser());
				return this.session_user; 
	}


	public void setAuditButton(boolean b) {
		this.show_audit_button=b;
	}
	

	public boolean isAuditButton() {
		return this.show_audit_button;
	}

	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected boolean isAuthorizedPrivateNotes() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getContent());
	}

	protected boolean isAuditTrail(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(content);
	}
	

	
	private WebMarkupContainer getProgressNotesPanel(IModel<Activity> activity) {

		WebMarkupContainer mk = new WebMarkupContainer("progress-notes-container");
		try {
			

			
			
			
			
			List<ActivityProgressNote> notes = activity.getObject().getProgressNotes();
			if (notes==null || notes.isEmpty()) {
				mk.setVisible(false);
				return mk;
			}
			mk.add( new ProgressNotesListPanel<T>("progress-notes", activity) );
		} 
		catch (Exception e ) {
			logger.error(e);
			mk.add( new ErrorPanel("progress-notes", e));
			
		}
		return mk;
	}

		
	private WebMarkupContainer getEFormsPanel(Activity activity) {

		WebMarkupContainer mk = new WebMarkupContainer("eforms-container");

		try {

			List<IModel<EFormData>> datamodels = new ArrayList<IModel<EFormData>>();
			for (EFormData data : ((KbeeWorkflowActivity)activity).getFormsData()) {
				if (data.getForm()!=null && (!data.getForm().getFormAccessLevel().equals(EFormAccessLevel.INTERNAL_INFO) || isInternalInfoReadable())) {
					datamodels.add(new ObjectModel<EFormData>(data));
				}
			}
			if (datamodels.isEmpty()) {
				mk.setVisible(false);
				return mk;
			}
			mk.add(new ListView<IModel<EFormData>>("eform", datamodels) {
 				@Override
				protected void populateItem(ListItem<IModel<EFormData>> item) {
					try {
						EFormData data = item.getModelObject().getObject();
						Link<?> formlink = new Link<Void>("eform-link") {
							public void onClick() {
								EFormData data = item.getModelObject().getObject();
								Activity activity = ((KbeeEFormActivityData)data).getActivity();
								IModel<Activity> activitymodel = new ObjectModel<Activity>(activity);
								setResponsePage(new EFormViewerPage(activitymodel, new ObjectModel<EFormData>(data)));
							}
						};
						formlink.add(new Label("eform-title", data.getForm().getDisplayName()));
						item.add(formlink);
					} 
					catch (Exception e ) {
						logger.error(e);
						item.add(new InvisiblePanel("resource-link"));
					}
				}
			});
		} 
		catch (Exception e ) {
			logger.error(e);
		}
		return mk;
	}
	
	private WebMarkupContainer getResourcesPanel(Activity activity) {

		WebMarkupContainer mk = new WebMarkupContainer("resources-uploaded-container");

		try {

			List<LogEvent> list = getContentDao().getAddResourcesAuditTrail(activity);
			
			if (list==null || list.isEmpty()) {
				logger.debug("getAddResourcesAuditTrail(Activity) is empty | Activity -> " + (activity!=null?activity.getDisplayName():"null"));
				mk.setVisible(false);
				return mk;
			}
			
			List<IModel<KBFile>> fi_list = new ArrayList<IModel<KBFile>>();
			
			for (LogEvent lo: list) {
				Resource fi = getContentDao().findResourceById(	KBFile.class,  (long) ((UpdateAddResourceEvent) lo).getResourceId());
				
				// We only include files uploaded into the "Resources" panel (Private Area are not included)
				if (fi instanceof KBFile) {
					if (fi.isPublicArea()) {
						IModel<KBFile> model = new ObjectModel<KBFile>( (KBFile) fi);
						fi_list.add(model);
					}
					else {
						logger.debug("File not in Resources: " + fi.getTitle());
					}
				}
			}
	
			mk.add(new ListView<IModel<KBFile>>("resources-uploaded", fi_list) {
				@Override
				public void onDetach() {
					super.onDetach();
					for (IModel<KBFile> m:super.getList())
						m.detach();
				}
				
				@Override
				protected void populateItem(ListItem<IModel<KBFile>> item) {
					try {
						
						KBFile file = item.getModelObject().getObject();
						String title=file.getTitle();
						String icon=file.getGlyphIcon();
						Label lt =new Label("resource-title", title);
						WebMarkupContainer gi = new WebMarkupContainer("glyphicon");
						gi.add(new AttributeModifier("class", icon));
						String size=" (" + NumberFormatter.formatFileSize(file.getSize(), getSessionUser().getLocale()) + ")";
						Label ls=new Label("resource-uploaded", size);
						ResourceLink<T> rl = new ResourceLink<T>("resource-link", new ObjectModel<Resource>(file),  getContentModel());
						rl.add(lt);
						rl.add(gi);
						rl.add(ls);
						item.add(rl);
						
					} catch (Exception e ) {
						logger.error(e);
						item.add(new InvisiblePanel("resource-link"));
					}
				}
			});
		} catch (Exception e ) {
			logger.error(e);
		}
		return mk;
	}

	
	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[])parameter);
		return model;
	}
	
	protected boolean isInternalInfoReadable() {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(content_model.getObject(), getSessionUser());
	}

}

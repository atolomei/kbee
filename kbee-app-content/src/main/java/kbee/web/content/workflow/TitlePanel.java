package kbee.web.content.workflow;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.form.ValueUpdated;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.LabelsService;
import com.novamens.content.user.UserLabel;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.markup.html.event.RemoveLabelEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.workflow.Task;

import kbee.util.logging.Logger;
import kbee.web.console.grid.LabelSetPanel;
import kbee.web.content.editor.ContentEditor;
import kbee.web.eform.EAjaxRefreshEvent;
import kbee.web.event.wicket.ContentEditorEvent;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.event.wicket.LabelEvent;

@SuppressWarnings("serial")

/**
 *  Task Editor Title 
 * 
 * @param <T>
 */
public class TitlePanel<T extends Content> extends ObjectEditorPanel<T>  {
	private static final long serialVersionUID = 1L;
																								
	private static Logger logger = Logger.getLogger(TitlePanel.class.getName());

	final boolean root		     = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean role_admin     = root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private String title;
	private String orignalTitle;
 
	private boolean manualupdate = false;
	private boolean editionEnabled = false;
	private boolean is_content_template_title_editable = true;

	private ClassificationSummaryPanel<T> clasification_summary;
	private LabelSetPanel<T> labelset;
	
	
	private WebMarkupContainer actions_container;
	
	
	public TitlePanel() {
		this("title-panel");
	}
	
	public TitlePanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public boolean isLabelsEnabled() {
		return true;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		if (title!=null && title.length()>0)
			return title;
		try {
			if (getModelObject() instanceof Content) {
				Content content = (Content) getModelObject();
				if (content.getContentTemplate()!=null) 
					return   new StringResourceModel("new", TitlePanel.this, null).getString() + " " + content.getContentTemplate().getName();
				if (content.getOId()!=null)
				return content.getOId().toString();
			}
			return new StringResourceModel("title", this, null).getString() + "  " + String.valueOf(getModelObject().hashCode());
		} 
		catch (Exception e) {
			logger.error(e);
			return "";
		}
	}

	
	@Override
	public void updateModel() {
		if (title!=null && !title.equals(getModelObject().getTitle())) {
			String oldvalue = getModelObject().getTitle();
			if (manualupdate) {
				getModelObject().setTitle(title);
				getModelObject().getService(PropertyService.class).setProperty("title", "true");
			}	
			else {
				getModelObject().setTitle(title);
				getModelObject().getService(PropertyService.class).removeProperty("title");
			}	
			setUpdatedField(new ValueUpdated(null, "title", oldvalue, title));
		}
	}
	
	
	public WebTask getTask() {
		Task task = getModelObject().getService(WorkflowService.class).getTask();
		if (task!=null && task instanceof WebTask) return (WebTask)task;
		return null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		is_content_template_title_editable = getModelObject().getContentTemplate().isTitleEditable();
		
		if (getTask()!=null && !getTask().isEditableTitle()) {
				is_content_template_title_editable = false;
		}
		
		setTitle(getModelObject().getTitle());
		
		orignalTitle = getModelObject().getTitle();
		
		if ("true".equals(getModelObject().getService(PropertyService.class).getProperty("title")))
			manualupdate = true;
		if (getModelObject().getVersion()>1)
			manualupdate = true;
		
		addComponents();
	}

	
	@SuppressWarnings("unchecked")
	protected void addComponents() {
		
		
		// para permitir remover: tiene que ser userworkspace y la tarea tener labels enabled
		//
		//if (isLabelsEnabled()) {
			labelset = new LabelSetPanel<T>("labels", getModel(), 
				isUserWorkspace() && getTask().isEnableLabels(), // remove enabled 
				true,   // label list 
				false); // dropdown
			
			addOrReplace(labelset);
		//}
		//else {
		//	addOrReplace(new InvisiblePanel("labels"));
		//}
		
		actions_container = new WebMarkupContainer("actions-container");
		actions_container.setVisible(!editionEnabled);
		add(actions_container);
		
		AjaxLink<Void> edit = new AjaxLink<Void>("edit") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				editionEnabled = !editionEnabled;
				((TextField<?>)TitlePanel.this.get("title-container:ztitle-editor")).onBeforeRender();
				((TextField<String>)TitlePanel.this.get("title-container:ztitle-editor")).setValue(getTitle());
				target.focusComponent(((TextField<?>)TitlePanel.this.get("title-container:ztitle-editor")).getInput());
				target.add(TitlePanel.this);
			}

			@Override
			public boolean isVisible() {
				return isUserWorkspace() && !editionEnabled  && is_content_template_title_editable;
			}
		};

		actions_container.add(edit);
		
		
		WebMarkupContainer title = new WebMarkupContainer("xcontent-title") {
			@Override
			public boolean isVisible() {
				return !isEditionEnabled();
			}
		};
		
		title.add(new Label("title-text", new Model<String>() { 
			public String getObject() { 
				return getTitle(); 
			};
		}));
		
		title.add(new WebMarkupContainer("lock-icon") { 
			public boolean isVisible() { 
				return getModelObject().isLocked(); 
			};
		});
		
		title.add(new WebMarkupContainer("checkout-icon") { 
			public boolean isVisible() { 
				return isCheckout(getModelObject()); 
			};
		});
		
		add(title);

		WebMarkupContainer title_container = new WebMarkupContainer("title-container") {
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
		};

		add(title_container);

		Label content_template_title_editable = new Label("not-editable", 
				new StringResourceModel("not-editable", this, null).setParameters( new Object[] {  getServerUrl()+"/model/contentclass/"+ getModelObject().getContentTemplate().getId().toString()+"?tab=display", getModelObject().getContentTemplate().getDisplayName()}));

		
		content_template_title_editable.setEscapeModelStrings(false);
		
		content_template_title_editable.setVisible(!is_content_template_title_editable);
		title_container.add(content_template_title_editable);
		
		title_container.add(new TextField<String>("ztitle-editor", new PropertyModel<String>(this, "title")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				manualupdate = !getValue().equals(getTitle());
				super.onUpdate(target);
			}
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
			
			
			@Override
			public boolean isEnabled() {
				return isEditionEnabled() && is_content_template_title_editable;
			}
			
			@Override
			public IModel<String> getLabel() {
				return new StringResourceModel("title", this, null);
			}
		});
		
		title_container.add(new AjaxSubmitLink("save-link", getEditor().getForm()) {
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				editionEnabled = !editionEnabled;
				getEditor().update(target);
				target.add(TitlePanel.this);
			}
			@Override
			public boolean isVisible() {
				if (!is_content_template_title_editable)
					return false;
				return isEditionEnabled();
			}
		});
		
		AjaxLink<Void> cl = new AjaxLink<Void>("cancel-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				((TextField<String>)(TitlePanel.this.get("title-container:ztitle-editor"))).cancel();
				editionEnabled = false;
				((TextField<String>)(TitlePanel.this.get("title-container:ztitle-editor"))).setValue(getOriginalTitle());
				getEditor().update(target);
				target.add(TitlePanel.this);
			}
			@Override
			public boolean isVisible() {
				 
				return isEditionEnabled();
			}
		};
		
		Label ca=new Label("cancel",  is_content_template_title_editable ?  new StringResourceModel("cancel", TitlePanel.this, null) :  new StringResourceModel("close", TitlePanel.this, null) );
		cl.add(ca);
		
		title_container.add(cl);
		
		title_container.add(new AjaxLink<Void>("rule-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setTitle(getTitleByRule(getContent(null)));
				((TextField<String>)(TitlePanel.this.get("title-container:ztitle-editor"))).setValue(getTitle());
				manualupdate = false;
				target.add(TitlePanel.this);
			}
			@Override
			public boolean isVisible() {
				return isEditionEnabled(); // && manualupdate;
			}
			
			@Override
			public boolean isEnabled() {
				return getTitleRule()!=null && getTitleRule().length()>0; // && manualupdate;
			}
			
		});
		
		title_container.add(new Link<Void>("view-rule-link") {
			@Override
			public void onClick() {
				Serializable sid=TitlePanel.this.getModel().getObject().getContentTemplate().getId();

				String url=getServerUrl()+"/model/contentclass/"+sid.toString()+"?tab=display";
				setResponsePage( new RedirectPage(url));
				
			}
			@Override
			public boolean isVisible() {
				return role_admin;
			}
		});

		//if (clasification_summary==null) {
		//	clasification_summary=new ClassificationSummaryPanel<T>();
		//	add(clasification_summary);
		//}
		add(new InvisiblePanel("summary"));
		
	}

	protected boolean isCheckout(T modelObject) {
		if ( (!modelObject.isHeadVersion()) && (modelObject.getWorkspace()!=null))
			return modelObject.getVersion()>1;
		return false;
	}

	protected boolean isUserWorkspace() {
		if (getModel().getObject().getWorkspace()==null)
			return false;
		return (getModel().getObject().getWorkspace().equals(getSessionUser().getId()));
	}

		
	protected void addListeners() {
		
		add(new WicketEventListener<LabelEvent>() {
			@Override
			public void onEvent(LabelEvent event) {
				logger.debug(event);
				if (isLabelsEnabled()) {
					labelset = new LabelSetPanel<T>("labels", TitlePanel.this.getModel(), 
							isUserWorkspace(), //remove 
							true, // labelset
							false); // dropdown
					TitlePanel.this.addOrReplace(labelset);
				}
				event.getRequestTarget().add(TitlePanel.this);
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof LabelEvent;
			}
		});		
		
		add(new WicketEventListener<RemoveLabelEvent<T>>() {
			public void onEvent(RemoveLabelEvent<T> event) {
				if (isLabelsEnabled()) {
					labelset = new LabelSetPanel<T>("labels", TitlePanel.this.getModel(), isUserWorkspace(), true, false);
					TitlePanel.this.addOrReplace(labelset);
				}
				event.getRequestTarget().add(TitlePanel.this);
			}
		});
		
		add(new WicketEventListener<EditorEvent>() {
			public void onEvent(EditorEvent event) {
				TitlePanel.this.onEvent(event);
				event.getRequestTarget().add(TitlePanel.this);
			}
		});
		
		add(new WicketEventListener<ContentEditorEvent>() {
			public void onEvent(ContentEditorEvent event) {
				TitlePanel.this.onEvent(event);
				if (event.getRequestTarget()!=null)
				event.getRequestTarget().add(TitlePanel.this);
			}
		});
		
		add(new WicketEventListener<EAjaxRefreshEvent>() {
			@Override
			public void onEvent(EAjaxRefreshEvent event) {
				TitlePanel.this.onEvent(event);
				if (event.getRequestTarget()!=null)
				event.getRequestTarget().add(TitlePanel.this);
			}
		});
	}

	protected boolean isEditionEnabled() {
		return getEditor().isEditionEnabled() && editionEnabled;
	}
	
	protected String getTitleRule() {
		return getModelObject().getContentTemplate().getTitleRuleTemplate();
	}
	
	protected void onEvent(WicketAjaxEvent event) {
		if (this.manualupdate) 
			return;
		
		if (getTitleRule()!=null && !"".equals(getTitleRule())) {
			setTitle(getTitleByRule(getContent(event)));
		}
		else {
			if (title==null || "".equals(title) || isDefaultTitle(title)) {
				setTitle(getTitleByRule(getContent(event)));
			}
		}
		if (event.getRequestTarget()!=null) {
			event.getRequestTarget().add(this);
		}	
	}

	protected String getTitleByRule(Content content) {
		ExtractionRule rule = getModelObject().getContentTemplate().getTitleRule();
		String title = (String)rule.extract(content);
		return title;
	}
	
	@SuppressWarnings("unchecked")
	protected Content getContent(WicketAjaxEvent event) {
		T content;
		if (event instanceof ContentEditorEvent) {
			content = (T)((ContentEditorEvent)event).getContent();
		}
		else {
			content = getEditor().getModelObject();
			((ContentEditor<T>)getEditor()).update(content);
		}
		return content;
	}
	
	protected String getLabels() {
		Content content = getModelObject();
		StringBuilder labels = new StringBuilder();
		for (UserLabel label : content.getService(LabelsService.class).getUserLabels()) {  
			try {
				labels.append("<span class=\"user-label "+label.getCss()+"\">"+label.getLabel()+"</span>");
			} catch (org.hibernate.ObjectNotFoundException e) {
				logger.error(e);
			}
		}
		return labels.toString();
	}
	
	protected boolean isDefaultTitle(String title) {
		String template = getModelObject().getContentTemplate().getName();
		return title.equals("New "+template) || title.equals("Nuevo "+template); 
	}
	
	protected String getOriginalTitle() {
		return this.orignalTitle;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
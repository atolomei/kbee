package com.novamens.content.web.content.markup;

import java.io.Serializable;

import org.apache.commons.text.WordUtils;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.LabelsService;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.content.user.UserLabel;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.text.template.KbeeContentTextTemplate;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.web.console.grid.LabelSetPanel;
import kbee.web.console.grid.LabelTagPanel;
import kbee.web.content.editor.ContentEditor;
import kbee.web.event.wicket.EditorEvent;
import kbee.web.event.wicket.LabelEvent;
import kbee.web.service.ApplicationSiteMapService;
import kbee.web.text.template.ContentVariableResolverWeb;

@SuppressWarnings("serial")

/**
 *  Task Editor Title 
 * 
 * @param <T>
 */
public class TitlePanel<T extends Content> extends ObjectEditorPanel<T>  {
			
	private static final long serialVersionUID = 1L;
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TitlePanel.class.getName());

	final boolean root		     = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean role_admin     = root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private String title;
	private String orignalTitle;
 
	private boolean manualupdate = false;
	private boolean editionEnabled = false;

	public TitlePanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public TitlePanel() {
		super("title-panel");
		setOutputMarkupId(true);
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
		} catch (Exception e) {
			logger.error(e);
			return "";
		}
		
	}

	/**
	 * 
	 */
	@Override
	public void updateModel() {
		if (title!=null && !title.equals(getModelObject().getTitle())) {
			getModelObject().setTitle(title);
			if (manualupdate)
				getModelObject().getService(PropertyService.class).setProperty("title", "true");
			else
				getModelObject().getService(PropertyService.class).removeProperty("title");
			setUpdatedPart("title");
		}
	}

	
	/**
	 * 
	 */
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("xcontent-title")==null) {
			
			setTitle(getModelObject().getTitle());
			
			orignalTitle = getModelObject().getTitle();
			
			if ("true".equals(getModelObject().getService(PropertyService.class).getProperty("title")))
				manualupdate = true;
			if (getModelObject().getVersion()>1)
				manualupdate = true;
			addComponents();
		}
	}

	/**
	 * 
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		if (get("labels")!=null)
			get("labels").detach();
	}

	
	/**
	 * 
	 */
	@SuppressWarnings({"deprecation","unchecked"})
	protected void addComponents() {
		
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
		
		title.add(new AjaxEventBehavior("click") {
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				editionEnabled = !editionEnabled;
				((TextField<?>)TitlePanel.this.get("title-container:ztitle-editor")).onBeforeRender();
				((TextField<String>)TitlePanel.this.get("title-container:ztitle-editor")).setValue(getTitle());
				target.focusComponent(((TextField<?>)TitlePanel.this.get("title-container:ztitle-editor")).getInput());
				target.add(TitlePanel.this);
			}
		});
		
		add(title);

		WebMarkupContainer title_container = new WebMarkupContainer("title-container") {
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
		};

		add(title_container);
		
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
				return isEditionEnabled();
			}
		});
		
		title_container.add(new AjaxLink<Void>("cancel-link") {
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
		});
		
		title_container.add(new AjaxLink<Void>("rule-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setTitle(getRuleTitle());
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
				PageParameters pa=new PageParameters();
				pa.set("id", sid.toString());
				setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("model-contenttemplate-page", pa));
			}
			@Override
			public boolean isVisible() {
				return role_admin;
			}
		});

		if (get("summary")==null)
			add(new ClassificationSummaryPanel<T>());
		
		if (get("labels")==null)
			add(new LabelSetPanel<T>("labels", getModel(), false, true, true));
		
		add(new WicketEventListener<EditorEvent>(EditorEvent.class) {
			public void onEvent(EditorEvent event) {
				TitlePanel.this.onEvent(event);
				event.getRequestTarget().add(TitlePanel.this);
			}
		});
		
		add(new WicketEventListener<LabelEvent>(LabelEvent.class) {
			public void onEvent(LabelEvent event) {
				
				throw new RuntimeException ("deprecated");
				
				//LabelTagPanel<T> panel = (LabelTagPanel<T>) TitlePanel.this.get("labels");
				//if (panel!=null) {
				//	panel.detach();
				//}
				// event.getRequestTarget().add(TitlePanel.this);
			}
		});
	}

	protected boolean isEditionEnabled() {
		return getEditor().isEditionEnabled() && editionEnabled;
	}
	
	protected String getTitleRule() {
		return getModelObject().getContentTemplate().getTitleRuleTemplate();
	}
	
	protected void onEvent(EditorEvent event) {
		if (this.manualupdate) 
			return;
		
		if (getTitleRule()!=null && !"".equals(getTitleRule())) {
			setTitle(getRuleTitle());
		}
		else {
			if (title==null || "".equals(title) || isDefaultTitle(title)) {
				setTitle(getRuleTitle());
			}
		}
		if (event.getRequestTarget()!=null)
			event.getRequestTarget().add(this);
	}
	

	protected String getRuleTitle() {
		
		ExtractionRule rule = getModelObject().getContentTemplate().getTitleRule();
		T content = getEditor().getModelObject();
		((ContentEditor<T>)getEditor()).update(content);
		String title = (String)rule.extract(content);
		
		return title;
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
}

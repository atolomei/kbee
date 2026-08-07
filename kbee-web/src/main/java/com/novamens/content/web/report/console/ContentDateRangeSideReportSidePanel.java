package com.novamens.content.web.report.console;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.portal.service.SearchSuggestionService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AdvancedSearchField;
import kbee.web.report.ReportBaseParameterPanel;
import kbee.web.searcher.panel.SearcherAdvancedPanel;

public class ContentDateRangeSideReportSidePanel extends ReportBaseParameterPanel {

	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentDateRangeSideReportSidePanel.class.getName());
	
	private static final long serialVersionUID = 1L;

	private IModel<Content> model;
	
	private Boolean listType = Boolean.valueOf(true);
	
	final boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	
	@Override
	public void onInitialize() {
		super.onInitialize();

		setListType(getParameters().get("type")!=null && getParameters().get("type").equals("list"));
		
		com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5<Void> close = new com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5<Void>("close") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
		};
		add(close);

		Form<?> form = new Form<Void>("form");

		form.add( new SearcherAdvancedPanel("searcher") {
			private static final long serialVersionUID = 1L;
			@Override
			@SuppressWarnings("unchecked")
			public void onSelect(AjaxRequestTarget target, Content content) {
				setContent(content);
				getParameters().put("content", content.getId());
				getParameters().put("contentOid", content.getOId());
				target.add(ContentDateRangeSideReportSidePanel.this);
				setVisible(false);
				TextField<Content> field = (TextField<Content>)form.get("content");
				field.setValue(content);
				field.setFieldValue(content.getTitle());
				((AdvancedSearchField<Content>) ContentDateRangeSideReportSidePanel.this.get("form:content")).setOpen(false);
				target.add(form);
			}
			@SuppressWarnings("unchecked")
			@Override
			protected void onClose(AjaxRequestTarget target) {
				setVisible(false);
				((AdvancedSearchField<Content>) ContentDateRangeSideReportSidePanel.this.get("form:content")).setOpen(false);
				target.add(form);
			}
		});
		
		
		form.add(new AdvancedSearchField<Content>("content",  new PropertyModel<Content>(this, "content")) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onOpenAdvancedSearch(AjaxRequestTarget target) {
				ContentDateRangeSideReportSidePanel.this.get("form:searcher").setVisible(!ContentDateRangeSideReportSidePanel.this.get("form:searcher").isVisible());
				target.add(form);
			}
			
			public void onUpdate(AjaxRequestTarget target) {
				target.focusComponent(getInput());
				setContent(getValue());
				getParameters().put("content", getValue().getId());
				getParameters().put("contentOid", getValue().getOId());
				target.add(ContentDateRangeSideReportSidePanel.this);
			} 
			
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				List<Suggestion> suggestions = getDomain().getService(SearchSuggestionService.class).getSuggestions(pattern);
				return suggestions;
			}
		});

		

		
		form.add(new OffsetDateTimeField("from", ZoneId.of( getDomain().getTimeZone() ), new PropertyModel<OffsetDateTime>(this, "from")) {
			private static final long serialVersionUID = 1L;
			public void onUpdate(AjaxRequestTarget target) {
				getParameters().put("from", getValue());
			}
		});
			
										
		form.add(new OffsetDateTimeField("to", ZoneId.of( getDomain().getTimeZone() ), new  PropertyModel<OffsetDateTime>(this, "to")) {
			private static final long serialVersionUID = 1L;
			public void onUpdate(AjaxRequestTarget target) {
				getParameters().put("to", getValue());
			}
		});

		
		
		form.add(new BooleanField("aggregate-users", new PropertyModel<Boolean>(this, "listType")) {
			private static final long serialVersionUID = 1L;
			public void onUpdate(AjaxRequestTarget target) {
				Boolean val=getValue();
				ContentDateRangeSideReportSidePanel.this.getParameters().put("type", val ? "list" : "aggregate");
			}
			
			protected String getFalseStr() {
				return new StringResourceModel("aggregate", ContentDateRangeSideReportSidePanel.this, null).getString();
			}

			protected String getTrueStr() {
				return new StringResourceModel("list", ContentDateRangeSideReportSidePanel.this, null).getString();
			}
		});

		
		
		form.add(new AjaxSubmitLink("submit") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				onChange(target, getParameters());
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s  = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<span class=\"far fa-sync fa-spin fa-fw spinning\"></span> "+getLoading()+"'";
						return s;																		
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		});
		
		
		WebMarkupContainer selectedcontent = new WebMarkupContainer("selected-content") {
			public boolean isVisible() {
				return getContent()!=null;
			}
		};
		
		add(selectedcontent);
		
		selectedcontent.add(new Label("title", new Model<String>() {
			public String getObject() {
				return getContent().getTitle();
			}
		}));

		selectedcontent.add(new Label("metadata", new Model<String>() {
			public String getObject() {
				return getContent()!=null?
						(getContent().getService(ContentService.class).getPortalSubtitle() + ". " + getLabel("version").getObject() + String.valueOf(getContent().getVersion())): "";
			}
		}));
				
	 
		add(form);

		
		
	}
	
	/**
	 * Contenido 
	 * From - To
	 */
	public ContentDateRangeSideReportSidePanel(String id,  String reportKey, Map<String, Object> map) {
		super(id, reportKey, map);
		
		setOutputMarkupId(true);
		
		
		
	}
	 
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
		
	public Content getContent() {
		return model!=null ? model.getObject() : null;
	}
	
	public void setContent(Content content) {
		model = new ObjectModel<Content>(content);
	}
	
	

	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null) 
			model.detach();
	}
	
	protected void onChange(AjaxRequestTarget target, Map<String, Object> parameters) {
	}

	public void  setListType(Boolean b) {
		this.listType = b;
	}
	
	public Boolean getListType() {
		return this.listType;
	}

}

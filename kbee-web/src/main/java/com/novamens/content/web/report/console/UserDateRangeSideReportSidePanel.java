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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.portal.service.SearchSuggestionService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AdvancedSearchField;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.report.ReportBaseParameterPanel;
import kbee.web.searcher.panel.SearcherAdvancedPanel;

public class UserDateRangeSideReportSidePanel extends ReportBaseParameterPanel {
	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserDateRangeSideReportSidePanel.class.getName());
	
	private static final long serialVersionUID = 1L;

	private IModel<User> model;
	
	//private boolean is_content_selector = false;
	
	
	final boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	private boolean role_portal_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.PORTAL_ADMIN.getId());

	/**
	 * 
	 * Contenido 
	 * From - To
	 * 
	 */
	public UserDateRangeSideReportSidePanel(String id, String reportKey) {
		super(id, reportKey);
		setOutputMarkupId(true);
	}
	 
	
	private IModel<Content> c_model;
	
	public Content getContent() {
		return c_model!=null ? c_model.getObject() : null;
	}
	
	public void setContent(Content content) {
		if (content!=null)
			c_model = new ObjectModel<Content>(content);
		else
			c_model=null;
	}
	
	/**
	 * add all here to assign default values to dates
	 */
	public void onInitialize() {
			super.onInitialize();

			com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5<Void> close = new com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5<Void>("close") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					onClose(target);
				}
			};
			add(close);

			Form<?> form = new Form<Void>("form");

			
			//--
			form.add( new SearcherAdvancedPanel("searcher") {
				private static final long serialVersionUID = 1L;
				@Override
				@SuppressWarnings("unchecked")
				public void onSelect(AjaxRequestTarget target, Content content) {
					setContent(content);
					getParameters().put("content", content.getId());
					getParameters().put("contentOid", content.getOId());
					target.add(UserDateRangeSideReportSidePanel.this);
					setVisible(false);
					TextField<Content> field = (TextField<Content>)form.get("content");
					field.setValue(content);
					field.setFieldValue(content.getTitle());
					((AdvancedSearchField<Content>) UserDateRangeSideReportSidePanel.this.get("form:content")).setOpen(false);
					target.add(form);
				}
				@SuppressWarnings("unchecked")
				@Override
				protected void onClose(AjaxRequestTarget target) {
					setVisible(false);
					((AdvancedSearchField<Content>) UserDateRangeSideReportSidePanel.this.get("form:content")).setOpen(false);
					target.add(form);
				}
			});
			
			
			form.add(new AdvancedSearchField<Content>("content",  new PropertyModel<Content>(this, "content"), false) {
				private static final long serialVersionUID = 1L;
				
				
				@Override
				public IModel<String> getHelpText() {
					IModel<String> m= getContentSelectorHelp();
					if (m==null)
						return super.getLabel();
					return m;
				}
				

				
				
				@Override
				public void onOpenAdvancedSearch(AjaxRequestTarget target) {
					UserDateRangeSideReportSidePanel.this.get("form:searcher").setVisible(!UserDateRangeSideReportSidePanel.this.get("form:searcher").isVisible());
					target.add(form);
				}
				
				
				
				public void onUpdate(AjaxRequestTarget target) {
					target.focusComponent(getInput());
					if (getValue()==null) {
						setContent(null);
						getParameters().put("content", null);
						getParameters().put("contentOid", null);
					}
					else  {
						setContent(getValue());
						getParameters().put("content", getValue().getId());
						getParameters().put("contentOid", getValue().getOId());
					}
					target.add(UserDateRangeSideReportSidePanel.this);
				} 
				
				@Override
				public List<Suggestion> getSuggestions(String pattern) {
					List<Suggestion> suggestions = getDomain().getService(SearchSuggestionService.class).getSuggestions(pattern);
					return suggestions;
				}
			});

			//--
			
			
			
			AutoCompleteFieldV5<User> usel = new AutoCompleteFieldV5<User>("user", new PropertyModel<User>(this, "user"), true) {
				private static final long serialVersionUID = 1L;

				@Override
				public IModel<String> getLabel() {
					
					IModel<String> m= getUserSelectorLabel();
					if (m==null)
						return super.getLabel();
					return m;
					
					

				}
				
				@Override
				public int getMaxHistory() {
					return 10;
				}
				
				@Override
				public boolean isEnabled() {
					return role_admin || role_portal_admin || role_support;
				}

				@Override
				public List<Suggestion> getSuggestions(String pattern) {
					return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern);
				}

				
				@Override
				public String getHistoryKey() {
					logger.debug("report-user-"+getReportKey());
					return "report-user-"+getReportKey();
				}

				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					if (getValue() != null) {
						getParameters().put("user", getValue().getId().toString());
					}
				}
			};

			form.add(usel);


			
			form.add(new OffsetDateTimeField("from", ZoneId.of(getDomain().getTimeZone()), new PropertyModel<OffsetDateTime>(this, "from"),  true) {
				private static final long serialVersionUID = 1L;
				public void onUpdate(AjaxRequestTarget target) {
					getParameters().put("from", getValue());
				}
			});
												
			form.add(new OffsetDateTimeField("to", ZoneId.of(getDomain().getTimeZone()) , new PropertyModel<OffsetDateTime>(this, "to"), true) {
				private static final long serialVersionUID = 1L;
				public void onUpdate(AjaxRequestTarget target) {
					getParameters().put("to", getValue());
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
			
			add(form);

			
			
	}
	
	protected IModel<String> getContentSelectorHelp() {
		return null;
	}

	
	protected IModel<String> getContentSelectorLabel() {
		return null;
	}
	
	 
	protected IModel<String> getUserSelectorLabel() {
		return null;
	}

	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
		
	public User getUser() {
		return model!=null ? model.getObject() : null;
	}
	
	public void setUser(User user) {
		model = new ObjectModel<User>(user);
	}
	
	

	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null) 
			model.detach();
	}
	
	protected void onChange(AjaxRequestTarget target, Map<String, Object> parameters) {
	}
}

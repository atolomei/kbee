package com.novamens.aerolineas.report.markup;

import java.util.*;

import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.portal.service.SearchSuggestionService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AdvancedSearchField;
import kbee.web.report.ReportBaseParameterPanel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.aerolineas.content.web.searcher.markup.DocumentSearcherPanel;
import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;

@SuppressWarnings("serial")
public class AcuseReportParametersPanel extends ReportBaseParameterPanel {
				
	//private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AcuseReportParametersPanel.class.getName());
	
	private static final long serialVersionUID = 1L;

	private Map<String, Object> parameters = new HashMap<String, Object>();
	
	private IModel<Content> model;

	final boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());


	public AcuseReportParametersPanel(String id, String reportKey) {
		super(id, reportKey);
		
		setOutputMarkupId(true);
		
		com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5<Void> close = new com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5<Void>("close") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
		};
		add(close);

		Form<?> form = new Form<Void>("form");

		DocumentSearcherPanel searcher = new DocumentSearcherPanel("searcher") {
			@Override
			@SuppressWarnings("unchecked")
			public void onSelect(AjaxRequestTarget target, Content content) {
				setContent(content);
				getParameters().put("content", content.getId());
				target.add(AcuseReportParametersPanel.this);
				setVisible(false);
				TextField<Content> field = (TextField<Content>)form.get("content");
				field.setValue(content);
				field.setFieldValue(content.getTitle());
				((AdvancedSearchField<Content>) AcuseReportParametersPanel.this.get("form:content")).setOpen(false);
				target.add(form);
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void onClose(AjaxRequestTarget target) {
				setVisible(false);
				((AdvancedSearchField<Content>) AcuseReportParametersPanel.this.get("form:content")).setOpen(false);
				target.add(form);
			}
		};
		
		
		form.add(new AdvancedSearchField<Content>("content",  new PropertyModel<Content>(this, "content")) {
			/**
			 * advanced search 
			 */
			@Override
			public void onOpenAdvancedSearch(AjaxRequestTarget target) {
				searcher.setVisible(!searcher.isVisible());
				target.add(form);
			}
			
			public void onUpdate(AjaxRequestTarget target) {
				target.focusComponent(getInput());
				setContent(getValue());
				getParameters().put("content", getValue().getId());
				target.add(AcuseReportParametersPanel.this);
			} 
			
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				List<Suggestion> suggestions = getDomain().getService(SearchSuggestionService.class).getSuggestions(pattern);
				return suggestions;
			}
		});

		form.add(searcher);

		
		form.add(new AjaxSubmitLink("submit") {
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
						
						String s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<span class=\"" +  com.novamens.wicket.markup.html.form.Form.SPINNING + " fa-fw\"></span> "+getLoading() +"'";
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

		selectedcontent.add(new Label("metadata",
				new Model<String>() {
			public String getObject() {
				return getContent()!=null?getContent().getService(ContentService.class).getPortalSubtitle():"";
			}
		}));
				
				
		
	 
		add(form);
		
	}
	 
		
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public Content getContent() {
		return model!=null ? model.getObject() : null;
	}
	
	public void setContent(Content content) {
		model = new ObjectModel<Content>(content);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
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

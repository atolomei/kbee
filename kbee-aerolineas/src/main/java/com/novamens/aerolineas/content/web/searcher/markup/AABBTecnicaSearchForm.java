package com.novamens.aerolineas.content.web.searcher.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.query.AttributeFilter;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.query.TextFilter;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.indexer.service.SuggestionService;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.searcher.searchform.AdvancedSearchClickEvent;
import kbee.web.searcher.searchform.BaseSearcherForm;

@SuppressWarnings("serial")
public class AABBTecnicaSearchForm extends BaseSearcherForm<Site> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AABBTecnicaSearchForm.class.getName());
	
	private IModel<DataSetMember> doctypemodel;
	private IModel<Classifier> document_type_classifier_model = null;
	
	
	private String code, titlefilter;
	private boolean is_initialized = false;

	public AABBTecnicaSearchForm(String id) {
		super(id);
	}

	public AABBTecnicaSearchForm() {
			super("main-searcher");
	}
	
	public AABBTecnicaSearchForm(String id, IModel<Site> model, String name) {
		super(id, model, name);
	}

	public DataSetMember getDocumentType() {
		return doctypemodel!=null?doctypemodel.getObject():null;
	}
	
	public void setDocumentType(DataSetMember type) {
		doctypemodel = type!=null ? new ObjectModel<DataSetMember>(type) : null;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getTitleFilter() {
		return titlefilter;
	}
	
	public void setTitleFilter(String value) {
		this.titlefilter = value;
	}
	
	@Override 
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (is_initialized)
			return;
		
		setOutputMarkupId(true);
		
		final Form<?> form = new Form<Void>("form");
		
		AutoCompleteFieldV5<DataSetMember> type_selector = new AutoCompleteFieldV5<DataSetMember>("documentType",  new PropertyModel<DataSetMember>(this, "documentType"), false) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.focusComponent(getInput());
				AABBTecnicaSearchForm.this.setDocumentType(getValue());
				target.add(AABBTecnicaSearchForm.this);
			}
			public int getMaxHistory() {
				return 6;
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				Classifier classifier = getDocumentTypeClassifier();
				if (classifier==null) return new ArrayList<Suggestion>();
				return classifier.getService(SuggestionService.class).getSuggestions(pattern, parameters);
			}
			@Override 
			public String getHistoryKey() {
				return "lib-portal-"+String.valueOf(AABBTecnicaSearchForm.this.getModel().getObject().getId())+"-document-type"; 
			}
		};
		
		form.add(type_selector);

		TextField<String> code = new TextField<String>("code", new PropertyModel<String>(this, "code"));
		form.add(code);
		
		TextField<String> title = new TextField<String>("title", new PropertyModel<String>(this, "titleFilter"));
		form.add(title);
		
		TextField<String> text = new TextField<String>("text", new PropertyModel<String>(this, "text"));
		form.add(text);

		AjaxSubmitLink searchbutton = new AjaxSubmitLink("submit") {
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
						String s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<span class=\"" + Form.SPINNING + " fa-fw\"></span> "+getLabel() +"'";
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
		};
		
		form.setDefaultButton(searchbutton);
		form.add(searchbutton);
		
		AjaxLink<Void> asl=new AjaxLink<Void>("advanced") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire(new AdvancedSearchClickEvent(target));
			}
			public boolean isVisible() {
				return isAdvancedSearchLinkVisible();
			}
		};
		
		Label la=new Label("slabel", new Model<String>() {
			public String getObject() {
				return getAdvancedSearchLinkLabel().getObject();
			}
		});
		
		form.add(asl);
		asl.add(la);
		
		addOrReplace(form);
		
		is_initialized  = true;
	}
	
	public Map<String, Object> getParameters() {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		List<String> members = new ArrayList<String>();
		
		try {
			DataSetMember documentType = getDocumentType();
			if (documentType!=null) {
				members.add(getPath(documentType));
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
		
		if (!members.isEmpty()) {
			parameters.put("members", members);
		}
		
		if (getCode()!=null && !"".equals(getCode())) {
			Attribute attributecode = getAttributeCode();
			if (attributecode!=null) {
				parameters.put("code", new AttributeFilter(attributecode, getCode()));
			}
		}

		if (getTitleFilter()!=null && !"".equals(getTitleFilter())) {
			parameters.put("title", new ValueFilter("titlephonetic", getTitleFilter()));
		}
		
		if (getText()!=null && !"".equals(getText())) {
			String text = getText();
			if (text!=null) {
				text = text.replace("-", " ");
			}
			// parameters.put("text", new TextFilter(text));
			parameters.put("text", getText());
		}
		
		logger.debug(parameters.toString());
		
		return parameters;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (document_type_classifier_model!= null) 
			document_type_classifier_model.detach();
		if (doctypemodel!=null) 
			doctypemodel.detach();
	}
	
	protected Classifier getDocumentTypeClassifier() {
		
		if (document_type_classifier_model==null) {
			for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
				logger.debug(classifier.getName() +" -> " + classifier.getAlias());
				if (classifier.getName()!=null && "tipo".equals(classifier.getAlias())) {
					document_type_classifier_model = new ObjectModel<Classifier>(classifier);
					break;
				}
			}
		}
		
		if (document_type_classifier_model != null)
			return document_type_classifier_model.getObject();
		
		return null;
	}
	
	
	private Attribute getAttributeCode() {
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if ("codigo".equals(attribute.getAlias())) {
				return attribute;
			}
		}
		return null;
	}
	
	private String getLabel() {
		return new StringResourceModel("working", this, null).getString();
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}

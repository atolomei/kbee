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
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.query.PhoneticTextFilter;
import com.novamens.indexer.query.Suggestion;
import com.novamens.indexer.service.SuggestionService;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.searcher.searchform.AdvancedSearchClickEvent;
import kbee.web.searcher.searchform.BaseSearcherForm;

public class JGEnergyPortalSearchForm extends BaseSearcherForm<Site> {
				
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(JGEnergyPortalSearchForm.class.getName());
	
	private IModel<DataSetMember> projectmodel;
	private IModel<DataSetMember> locationmodel;
	private IModel<DataSetMember> assetmodel;
	private IModel<DataSetMember> doctypemodel;
	
	private Classifier document_type_classifier = null;	
	private Classifier project_classifier = null;
	private Classifier location_classifier = null;
	private Classifier asset_classifier = null;
						
	
	boolean is_initialized = false;


	public JGEnergyPortalSearchForm(String id) {
		super(id);
	}

	public JGEnergyPortalSearchForm() {
			super("main-searcher");
	}
	
	public JGEnergyPortalSearchForm(String id, IModel<Site> model, String name) {
		super(id, model, name);
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
		
		this.project_classifier = null;
		this.location_classifier = null;
		this.asset_classifier = null;
		this.document_type_classifier= null;
		
		if (projectmodel!=null)	 projectmodel.detach();
		if (locationmodel!=null) locationmodel.detach();
		if (assetmodel!=null)	 assetmodel.detach();
		if (doctypemodel!=null)	 doctypemodel.detach();
	}

	
	

	// 1
	public DataSetMember getDocumentType() {return doctypemodel!=null?doctypemodel.getObject():null;}
	public void setDocumentType(DataSetMember type) {doctypemodel = type!=null ? new ObjectModel<DataSetMember>(type) : null;}
	
	// 2	
	public void setProject(DataSetMember property) {projectmodel = property!=null ? new ObjectModel<DataSetMember>(property) : null;}
	public DataSetMember getProject() {	return projectmodel!=null? projectmodel.getObject():null;}
	
	// 3										
	public void setAsset(DataSetMember property) {assetmodel = property!=null ? new ObjectModel<DataSetMember>(property) : null;}
	public DataSetMember getAsset() {	return assetmodel!=null? assetmodel.getObject():null;}

	// 4										
	public void setLocation(DataSetMember property) {locationmodel = property!=null ? new ObjectModel<DataSetMember>(property) : null;}
	public DataSetMember getLocation() {	return locationmodel!=null? locationmodel.getObject():null;}

	/**
	 * 
	 * 
	 */
	@SuppressWarnings("serial")
	@Override 
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (is_initialized)
			return;
		
		
		setOutputMarkupId(true);
		String title = getModel().getObject().getTitle();
		if (title!=null) title = title.toLowerCase().trim();
		
		final Form<?> form = new Form<Void>("form");
												
		AutoCompleteFieldV5<DataSetMember> location_selector = new AutoCompleteFieldV5<DataSetMember>("location",  new PropertyModel<DataSetMember>(this, "location"), false) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.focusComponent(getInput());
				JGEnergyPortalSearchForm.this.setLocation(getValue());
				target.add(JGEnergyPortalSearchForm.this);
			}
			public int getMaxHistory() {
				return 6;
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				return (getLocationClassifier()!=null) ? getLocationClassifier().getService(SuggestionService.class).getSuggestions(pattern, parameters) : new ArrayList<Suggestion>();

			}
			@Override 
			public String getHistoryKey() {
				return "lib-portal-"+String.valueOf(JGEnergyPortalSearchForm.this.getModel().getObject().getId())+"-location"; 
			}
		};
		form.add(location_selector);

												

		AutoCompleteFieldV5<DataSetMember> asset_selector = new AutoCompleteFieldV5<DataSetMember>("asset",  new PropertyModel<DataSetMember>(this, "asset"), false) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.focusComponent(getInput());
				JGEnergyPortalSearchForm.this.setAsset(getValue());
				target.add(JGEnergyPortalSearchForm.this);
			}
			public int getMaxHistory() {
				return 6;
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				return (getAssetClassifier()!=null) ? getAssetClassifier().getService(SuggestionService.class).getSuggestions(pattern, parameters) : new ArrayList<Suggestion>();
			}
			@Override 
			public String getHistoryKey() {
				return "lib-portal-"+String.valueOf(JGEnergyPortalSearchForm.this.getModel().getObject().getId())+"-asset"; 
			}
		};
		form.add(asset_selector);

		
		
		
		AutoCompleteFieldV5<DataSetMember> site_name_selector = new AutoCompleteFieldV5<DataSetMember>("project",  new PropertyModel<DataSetMember>(this, "project"), false) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.focusComponent(getInput());
				JGEnergyPortalSearchForm.this.setProject(getValue());
				target.add(JGEnergyPortalSearchForm.this);
			}
			public int getMaxHistory() {
				return 6;
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				return (getProjectClassifier()!=null) ? getProjectClassifier().getService(SuggestionService.class).getSuggestions(pattern, parameters) : new ArrayList<Suggestion>();
			}
			@Override 
			public String getHistoryKey() {
				return "lib-portal-"+String.valueOf(JGEnergyPortalSearchForm.this.getModel().getObject().getId())+"-project"; 
			}
		};
		form.add(site_name_selector);

		

		
		
		AutoCompleteFieldV5<DataSetMember> type_selector = new AutoCompleteFieldV5<DataSetMember>("documentType",  new PropertyModel<DataSetMember>(this, "documentType"), false) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.focusComponent(getInput());
				JGEnergyPortalSearchForm.this.setDocumentType(getValue());
				target.add(JGEnergyPortalSearchForm.this);
			}
			public int getMaxHistory() {
				return 6;
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				return (getDocumentTypeClassifier()!=null) ? getDocumentTypeClassifier().getService(SuggestionService.class).getSuggestions(pattern, parameters) : new ArrayList<Suggestion>();
			}
			@Override 
			public String getHistoryKey() {
				return "lib-portal-"+String.valueOf(JGEnergyPortalSearchForm.this.getModel().getObject().getId())+"-document-type"; 
			}
		};
		
		form.add(type_selector);
		type_selector.setVisible(false);

		
		TextField<String> text = new TextField<String>("text", new PropertyModel<String>(this, "text"));
		form.add(text);
		

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
						String s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<span class=\""+Form.SPINNING +" fa-fw\"></span> "+getLabel() +"'";
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
			DataSetMember site = getProject();
			if (site!=null) {
				members.add(getProjectClassifier().getUniqueName()+"member/"+site.getId()+"*");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		
		try {
			DataSetMember documentType = getDocumentType();
			if (documentType!=null) {
				members.add(getDocumentTypeClassifier().getUniqueName()+"member/"+documentType.getId()+"*");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		
		
		try {
			DataSetMember locationType = getLocation();
			if (locationType!=null) {
				members.add(getLocationClassifier().getUniqueName()+"member/"+locationType.getId()+"*");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		

		try {
			DataSetMember assetType = getAsset();
			if (assetType!=null) {
				members.add(getAssetClassifier().getUniqueName()+"member/"+assetType.getId()+"*");
			}
		} catch (Exception e) {
			logger.error(e);
		}
		
		
		if (!members.isEmpty()) {
			parameters.put("members", members);
		}
		

		try {
			if (getText()!=null && !"".equals(getText())) {
				//parameters.put("text", new PhoneticTextFilter(getText()));
				parameters.put("text", getText());
			}
		} catch (Exception e) {
			logger.error(e);
		}
			
		
		logger.debug(parameters.toString());
		
		return parameters;
	}

	

	
	protected Classifier getAssetClassifier() {
		if (asset_classifier != null)
			return asset_classifier;
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.getAlias()!=null && (classifier.getAlias().toLowerCase().equals("asset"))) {
				asset_classifier = classifier;
				return asset_classifier;
			}	
		}
		return asset_classifier; 
	}

	
	protected Classifier getProjectClassifier() {
		if (project_classifier != null)
			return project_classifier;
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if ( (classifier.getAlias()!=null && (classifier.getAlias().toLowerCase().equals("project"))) ||
				 (classifier.getAlias()!=null && (classifier.getAlias().toLowerCase().equals("proyecto"))) ) {
				project_classifier = classifier;
				return project_classifier;
			}	
		}
		return project_classifier; 
	}
	
	protected Classifier getLocationClassifier() {
		if (location_classifier != null)
			return location_classifier;
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.getAlias()!=null && (classifier.getAlias().toLowerCase().startsWith("loca"))) {
				location_classifier = classifier;
				return location_classifier;
			}	
		}
		return location_classifier; 
	}
	

	
	protected Classifier getDocumentTypeClassifier() {
		
		if (document_type_classifier != null)
			return document_type_classifier;
		
		if (document_type_classifier==null) {
			for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
				logger.debug(classifier.getName() +" -> " + classifier.getAlias());
				if (classifier.getName()!=null && 
						(classifier.getAlias()!=null && classifier.getAlias().equals("type")) ||
						(classifier.getAlias()!=null && classifier.getAlias().equals("documenttype")) ||
						classifier.getName().startsWith("Tipo de Documento") || 
						classifier.getName().startsWith("Tipo")) {
					document_type_classifier = classifier;
					return document_type_classifier;
				}
			}
		}
		return document_type_classifier; 
	}
	
		private String getLabel() {
		return new StringResourceModel("working", this, null).getString();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	

}

package kbee.web.searcher.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.beans.BeansService;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExternalDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;
import kbee.web.searcher.searchform.SearcherFormFactory;

import com.novamens.wicket.markup.html.form.Form.Disposition;

/**
 * 
 *   General
 *   Home blocks
 *   About
 *   
 *   Generales del Sitio
 *   
 *   about   [title, abstract, text        ]
 *   contact [title, abstract, text, email ]
 *   Site 
 *   query -> String
 *   HomeBlock 1_N
 *   footer options (lista de string, link)
 *   
 */
public class SearcherSiteEditor extends DomainObjectEditor<Site> {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherSiteEditor.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private boolean is_new;
	
	private String current_url;
	private String current_alias;
	
	private String searchForm;
	private String advancedsearchForm;
	
	public class UrlValidator implements IValidator<String> {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void validate(final IValidatable<String> validatable) {
	
			String url = validatable.getValue();
			
			if (current_url!=null && url!=null && url.trim().equals(current_url.trim()))
				return;
			
			if (url == null || url.length() == 0) {
				validatable.error(new ValidationError(this, "characters"));
				return;
			}
			
			if (!url.matches("[a-z|0-9]+")) {
				validatable.error(new ValidationError(this, "characters"));
				return;
			}
		
			boolean is_available = ServiceLocator.getService(PortalUrlService.class).isAvailableSiteUrl(url, getDomain());
			
			if (!is_available) {
				validatable.error(new ValidationError(this, "not-available"));
				return;
			}
		}
	}
	

	
	public class AliasValidator implements IValidator<String> {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void validate(final IValidatable<String> validatable) {
	
			String url = validatable.getValue();
			
			if (current_alias!=null && url!=null && url.trim().equals(current_alias.trim()))
				return;
			
			if (url == null || url.length() == 0) {
				validatable.error(new ValidationError(this, "characters"));
				return;
			}
			
			boolean is_available = ServiceLocator.getService(PortalUrlService.class).isAvailableSiteAlias(url, getDomain());
			
			if (!is_available) {
				validatable.error(new ValidationError(this, "not-available"));
				return;
			}
		}
	}


	/**
	 * @param id
	 * @param model
	 * @param isNew
	 */
	public SearcherSiteEditor(String id, IModel<Site> model, boolean isNew) {
		super(id, model);
	
		setNew(isNew);
		setEditionEnabled(isNew);

		Json json;
		String search_form,  advanced_search_form;
		
		try {
			json = getModel().getObject().getCustomValuesJson();
			search_form =  json.getString("search-form");
			advanced_search_form =  json.getString("advanced-search-form");
		}
		catch (Exception e) {
			logger.error(e);
			json = new KbeeJson();
			search_form = null;
			advanced_search_form = null;
		}
		
		setSearchForm(search_form);
		setAdvancedSearchForm(advanced_search_form);
		
		this.current_url = model.getObject().getUrl();
		this.current_alias = model.getObject().getKey();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("title", true));
		form.add(new StaticField<String>("id", new Model<String>(model.getObject().getId().toString())));
		
		TextField<String> tfurl = new TextField<String>("URI", true, new UrlValidator());
		form.add(tfurl);
		
		form.add(new BooleanField("public") {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getFalseStr() {
				return "Private";
			}
			@Override
			protected String getTrueStr() {
				return "Public";
			}
		});
		
		
		form.add(new TextField<String>("subtitle"));
		form.add(new TextAreaField<String>("description", 8, 40));
		
		ChoiceField<String> cf= new ChoiceField<String>("searchForm", new PropertyModel<String>(this, "searchForm"), new PropertyModel<List<String>>(this, "searchForms")) {
			private static final long serialVersionUID = 1L;
			@Override
				public void onUpdate(AjaxRequestTarget target) {
					setSearchForm(super.getValue());
					setUpdatedPart("search form " + super.getValue());
				}
			};
		form.add(cf);
		
		
		ChoiceField<String> acf= new ChoiceField<String>("advancedSearchForm", new PropertyModel<String>(this, "advancedSearchForm"), new PropertyModel<List<String>>(this, "searchForms")) {
			private static final long serialVersionUID = 1L;
			@Override
				public void onUpdate(AjaxRequestTarget target) {
					setAdvancedSearchForm(super.getValue());
					setUpdatedPart("advanced search form " + super.getValue());
				}
			};
		form.add(acf);

		
		
		
		TextField<String> f_email = new TextField<String>("emailContact") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return !SearcherSiteEditor.this.getModel().getObject().isExternal();
			}
		};
		form.add(f_email);

		add(form);

		add(new EditButtonsV5<Site>(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !isReadOnly();
			}
		});
	}
	

	
	protected IModel<DataSetMember> getSiteDataSetMemberModel() {
		DataSet dataset = getExternalDao().getSiteDataSet(getDomain());
		DataSetMember  member = getExternalDao().findMemberByExternalId(getModel().getObject().getOId(), dataset);
		if (member!=null)
			return new ObjectModel<DataSetMember>(member);
		return null;
	}
	
	private ExternalDao getExternalDao() {
		return (ExternalDao) ServiceLocator.getService(BeansService.class).getBean("externalDao");
	}


	public void onEdit(IModel<Site> model) {
	}

	public void onUpdate(AjaxRequestTarget target) {
	}

	public void onCancel(AjaxRequestTarget target) {
	}

	public void update(AjaxRequestTarget target) {
		try {

			 if (!getUpdatedParts().isEmpty()) {
				 
				Json json = getModel().getObject().getCustomValuesJson();
				
				json.put("search-form", getSearchForm());
				json.put("advanced-search-form", getAdvancedSearchForm());
				
				((KbeeSite) getModel().getObject()).setCustomValuesJson(json);

				if (getModel().getObject().getKey()==null && getModel().getObject().getTitle()!=null)
					((KbeeSite) getModel().getObject()).setKey(getModel().getObject().getTitle().replace(" ",  "").toLowerCase());
				
				((KbeeSite) getModel().getObject()).setCustomValuesJson(json);
				
				
				getModel().getObject().getService(SiteService.class).update(getUpdatedParts());
				target.add(this);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	protected Domain getDomain() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		} catch (Exception e) {
			logger.error(e);
			return null;
		} 
	}

	
	public void setNew(boolean is_new) {
		this.is_new = is_new;
	}

	public boolean isNew() {
		return is_new;
	}

	private List<String> blist;
	

	
/**
 * 
 * 	<bean id="search-external" class="com.novamens.kbee.portal.model.factory.KbeeBlockFactory">
		<property name="id" value="search-external"/>
		<property name="name" value="Buscador externo"/>
		<property name="className" value="com.novamens.kbee.portal.model.KbeeBlockSearchExternal"/>
		<property name="usage" value="Buscador externo."/>
		<property name="block_intro_visible" value="false"/>
		<property name="block_title_visible" value="false"/>
	</bean>

 * 
 * @return
 */
	public List<String> getSearchForms() {
	
		if (blist!=null)
			return blist;
		
		blist = new ArrayList<String>();
		java.util.Map<String, kbee.web.searcher.searchform.SearcherFormFactory> beans = ServiceLocator.getService(BeansService.class).getBeansOfType(kbee.web.searcher.searchform.SearcherFormFactory.class);
		for (Entry<String, SearcherFormFactory> entry: beans.entrySet()) {
			if (entry.getValue().getDomainName()==null || entry.getValue().getDomainName().equals(getDomain().getName()))
				blist.add(entry.getKey());
		}
		Collections.sort(blist);
		return blist;
	}
	
	public String getSearchForm() {
		return this.searchForm;
	}
	
	public void setSearchForm(String s) {
		this.searchForm=s;
	}


	public String getAdvancedSearchForm() {
		return this.advancedsearchForm;
	}
	
	public void setAdvancedSearchForm(String s) {
		this.advancedsearchForm=s;
	}

	
}

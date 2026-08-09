package kbee.web.searcher.editor;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.library.Library;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.library.IqlCriteria;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;
import kbee.web.portal6.DomainSearcherPortalService;

import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.model.ObjectModel;


public class SearcherSiteIqlEditor extends DomainObjectEditor<Site> {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherSiteIqlEditor.class.getName());
	
	private String iql;

	private boolean isDisplayValidVersion;	
	private IModel<Library> ml;

	private Boolean isAudit = Boolean.valueOf(true);
	

	private Form<?> form;

	
	public SearcherSiteIqlEditor(String id, IModel<Site> model) {
		super(id, model);
	}
	
	
	public void onDetach() {
		super.onDetach();
		if (this.ml!=null)
				this.ml.detach();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		
		Json json;
		String site_iql;
		String library;
			
		try {
			json = getModel().getObject().getCustomValuesJson();
		
			site_iql = (String) json.get("iql");
			library  = (String) json.get("library");
			isAudit = ( (json.get("audit") == null) ? Boolean.valueOf(true) : Boolean.valueOf(json.get("audit").equals("true"))); 
			
		}
		catch (Exception e) {
			logger.error(e);
			
			json = new KbeeJson();
			site_iql = null;
			library  =null;
		}
		
		setIql(site_iql !=null ? site_iql : getDomain().getService(DomainSearcherPortalService.class).getDefaultSearcherPortalIql(getModel().getObject()));
		setDisplayValidVersion(getModel().getObject().isDisplayValidVersion());
		
		
		if ((library!=null) && !library.equals("null")) {
			try {
			 setLibrary( getRepository(Library.class).findById(Long.valueOf(library)));
			} catch (Exception e) {
				logger.error(e);
			}
		}
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ChoiceField<Library>( 
				"library", 
				new PropertyModel<Library>(this, "library"),	
				new PropertyModel<List<Library>>(this, "libraries"), true) {
				private static final long serialVersionUID = 1L;

			
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				Library source = getValue();
				if (source!=null && (source.getCriteria() instanceof IqlCriteria)) { 
					setIql(((IqlCriteria) source.getCriteria()).getStatement());
					((TextAreaField<String>) form.get("iql")).setValue( getIql());
				}

				/**
				if (source.equals(all))
					getBrowser().getQuery().getParameters().remove("status");

				else if (source.equals(draft))
					getBrowser().getQuery().getParameters().put("status", String.valueOf(ObjectState.DRAFT.getId()));

				else if (source.equals(enabled))
					getBrowser().getQuery().getParameters().put("status", String.valueOf(ObjectState.ENABLED.getId()));

				else if (source.equals(archived))
					getBrowser().getQuery().getParameters().put("status", String.valueOf(ObjectState.ARCHIVED.getId()));

				 	**/
				
				target.add( SearcherSiteIqlEditor.this);
				}
			
			@Override
			public String getIdValue(Library value) {
				if (value==null)
					return "none";
				return value.getId().toString();
			}

			@Override
			public String getDisplayValue(Library value) {
				if (value==null)
					return "none";
				return value.getDisplayName();
			}
		});
		
		
		form.add(new TextAreaField<String>("iql", new Model<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				return getIql();
			}
			
			@Override
			public void setObject(String o) {
				setIql(o);
			}
		}, 8, 80));
		
								
		
		form.add(new BooleanField("audit", new PropertyModel<Boolean>(this, "audit")) {
			protected String getFalseStr() {
				return new StringResourceModel("no", this, null).getString();
			}
			protected String getTrueStr() {
				return new StringResourceModel("yes", this, null).getString();
			}
		});
		
		
		/**
		form.add(new BooleanField("displayValidVersion", new PropertyModel<Boolean>(this, "displayValidVersion")) {
			protected String getFalseStr() {
				return "Head";
				// return new StringResourceModel("no", this, null).getString();
			}
			protected String getTrueStr() {
				return "Valid";
				// return new StringResourceModel("yes", this, null).getString();
			}
			
		});
		**/
		add(form);

		add(new EditButtonsV5<Site>(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !isReadOnly();
			}
		});
	}


	public String getIql() {
		return iql;
	}


	public void setIql(String iql) {
		this.iql = iql;
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
				
				if (getLibrary()!=null) {
					json.put("library", getLibrary().getId().toString() );
					//json.put("iql", getLibrary().getCriteria().getStatement());
				}
				json.put("iql", getIql()==null?"null":getIql());
				
				if (getAudit()!=null) {
					json.put("audit", getAudit() ? "true" : "false"); 	 
				}
				
				//json.put("iql", "null");
				KbeeSite ksite = (KbeeSite) getModel().getObject();
				ksite.setCustomValuesJson(json);
				ksite.setDisplayValidVersion(this.isDisplayValidVersion());
				ksite.getService(SiteService.class).update(getUpdatedParts());
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	

	
	public List<Library> getLibraries() {
		return getContentDao().getLibraries(getDomain(), ObjectState.ENABLED);
	}
	


	public Library getLibrary() {
		if (ml==null)
			return null;
		return ml.getObject(); 
	}

	public boolean isDisplayValidVersion() {
		return this.isDisplayValidVersion;
	}
	
	public void setDisplayValidVersion(boolean b) {
		this.isDisplayValidVersion=b;
	}
	
	

	public Boolean getAudit() {
		return isAudit;
	}


	public void setAudit(Boolean isAudit) {
		this.isAudit = isAudit;
	}
	
	public void setLibrary(Library l) {
		if (ml==null)
			ml=new ObjectModel<Library>(l);
		else
			ml.setObject(l); 
	}
	
	
	
	
	protected Domain getDomain() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	

}

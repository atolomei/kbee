package kbee.web.searcher.editor;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.portal.model.KbeeBlock;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;

import com.novamens.wicket.markup.html.form.NumberField;


/**
 *
 */
public class SearcherSiteHomeBlockEditor extends DomainObjectEditor<Block> {
			
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherSiteHomeBlockEditor.class.getName());

	private IModel<Site> siteModel;
	private String iql;					//
	private String sort; 				// order by query
	private String orderSet; 			// sort results 
	private Boolean includeMetadata; 	//
	
	
	public SearcherSiteHomeBlockEditor(String id, IModel<Block> model, IModel<Site> site_model) {
		super(id, model);
		setSiteModel(site_model);
	}
	
	@SuppressWarnings("serial")
	private void addEditor() {
			
		Json json;
		String site_iql, imd, st, os;
		
		try {
			json = getModel().getObject().getCustomValuesJson();
			site_iql = (String) json.get("iql");
			imd = (String) json.get("include-metadata");
			st = json.getString("sort");
			os =  json.getString("order-set");
			
		}
		catch (Exception e) {
			logger.error(e);
			json = new KbeeJson();
			site_iql = null;
			imd = null;
			st =null;
			os= null;
		}
		
		setIncludeMetadata(Boolean.valueOf(imd!=null && imd.trim().toLowerCase().equals("yes")));
		setIql(site_iql !=null ? site_iql : null);
		setSort(st!=null?st:"modified");
		setOrderSet(os!=null?os:"modified");
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		add(form);
		
		TextField<String> f_title 		= new TextField<String>("title");
		BooleanField f_meta				= new BooleanField("includeMetadata", new PropertyModel<Boolean>(this, "includeMetadata"));
		
		NumberField<Integer> f_maxelement 		= new NumberField<Integer>("maxElements");
		
		TextField<String> f_subtitle 	= new TextField<String>("subtitle");
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
		
		form.add(f_title);
		form.add(f_subtitle);
		form.add(f_maxelement);
		form.add(f_meta);

		
		// Sort query == "Order by"
		form.add(
				new ChoiceField<String> ("sort", new PropertyModel<String>(this, "sort"), new PropertyModel<List<String>>(this,"sortModes"), true) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						setSort(super.getValue());
					}
				});


		
		// Sort query == "Order by"
		form.add(new ChoiceField<String> ("orderSet", new PropertyModel<String>(this, "orderSet"), new PropertyModel<List<String>>(this,"orderSetModes"), true) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				
				logger.debug(getValue());
				
				setOrderSet(super.getValue());
			}
		});

		
		
		EditButtonsV5<Block> buttons = new EditButtonsV5<Block>(this) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isEnabled() {
				return !isReadOnly();
			}
		};
		
		add(buttons);

		/**
		 * 
		ListModel<Panel> ldp = new ListModel<Panel>(new Model<Panel>(this), "panels");
		ListView<Panel> ldata = new ListView<Panel>("block-iql-groups", ldp) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<Panel> item) {
				try {
					item.add(item.getModelObject());
					item.setOutputMarkupId(true);
					
				}  catch (Exception e) {
					logger.error(e);
					 item.setVisible(false);
				}
			}
		};

		add(ldata);
		ldata.setOutputMarkupId(true);
		*/
		
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
	
		//WebMarkupContainer container = new WebMarkupContainer("container");
		//add(container);
		
		addEditor();
		
		
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				Json json = getModel().getObject().getCustomValuesJson();
				json.put("iql", getIql());
				json.put("sort", getSort());
				json.put("order-set", getOrderSet());
				json.put("include-metadata", getIncludeMetadata().booleanValue()?"yes":"no");
				logger.debug(json.toString());
				KbeeBlock kb = (KbeeBlock) getModel().getObject();
				kb.setCustomValuesJson(json);
				
				
				getSiteModel().getObject().getService(SiteService.class).update(getUpdatedParts());
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
		

	public Boolean getIncludeMetadata() {
		return this.includeMetadata;
	}

	public void setIncludeMetadata(Boolean b) {
		this.includeMetadata=b;
	}


	public IModel<Site> getSiteModel() {
		return siteModel;
	}

	public void setSiteModel(IModel<Site> siteModel) {
		this.siteModel = siteModel;
	}

	public String getIql() {
		return iql;
	}


	public void setIql(String iql) {
		this.iql = iql;
	}
	
	public String getOrderSet() {
		return this.orderSet;
	}


	public void setOrderSet(String iql) {
		this.orderSet = iql;
	}
	
	
	public String getSort() {
		return this.sort;
	}
	
	public void setSort(String sort) {
		this.sort=sort;
	}

	public List<String> getOrderSetModes() {
		List<String> list = new ArrayList<String>();
		list.add("modified");
		list.add("title");
		list.add("none");
		return list;
	}

		
	
	public List<String> getSortModes() {
		List<String> list = new ArrayList<String>();
		list.add("none");
		list.add("modified");
		list.add("title");
		return list;
	}

	
	
}

package kbee.web.searcher.editor;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.model.ListModel;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.searcher.searchform.SearcherFormFactory;


/**
 * List de Panel 
 * 1 x cada Block
 * 
 * Block
 * List de panel
 * 1 por cada 
 *
 */							
public class SearcherSiteHomeEditor extends DomainObjectEditor<Site> {
			
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherSiteHomeEditor.class.getName());

	IModel<Site> siteModel;
	
	private Long editing;
	
	

	public class HomeBlockViewFragment extends Fragment {
		private static final long serialVersionUID = 1L;
		IModel<Block> model;
		public void setModel(IModel<Block> model) {
			this.model=model;
		}
		
		public HomeBlockViewFragment (String id, IModel<Block> model) {
			super(id, "home-block-view-fragment", SearcherSiteHomeEditor.this);
			HomeBlockViewFragment.this.setModel(model);
			
			AjaxLink<Void> link = new AjaxLink<Void>("title-link") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (getEditingId()!=null && getEditingId().equals((Long) HomeBlockViewFragment.this.model.getObject().getId()))
						setEditingId(Long.valueOf(-1));
					else
						setEditingId((Long) HomeBlockViewFragment.this.model.getObject().getId());
					target.add(SearcherSiteHomeEditor.this);
				}
			};
			add(link);	
			String iql= (String) model.getObject().getCustomValuesJson().get("iql");
			link.add(new Label("title", model.getObject().getTitle()!=null ? model.getObject().getTitle() : model.getObject().getId().toString()));
			//link.add(new Label("iql",  iql));
		}
	}
	
	
	
	
	
	 
	public SearcherSiteHomeEditor(String id, IModel<Site> model) {
		super(id, model);
		setSiteModel(model);
	}
	
	
	
	// TODO VER AT SITE
	public List<Block> getBlocks() {
		List<Block> list = new ArrayList<Block>();
		if (getModel().getObject().getHomePage()!=null && getModel().getObject().getHomePage().getPageSections().get(0).getArea(0)!=null) {
			for (Block block: getModel().getObject().getHomePage().getPageSections().get(0).getArea(0).getBlocks())
				list.add(block);
		}
		return list;
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		
		
		
		ListModel<Block> ldp = new ListModel<Block>(new Model<Panel>(this), "blocks");
		
		ListView<Block> ldata = new ListView<Block>("home-blocks", ldp) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<Block> item) {
				try {
					Component viewer=new HomeBlockViewFragment("home-block-view-fragment", item.getModel());
					item.add(viewer);
					item.setOutputMarkupId(true);
					SearcherSiteHomeBlockEditor be = new SearcherSiteHomeBlockEditor("home-block-editor", item.getModel(),  getSiteModel());
					be.setVisible(getEditingId()!=null && getEditingId().equals((Long) item.getModel().getObject().getId()));
					// be.setVisible(true);
					item.add(be);
					
				}  catch (Exception e) {
					logger.error(e);
					item.setVisible(false);
				}
			}
		};

		add(ldata);
		ldata.setOutputMarkupId(true);
	}
	
	public IModel<Site> getSiteModel() {
		return siteModel;
	}

	public void setSiteModel(IModel<Site> siteModel) {
		this.siteModel = siteModel;
	}

	public void setEditingId(Long lo) {
		this.editing=lo;
	}
	
	public Long getEditingId() {
		return this.editing;
	}
	
	
	public List<String> getSearchForms() {

		List<String> list = new ArrayList<String>();
		return list;
		
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

package kbee.web.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.XArray;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.page.ConsoleSectionHomePage;
import kbee.web.page.PageContentHeaderPanel;
import kbee.web.panel.ListSimplePanel;
import kbee.web.service.ReportsLibraryService;


public class ReportsHomePage extends ConsoleSectionHomePage<Void> {
				
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportsHomePage.class.getName());
	
	private List<ReportGroupList> rg_list = null;
	private  Map<String, ReportGroupList> repo_map = new HashMap<String, ReportGroupList>();
	

	
	/**
	 * 
	 * 
	 */
	public class ReportGroupList implements Serializable {

		private static final long serialVersionUID = 1L;
		
		String reportGroup;
		String name;
		List<ReportFactory> factories = new ArrayList<ReportFactory>();
		
		public ReportGroupList(String reportGroup) {
			this.reportGroup=reportGroup;
			this.name = ServiceLocator.getService(LanguageService.class).getString(reportGroup, getSessionUser().getLocale());
		}
		
		public String getName() {
			return this.name;
		}
		
		public void addFactory(ReportFactory factory) {
			factories.add(factory);
		}
		
		public List<ReportFactory> getFactories() {
			return factories;
		}
		
		public String getGroupName() {
			return reportGroup;
		}
	}
	
	
	public ReportsHomePage() {
	}
	

	
	/**
	 * @return
	 */
	public List<ReportGroupList> getItems() {
	
		if (rg_list!=null)
			return rg_list;
		
		List<ReportFactory> factories = ServiceLocator.getService(ReportsLibraryService.class).getUserSessionReports();
		
		logger.debug( "factories.size() -> " + factories.size());
		
		for (ReportFactory factory : factories) {
			String reportGroup = factory.getReport().getReportGroup();
			if(!repo_map.containsKey(reportGroup)) {
				repo_map.put(reportGroup, new ReportGroupList(reportGroup));
			}
			repo_map.get(reportGroup).addFactory(factory);
		}
		

		rg_list = new ArrayList<ReportGroupList>();
		
		for (Entry<String, ReportGroupList> entre: repo_map.entrySet()) 
			rg_list.add(entre.getValue());
		
		rg_list.sort(new Comparator<ReportGroupList>() {
			@Override
			public int compare(ReportGroupList a, ReportGroupList b) {
				try {
					return a.getName().compareToIgnoreCase(b.getName());
				} catch (Exception e) {
					return 0;	
				}
			}
		});
		
		logger.debug("getItems() -> " + rg_list.size());
		
		return  rg_list;
	}

	
	public List<IModel<XArray>> getReports(ReportGroupList rp) {
		
		List<IModel<XArray>> xlist = new ArrayList<IModel<XArray>>();
		
		for (ReportFactory r: rp.getFactories()) {
			
			logger.debug(r.getDisplayName());
			
			XArray da= new XArray(r.getDisplayName(), 		// label          -> Display name		
								  r.getDisplayName(), 		// label          -> Sort Display name
								  "",						// quantity
								  r.getReportAbstract(),    // description    -> description
								  r.getKey(),  				// key	          -> Report "key"
						          r.getReportGroup()); //   // URL            -> report group
			
			xlist.add(new Model<XArray>(da));
		}
		
		xlist.sort(new Comparator<IModel<XArray>>() {
			@Override
			public int compare(IModel<XArray> a, IModel<XArray> b) {
				try {
					return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
				} catch (Exception e) {
					return 0;	
				}
			}
		});

		logger.debug( rp.getName()+ " -> " + xlist.size());
		return xlist;
	}
	

	
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		setTopNavigation(getMainTopbar());
		setMenu(getMainLaternalMenu());

		PageContentHeaderPanel<Void> panel=new PageContentHeaderPanel<Void>(null);
		setPageTitle(new StringResourceModel("bc.reports", this, null));
		panel.setTitle(new StringResourceModel("bc.reports", this, null));
		panel.setBreadcrumbPanel(getHeaderPanelBreadcrumbPanel());
		
		setSuggester(false); 
		setSearchPanel(false);
		setAdvancedSearch(false); 
		setPageContentHeader(panel);
		
		repo_map.clear();
		
		WebMarkupContainer w = new WebMarkupContainer("rg-container");
		add(w);
		w.setOutputMarkupId(true);
				
		ListView<ReportGroupList> reps = new ListView<ReportGroupList>("report-group", new ListModel<ReportGroupList>(new Model<Page>(this), "items")) {
			private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<ReportGroupList> item) {
					ListSimplePanel<XArray> pa = getLSPanel("reports",  "reports-group-"+ String.valueOf(item.getIndex()), item.getModelObject());
					item.add(pa);
		}};
		
		reps.setOutputMarkupId(true);
		w.add(reps);
	}
	
	protected ListSimplePanel<XArray> getLSPanel(String id, String key, ReportGroupList rg) {
		
		ListSimplePanel<XArray> pa = new ListSimplePanel<XArray>(id,  key, getReports(rg)) {
			private static final long serialVersionUID = 1L;
			
			public void onClick(IModel<XArray> modelObject) {
				setResponsePage(new RedirectPage("/reports/"+modelObject.getObject().getKey() +"/"+  modelObject.getObject().getQuantity()));
			}
			
			@Override
			protected void onClickExpand(AjaxRequestTarget target, int index) {
				target.add( ReportsHomePage.this.get("rg-container"));
			}

			
			@Override
			protected IModel<String> getItemLabelMeta(IModel<XArray> modelObject) {
				return null;	
			}
			
			@Override
			protected IModel<String> getItemAbstract(IModel<XArray> modelObject) {
				return new Model<String>(modelObject.getObject().getDescription());
				}
		};
		pa.setOutputMarkupId(true);
		pa.setExpand(true);
		pa.setTitle(new Model<String>(rg.getName()));
		return pa;
	}



	/**
	 * @return
	 */
	protected Panel getHeaderPanelBreadcrumbPanel() {
		try {
			MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
			bc.addElement(new ReportsDropdownBC());
			bc.addElement(new BCElement( new StringResourceModel("bc.reports", this, null)));
			 
			return bc;
		} catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("breadcrumb");
		}
	}

	
		
		/**
		 * 
		 * public IModel<String> getReportDescription(String reportKey) {
		 * String paramName = "report." + reportKey +".description";
		 
		String s=ServiceLocator.getService(SystemParameterService.class).getParameter(paramName, null);
		if (s!=null)
			return new Model<String>(s);
		return new Model<String>(paramName);
		}
		*/
	


	@Override
	public void onDetach() {
		super.onDetach();
		
		repo_map.clear();
		rg_list=null;
		
		//if (list!=null)
		//	list.forEach(item -> item.detach());
	}

	
}

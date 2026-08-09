package kbee.web.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.security.User;
import com.novamens.service.LanguageService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.XArray;
import com.novamens.wicket.markup.html.panel.BCTitleElementBC;

import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.ReportsSectionBC;
import kbee.web.nav.SeparatorBC;

import kbee.web.service.ReportsLibraryService;


public class ReportsDropdownBC extends DropDownMenuBC<Void> {
				
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportsDropdownBC.class.getName());	
	
	private class ReportGroupList implements Serializable {

		private static final long serialVersionUID = 1L;
		
		String reportGroup;
		String name;
		List<ReportFactory> factories = new ArrayList<ReportFactory>();
		
		public ReportGroupList(String reportGroup) {
			this.reportGroup=reportGroup;
			this.name = ServiceLocator.getService(LanguageService.class).getString(reportGroup, getSessionUser().getLocale());
		}
		
		public String getGroupName() {
			return this.reportGroup;
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
		
		
	}

	
	public ReportsDropdownBC() {


		addElement(new ReportsSectionBC(), true);
		
		addElement(new ReportsSectionBC());
		addElement(new ReportsSubscriptionBC());
		
		for (ReportGroupList rg: getItems()) {
		
			addElement(new SeparatorBC());
			addElement(new BCTitleElementBC( new Model<String>(rg.getName())));
			
			List<ReportFactory> li = rg.getFactories();
			li.sort(new Comparator<ReportFactory>() {
				@Override
				public int compare(ReportFactory o1, ReportFactory o2) {
					try {
						return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
					} catch (Exception e) {
						logger.error(e);
						return 0;
					}
				}
			});
			for (ReportFactory rf: li) {
				addElement(new ReportBC(rf));
			}
		}
		
		
	}
	
	
	
	private List<ReportGroupList> rg_list = null;
	private  Map<String, ReportGroupList> repo_map = new HashMap<String, ReportGroupList>();
	
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
		
		for (Entry<String, ReportGroupList> entre: repo_map.entrySet()) {
			rg_list.add(entre.getValue());
		}
		
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
								  r.getDisplayName(), 		// label          -> sort name
								  "", 
								  r.getReportAbstract(),    // description    -> description
								  r.getKey(),  				// value          -> Report "key"
						          r.getReportGroup()); //   // key            -> report group
			
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

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
		
}

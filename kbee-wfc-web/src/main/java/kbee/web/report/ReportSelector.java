package kbee.web.report;


import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;

import kbee.web.console.Console;
import kbee.web.service.ReportsLibraryService;


@Deprecated
public class ReportSelector extends Panel {
			
	private static final long serialVersionUID = 1L;
																							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportSelector.class.getName());

	private IModel<ReportFactory> model;

	List<ReportFactory> list = null;
	
	
	public ReportSelector(Console<?> console) {
		this("report", console);
	}

	public ReportSelector(String id, Console<?> console) {
		super(id);
		
		setModel(console);

		List<ReportFactory> factoriesSameGroup = getFactoriesSameGroup(console.getDisplayName().getObject());
		if (factoriesSameGroup ==null || factoriesSameGroup.size()==0)
			add(new InvisiblePanel("report"));
		else {
			add(new ExtendedChoiceField<ReportFactory>("report", getModel(), ()-> getFactoriesSameGroup(console.getDisplayName().getObject())) {
				private static final long serialVersionUID = 1L;
				public void onUpdate(AjaxRequestTarget target) {
					
					//getValue();
					 //PageParameters pageParameters = new PageParameters();
	                  //pageParameters.set("reportGroup", group);
					//  THIS IS NOT USED ANYMORE		 
					//setResponsePage(new ReportPage(getValue()));
				}
				@Override
				public String getDisplayValue(ReportFactory value) {
					return value.getDisplayName();
				}
			});
		}
	}
	
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
		this.list=null;
	}

	
	public void setModel(Console<?> console) {
		for (ReportFactory factory : getFactoriesSameGroup(console.getDisplayName().getObject())) {
			if (factory.getReport().getDisplayName().getObject().equals(console.getDisplayName().getObject())) {
				model = new Model<ReportFactory>(factory);
				break;
			}
		}
	}

	public IModel<ReportFactory> getModel() {
		return model;
	}
	

	
	
	public List<ReportFactory> getFactoriesSameGroup(String consoleDisplayName) {

		if (list!=null)
			return list;
		
		list = ServiceLocator.getService(ReportsLibraryService.class).getUserSessionReports();

		
		//if (logger.isDebugEnabled()) {
		//	list.forEach(item -> logger.debug(item.getReport().getReportGroup()+ " | " + item.getKey()));
		//}
		
		try {
			
			ReportFactory currentReportFactory = list.stream().filter(rep -> rep.getReport().getDisplayName().getObject().equals(consoleDisplayName)).findFirst().get();
			String currentReportGroup =currentReportFactory.getReport().getReportGroup();
			list = list.stream().filter(rep ->currentReportGroup.equals(rep.getReport().getReportGroup())).collect(Collectors.toList());
			
			list.sort(new Comparator<ReportFactory>() {

				@Override
				public int compare(ReportFactory o1, ReportFactory o2) {
					try { 
						return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
					} catch (Exception e) {
						logger.error(e);
					}
					return 0;
				}
				
				
			});
			
		} catch (Exception e) {
			logger.error(e);
		}

		return list;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
}

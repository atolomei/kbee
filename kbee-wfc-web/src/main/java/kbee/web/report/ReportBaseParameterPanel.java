package kbee.web.report;

import java.time.OffsetDateTime;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.service.ServiceLocator;

public abstract class ReportBaseParameterPanel extends ConsoleSidePanel {
					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportBaseParameterPanel.class.getName());
	
	private static final long serialVersionUID = 1L;

	private Map<String, Object> parameters ;
	
	private OffsetDateTime from, to;

	private String reportKey;

	//public ReportBaseParameterPanel(String id) {
	//	super(id);
	//}

	
	public ReportBaseParameterPanel(String id,  String reportKey) {
		this(id, reportKey, new HashMap<String, Object>());
	}
	public ReportBaseParameterPanel(String id,   String reportKey, Map<String, Object> map) {
		super(id);
		setParameters(map);
		this.reportKey=reportKey;
		setOffsetDateTimeFrom(OffsetDateTime.now().minusDays(OffsetDateTime.now().getDayOfMonth()-1));
		setOffsetDateTimeTo(OffsetDateTime.now());
	}


	public String getReportKey() {
		return this.reportKey;
	}
	

	
	public void setOffsetDateTimeFrom(OffsetDateTime date) {
		this.parameters.put("from", date);
		this.from = date;
	}
	
	public OffsetDateTime getOffsetDateTimeTo() {
		return to;
	}
	
	public void setOffsetDateTimeTo(OffsetDateTime date) {
		this.parameters.put("to", date);
		this.to = date;
	}
	public OffsetDateTime getOffsetDateTimeFrom() {
		return from;
	}
	

	public void setParameters(Map<String, Object> map) {
        parameters = map;
	}
	
	
	public Map<String, Object> getParameters() {
        return parameters;
	}

	
	
	@Override
	public void onClose(AjaxRequestTarget target) {
		
	}
	
	protected void onChange(AjaxRequestTarget target, Map<String, Object> parameters) {
		
	}
	
	protected void onSubmit(AjaxRequestTarget target, Map<String, Object> parameters) {
		
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isDomainKbee() {
		try {
			return getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	
	protected String getLoading() {
		return new StringResourceModel("loading", this, null).getObject();
		
	}

}

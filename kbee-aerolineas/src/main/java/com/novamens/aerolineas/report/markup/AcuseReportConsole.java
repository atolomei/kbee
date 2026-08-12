package com.novamens.aerolineas.report.markup;



import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.SidePanelEvent;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;
import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;

import kbee.web.report.ReportConsole;
import kbee.web.report.Row;




/**
 * 
 * Acuse de Recibo de 1 Documento
 * ------------------------------
 * 
 * Acuse de Recibo de 1 Usuario Todo lo que puso OK
 * Acuse de Recibo de 1 Usuario Todo lo que nos Debe
 *
 */
@SuppressWarnings("serial")
public class AcuseReportConsole extends ReportConsole {
																							
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AcuseReportConsole.class.getName());

	private static final long serialVersionUID = 1L;
	

	private List<GridColumn<SearchResult,String>> columns = null;

	public AcuseReportConsole() {
			super("acuse-report", null, "AcuseReport");
			//setConsole("AcuseReport");
			setReportGroup(AUDIT);
	}
	
	
	public AcuseReportConsole(String key) {
		super("AcuseReport", null, key);
		// setConsole("AcuseReport");
		setReportGroup(AUDIT);
	}

	public List<GridColumn<SearchResult, String>> getColumns() {
		if (this.columns!=null)
			return this.columns;
		
        this.columns = new ArrayList<GridColumn<SearchResult,String>>();
		
		this.columns.add(new InnerStringColumn("datepublish", getLabel("column.datepublish")) {
			@Override
			protected IModel<String> getLabelModel(Row row) {
				try {
					OffsetDateTime date = (OffsetDateTime)row.getValue("datepublish");
					if (date!=null) {
						String str=ServiceLocator.getService(DateTimeService.class).formatTZ(date,getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Day_Month_Year_hh_mm_ss);
						return new Model<String>(str);
					}
					return new Model<String>("");
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
		});
													
		this.columns.add(new InnerStringColumn("user", getLabel("column.user"), "user"));
		this.columns.add(new InnerStringColumn("user-firstname", getLabel("column.user-firstname"), null));
		this.columns.add(new InnerStringColumn("group", getLabel("column.group"), "group"));
		this.columns.add(new InnerStringColumn("dateread", getLabel("column.dateread"), "dateread") {
			@Override
			protected IModel<String> getLabelModel(Row row) {
				try {
					OffsetDateTime date = (OffsetDateTime)row.getValue("dateread");
					if (date!=null) {
						String str=ServiceLocator.getService(DateTimeService.class).formatTZ(date,getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Day_Month_Year_hh_mm_ss);
						return new Model<String>(str);
					}
					return new Model<String>("");
				} catch (Exception e) {
					logger.error(e);
					return new Model<String>(e.getClass().getSimpleName());
				}
			}
		});


		return this.columns;
	}
	
	/**
	 * 	<bean id="AerolineasAcuseReportFactory" class="...ReportFactory">
		<constructor-arg><value>notification-accepted</value></constructor-arg>
		<constructor-arg><value>AerolineasAcuseReport</value></constructor-arg>
	</bean>
	<bean id="AerolineasAcuseReport" class="com.novamens.aerolineas.report.markup.AcuseReportConsole" scope="prototype"/>

	 */
	@Override
	public IModel<String> getDisplayName() {										
		return new Model<String>(ServiceLocator.getService(LanguageService.class).getString("notification-accepted", getSessionUser().getLocale()));
	}


	@Override
	public String getGridExportTitle() {
		Content content = ((AcuseQuery) getQuery()).getContent();
		if (content!=null) {
			String str=ServiceLocator.getService(DateTimeService.class).formatTZ( content.getLastModifiedOffsetDateTime(), 
					getSessionUser().getTimeZone(), getSessionUser().getLocale(), DateTimeService.Dow_Month_Day_year);
			return content.getTitle() + "-" + str;
		}
		return null;
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}

	@Override
	protected ConsoleSidePanel getRightPanel() {
		return new AcuseReportParametersPanel("side", getKey()) {
			@Override
			protected void onChange(AjaxRequestTarget target, Map<String, Object> parameters) {
				for (String key : parameters.keySet()) {
					getQuery().getParameters().put(key, parameters.get(key));
				}
				refresh(target);
			}
			@Override
			protected void onSubmit(AjaxRequestTarget target, Map<String, Object> parameters) {
				for (String key : parameters.keySet()) {
					getQuery().getParameters().put(key, parameters.get(key));
				}
				refresh(target);
			}
			@Override
			public void onClose(AjaxRequestTarget target) {
				getBrowser().togglePanel(AcuseReportParametersPanel.class);
				AcuseReportConsole.this.refresh(target);
				fire(new SidePanelEvent(target));
			}
		};
	}
	
	@Override
	public Query newQuery() {
		return new AcuseQuery();
	}
	
	@Override
	public Domain getDomain() {
		return getContentDao().findDomainByName("aerolineas");
	}

	protected Content getContent(String id) {
		return getContentDao().findContentById(Long.valueOf(id));
	}
	
	protected Attribute getAttribute(String name) {
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (attribute.getName() != null && attribute.getName().equals(name)) {
				return attribute;
			}
		}
		return null;
	}

}

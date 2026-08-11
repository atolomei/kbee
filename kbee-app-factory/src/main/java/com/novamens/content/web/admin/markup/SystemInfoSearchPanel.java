package com.novamens.content.web.admin.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.Model;

import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.java.LogIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.wicket.util.BCElement;

import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import kbee.util.Tuple;

public class SystemInfoSearchPanel extends AbstractSystemInfoPanel {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemInfoSearchPanel.class.getName());

	
	public SystemInfoSearchPanel(String id) {
		super(id);
	}
	
	
	public SystemInfoSearchPanel() {
		super("info-panel");
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		// add(getMenuBreadCrumbPanel());
		
		AreaInfoPanel area = new AreaInfoPanel("info");
		add(area);
		
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");

		// KBFS 1													
		area.addPanel(new GridInfoPanel("element",  searchInfo(), new Model<String>("Search Platform"), true));

	}

	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Search Platform"));
	}

	
	/**
	 * @return
	 */
	private List<Tuple> searchInfo() {
		
		long start = System.currentTimeMillis();
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
			
			try {
			
				data.add(new Tuple( "solr.url", 			
						"<a href=\""+ PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim()+"\" target=\"_blank\">" + PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.url", "").trim() +"</a>" 
						));
				
				data.add(new Tuple( "solr.content-core", 	PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.content-core", "").trim()	));
				
				//data.add(new Tuple( "solr.audit-core", 		
				//		PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.audit-core", "")!=null ?
				//		PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.audit-core", "").trim() : ""));

				data.add(new Tuple( "solr.file-core", 	    
						PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.file-core", "")!=null ?
						PropertiesFactory.getInstance("kbee").getProperties().getProperty("solr.file-core", "").trim() :""));

			} catch (Exception e) {
				data.add(new Tuple( "solr variables ",  e.getClass().getName()+" | " + e.getMessage()));
				logger.error(e);		
		
			}
			
				
			SystemMetricsService service = ServiceLocator.getService(SystemMetricsService.class);
		
			String rate_ig;
			String rate_im;
			String rate_ia;

			String imm;
			String iam;
			
			String igm;

			try {
				long s_start = System.currentTimeMillis();
				
				SolrQuery q = new SolrQuery(getQueryIndex()) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getStatement() {
						return "*:*";
					}
					@Override
					public String getSolrStatement() {
						return "*:*";
					}
				};
				int qsize = q.execute().size();
				long s_end = System.currentTimeMillis();
				data.add(new Tuple( "Total Java Index", NumberFormatter.formatNumber(qsize, getSessionUser().getLocale()).trim() +"  <span class=\"ago\"> (" +  String.valueOf(s_end-s_start) + " ms) </span>"));
				
			} catch (Exception e) {
				data.add(new Tuple( "Total Java Index",  e.getClass().getName()+" | " + e.getMessage()));
				logger.error(e);		
			}
			
		 
			
			
			
			try {
				long s_start = System.currentTimeMillis();

				SolrQuery q = new SolrQuery(getFileIndex()) {
					private static final long serialVersionUID = 1L;
					@Override
					public String getStatement() {
						return "*:*";
					}
					@Override
					public String getSolrStatement() {
						return "*:*";
					}
				};
				int qsize = q.execute().size();
				long s_end = System.currentTimeMillis();
				data.add(new Tuple( "Total File Index",  NumberFormatter.formatNumber(qsize, getSessionUser().getLocale()).trim() +"<span class=\"ago\"> (" +  String.valueOf(s_end-s_start) + " ms) </span>"));
				
			} catch (Exception e) {
				data.add(new Tuple( "Total File Index   ",  e.getClass().getName()+" | " + e.getMessage()));
				logger.error(e);		
		
			}

			
			
			
			
			
			
			try {	
				String ig1 	= NumberFormatter.formatNumber(service.getMeterIndexTasks().getOneMinuteRate(), getSessionUser().getLocale()).trim();
				String ig5 	= NumberFormatter.formatNumber(service.getMeterIndexTasks().getFiveMinuteRate(), getSessionUser().getLocale()).trim();
				String ig15 = NumberFormatter.formatNumber(service.getMeterIndexTasks().getFifteenMinuteRate(), getSessionUser().getLocale()).trim();

				igm  = NumberFormatter.formatNumber(service.getMeterIndexTasks().getMeanRate(), getSessionUser().getLocale()).trim() + " <span class=\"ago\">task/sec</span>";
						
				rate_ig = ig1 + "<span class=\"separator\">|</span>" +
						  ig5 + "<span class=\"separator\">|</span>" +
						  ig15;
				
			} catch (Exception e) {
				rate_ig=e.getClass().getName() + " | " + e.getMessage();
				igm=e.getClass().getName();
				logger.error(e);		

			}
		
			try {
													
				String im1 	= NumberFormatter.formatNumber(service.getMeterIndexMetainfoTasks().getOneMinuteRate()).trim();
				String im5 	= NumberFormatter.formatNumber(service.getMeterIndexMetainfoTasks().getFiveMinuteRate()).trim();
				String im15 = NumberFormatter.formatNumber(service.getMeterIndexMetainfoTasks().getFifteenMinuteRate()).trim();
				
				imm = String.format("%6.2f", service.getMeterIndexMetainfoTasks().getMeanRate()).trim() + " <span class=\"ago\">task/sec</span>";;
						
				rate_im = im1 + "<span class=\"separator\">|</span>" +
						  im5 + "<span class=\"separator\">|</span>" +
						  im15;
				
				String ia1 	= NumberFormatter.formatNumber( service.getMeterIndexAttachmentsTasks().getOneMinuteRate()).trim();
				String ia5 	= NumberFormatter.formatNumber( service.getMeterIndexAttachmentsTasks().getFiveMinuteRate()).trim();
				String ia15 = NumberFormatter.formatNumber( service.getMeterIndexAttachmentsTasks().getFifteenMinuteRate()).trim();
				
				iam = NumberFormatter.formatNumber(service.getMeterIndexAttachmentsTasks().getMeanRate()).trim() + " <span class=\"ago\">task/sec</span>";
							
				rate_ia = ia1 + "<span class=\"separator\">|</span>" +
						  ia5 + "<span class=\"separator\">|</span>" +
						  ia15;
				
			} catch (Exception e) {
				
				rate_im=e.getClass().getName();
				rate_ia=e.getClass().getName();
				imm=e.getClass().getName();
				iam=e.getClass().getName();
				logger.error(e);
			}
			
			data.add(new Tuple( "Index Metainfo 	task/sec (1m 5m 15m) ",  rate_im));
			data.add(new Tuple( "Index Attachments  task/sec (1m 5m 15m) ",  rate_ia));
			data.add(new Tuple( "Index Total		task/sec (1m 5m 15m) ",  rate_ig));
			
			data.add(new Tuple( "Index Metainfo 	mean rate ",  imm));
			data.add(new Tuple( "Index Attachments  mean rate ",  iam));
			data.add(new Tuple( "Index Total		mean rate",  igm));
										
			data.add(new Tuple( "Index Metainfo Total ",  	NumberFormatter.formatNumber(service.getMeterIndexMetainfoTasks().getCount()).trim()));
			data.add(new Tuple( "Index Attachments Total ", NumberFormatter.formatNumber(service.getMeterIndexAttachmentsTasks().getCount()).trim()));
			
			
			
		} catch (Exception e) {
			data.add(new Tuple( "Index. ",  e.getClass().getName()+" | " + e.getMessage()));
			logger.error(e);		
		}
		finally {
			
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+" ms");
		}
		return data;
	}
	
	
	protected Index getFileIndex() {
		return getDomain().getService(FileIndexerService.class).getIndex();
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	protected Index getAuditIndex() {
		return getDomain().getService(LogIndexerService.class).getIndex();
	}

	
}

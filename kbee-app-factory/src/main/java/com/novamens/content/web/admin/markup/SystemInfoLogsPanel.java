package com.novamens.content.web.admin.markup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.wicket.util.BCElement;

import kbee.util.Tuple;
import kbee.web.error.LogsInfoPanel;

public class SystemInfoLogsPanel extends AbstractSystemInfoPanel {
			
	static private Logger logger = LogManager.getLogger(SystemInfoLogsPanel.class.getName());
	
	private static final long serialVersionUID = 1L;

	public SystemInfoLogsPanel() {
		this("info-panel");
	}
	
	public SystemInfoLogsPanel(String id) {
		super(id);
	}
	
	/**
	 * 
	 */
	public void onInitialize() {
		super.onInitialize();
		

		
		
		AreaInfoPanel area = new AreaInfoPanel("info");
		add(area);
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");
		
		
		area.addPanel(new LogsInfoPanel("element"));
		
		List<String> ti = new ArrayList<String>();
		ti.add("Level");
		ti.add("Parent");
		area.addPanel(new GridInfoPanel("element",  getLoggersInfo(), 		new Model<String>("Loggers"), ti, true));
	}
	
	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Logs"));
	}

	
	protected List<Tuple> getLoggersInfo() { 
		
		List<Tuple> data = new ArrayList<Tuple>();
		long start = System.currentTimeMillis();
		try  {
			LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
			Map<String, LoggerConfig> map = logContext.getConfiguration().getLoggers();
			for(Entry<String, LoggerConfig> entry: map.entrySet()) {
				
				String le="";
				String pa="";
				
				if (entry.getValue().getLevel()!=null)
					le=entry.getValue().getLevel().name();
				
				if (entry.getValue().getParent()!=null) 
					pa=entry.getValue().getParent().getName();
				
				String [] arr = {le, pa}; 
				data.add(new Tuple(entry.getValue().getName(), arr));
			}
			
			data.sort(new Comparator<Tuple>() {
				@Override
				public int compare(Tuple o1, Tuple o2) {
					try {
					return o1.label.compareToIgnoreCase(o2.label);
					} catch (Exception e) {
						return 0;
					}
				}
			});
			
		}		
		catch (Exception e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName() + " | " + e.getMessage()));
			logger.error(" {} | {} | {} | {} ", (getSessionUser()!=null?getSessionUser().getUserName():""), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
		} finally {
			long end = System.currentTimeMillis();
			
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+"ms");
		}
		
		
		return data;
	}

}

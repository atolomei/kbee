package com.novamens.kbee.wicket.markup.html.console.browser;

import java.math.RoundingMode;
import java.text.NumberFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;


@SuppressWarnings("serial")
public class NavigationLabel extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(NavigationLabel.class.getName());

	
	final NumberFormat nf;
	
	public NavigationLabel(BaseBrowser<?> browser, Align align) {
		super(browser, align);
		nf = NumberFormat.getInstance(getSessionUser().getLocale());
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(0);
		nf.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		//if (get("total")==null)
			addLabels();
	}
	
	protected void addLabels() {
		add(new Label("from", new Model<String>() {
			public String getObject() {
				DataViewPanel<?> dataview = getBrowser().getPanel(DataViewPanel.class);
				long currentPage = dataview.getCurrentPage();
				int pageSize = dataview.getPageSize();
				long from = currentPage*pageSize+1;
				return nf.format(from);
			}
		}));
		add(new Label("to", new Model<String>() {
			public String getObject() {
				DataViewPanel<?> dataview = getBrowser().getPanel(DataViewPanel.class);
				long currentPage = dataview.getCurrentPage();
				int pageSize = dataview.getPageSize();
				long from = currentPage*pageSize+1;
				long total = dataview.getSearcher().size();
				long to = from+pageSize-1;
				if (to>total) to = total;
				return nf.format(to);
			}
		}));
		add(new Label("total", new Model<String>() {
			public String getObject() {
				DataViewPanel<?> dataview = getBrowser().getPanel(DataViewPanel.class);
				long total = dataview.getSearcher().size();
				return nf.format(total);
			}
		}));
	}
	
	
	protected User getSessionUser() {
		try {
			return  ServiceLocator.getService(SecurityService.class).getSessionUser();

		} catch (Exception e) {
			logger.error(" {} | {} | {} | {}", "getSessionUser() gave the error", e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
			return null;
		}
	}
	
	
	
}

package kbee.web.console.tools;

import com.novamens.content.query.SavedQuery;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.GridPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.DownloadMenuItemPanel;
import com.novamens.logging.DownloadEvent;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.console.AbstractConsole;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GridExportMenuItem<T> extends DownloadMenuItemPanel<T> {

	private static final long serialVersionUID = 1L;
	private final static int _MAX_ITEMS_TO_EXPORT = 150000;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GridExportMenuItem.class.getName());
	
	static private Logger DBLogger = LogManager.getLogger("DBEventLogger");

	private static AtomicInteger max_items = null;
	
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	IModel<SavedQuery> model;
	
	
	public GridExportMenuItem(String id, String label, IModel<SavedQuery> model) {
		super(id, label);
		this.model=model;
	}
	
	
	public GridExportMenuItem(String id, String label) {
		super(id, label);
	}

	@Override
	protected void onGridExport(AjaxRequestTarget target) {
		int size = getConsole().getQuery().execute().size();
		if (size > getMaxRowsToExport()) {
			logger.debug("too many items: " + String.valueOf(size) + " MAX: " + String.valueOf(getMaxRowsToExport()));
			getInfoDialog().open(target, () -> "Information", getLabel("gridexport.tooManyItems", String.valueOf(size), String.valueOf(getMaxRowsToExport())));
		} else
			export(target);
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}

	
	private void export(AjaxRequestTarget target) {
		try {

			String fileName = getConsole().getDownloadFileName() + (model!=null? ("-"+model.getObject().getName().trim().toLowerCase().replace(" ","")):"") + getGridExport().getFileExtension();
			
			List<GridColumn<SearchResult, String>> columns = ((GridPanel<?>) getConsole().getBrowser().getPanel(GridPanel.class)).getVisibleColumns();
			
			Query query;
			
			if (model!=null) {
				query=getConsole().newQuery();
				query.setParameters(model.getObject().getParameters());
			}
			else
				query=getConsole().getQuery();
			
			
			File file = getGridExport().export(query, columns, fileName, getConsole().getGridExportTitle());
			
			downloadFile(target, file);
			DBLogger.info(new DownloadEvent((String) getConsole().getDisplayName().getObject(), fileName));
		
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public abstract AbstractConsole<?> getConsole();
	public abstract GridExport getGridExport();
	public abstract InfoDialog getInfoDialog();

	protected int getMaxRowsToExport() {
		if (max_items == null)
			max_items = new AtomicInteger(ServiceLocator.getService(SystemParameterService.class).getIntegerParameter("grid.export.max", _MAX_ITEMS_TO_EXPORT));
		return max_items.get();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	
	public void onDetach() {
		super.onDetach();
		
		if (model!=null)
			model.detach();
	}

}

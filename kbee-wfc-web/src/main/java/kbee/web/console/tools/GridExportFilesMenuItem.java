package kbee.web.console.tools;



//import com.novamens.content.web.admin.markup2.datamanagement.ExportContentsPage;
//import com.novamens.content.web.base.markup.ContentBaseBC;
//import com.novamens.content.web.console.markup.ContentBasePage;


import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.console.AbstractConsole;
import kbee.web.nav.ContentBaseBC;
import kbee.web.page.ExportContentsPage;
import kbee.web.service.ApplicationSiteMapService;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GridExportFilesMenuItem extends AjaxMenuItemPanelV5<Void> {
	private static final long serialVersionUID = 1L;
	private final static int _MAX_ITEMS_TO_EXPORT = 50000;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(GridExportFilesMenuItem.class.getName());
	private static AtomicInteger max_items = null;
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	private String label;

	public GridExportFilesMenuItem(String id, String label) {
		super(id);
		this.label = label;
	}

	@Override
	public void onClick(AjaxRequestTarget target) throws Exception {
		onGridExport(target);
	}

	protected void onGridExport(AjaxRequestTarget target) {
		int size = getConsole().getQuery().execute().size();
		if (size > getMaxRowsToExport()) {
			logger.debug("too many items: " + String.valueOf(size) + " MAX: " + String.valueOf(getMaxRowsToExport()));
			getInfoDialog().open(target, () -> getString("information"), getLabel("gridexport.tooManyItems", String.valueOf(size), String.valueOf(getMaxRowsToExport())));
		} else
			export(target);
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}


	private void export(AjaxRequestTarget target) {
		

		setResponsePage(new ExportContentsPage(getConsole().getQuery(), new ContentBaseBC()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClose() {
				setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("library-contentbase-page"));
			}
		});
		getConsole().refresh(target);
	}

	@SuppressWarnings("unused")
	private String resolveExportName() {
		String fileName = (String) getConsole().getDisplayName().getObject();
		fileName = fileName.replaceAll("[ |\\t|\\s|(|)]", "-").toLowerCase();
		Map<String, Object> parameters = getConsole().getQuery().getParameters();
		for (String paramKey : parameters.keySet()) {
			fileName += "_" + paramKey + " " + parameters.get(paramKey);
		}
		return fileName;
	}

	public abstract AbstractConsole<?> getConsole();

	public abstract InfoDialog getInfoDialog();

	protected int getMaxRowsToExport() {
		if (max_items == null)
			max_items = new AtomicInteger(ServiceLocator.getService(SystemParameterService.class).getIntegerParameter("grid.export.max", _MAX_ITEMS_TO_EXPORT));
		return max_items.get();
	}

	@Override
	public boolean isEnabled() {
		if (is_root || is_domain_admin)
			return true;

		if (is_support)
			return false;

		return false;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}

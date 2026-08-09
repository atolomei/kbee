package com.novamens.kbee.wicket.markup.html.console.panel;


import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.File;

public abstract class DownloadMenuItemPanel<T> extends AjaxMenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DownloadMenuItemPanel.class.getName());

	private String label;
	private AJAXDownload download;

	public DownloadMenuItemPanel(String id,String label) {
		super(id, "toright");
		this.label = label;

		download = new AJAXDownload();
		this.add(download);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
	}

	@Override
	public String getBeforeClick() {

		return super.getBeforeClick();
	}


	public void onClick(AjaxRequestTarget target) throws Exception {
		onGridExport(target);
	}

	protected void downloadFile(AjaxRequestTarget target, File file) {
		download.setFile(file);
		download.initiate(target);
	}

	protected abstract void onGridExport(AjaxRequestTarget target);

	@Override
	public String getLabel() {
		return label;
	}


	@Override
	public String getWorkingLabel() {
		return this.getLabel();

	}


}

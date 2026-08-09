package kbee.web.content.eform;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EResourceField;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.signature.SignatureException;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;

import kbee.util.logging.Logger;
import kbee.web.eform.EPdfFile;

@SuppressWarnings("serial")
public class ContentFormViewerToolbar extends ModelPanel<EFormData> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(ContentFormViewerToolbar.class.getName());
	
	public ContentFormViewerToolbar(String id, IModel<EFormData> model) {
		super(id, model);
	}
	
	public EFormData getFormData() {
		return getModelObject();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		WebMarkupContainer bar = new WebMarkupContainer("bar");
		
		DonwloadMenuItemPanelV5<EFormData> downloadPdf = new  DonwloadMenuItemPanelV5<EFormData>("downloadPdf") {
			@Override 
			public String getLabel() {
				return "";
			}
			@Override
			public boolean isDeleteFileAfterDownload()  {
				return true;
			}
			public String getFileName() {
				try {
					if (getFormData().getForm().isFileContainer()) {
						File file = getIncludedFile(getFormData());
						return file!=null?file.getName():null;
					}
					else {
						return getPdf().getFileName();
					}	
				}
				catch (IOException e) {
					logger.error(e);
					return null;
				}
			}
			@Override
			protected File getFile() {
				try {
					if (getFormData().getForm().isFileContainer()) {
						return getIncludedFile(getFormData());
					}
					else {
						return getPdf().getFile();
					}	
				}
				catch (IOException|SignatureException e) {
					logger.error(e);
					return null;
				}
			}
			protected EPdfFile getPdf() {
				return new EPdfFile(getFormData());
			}
		};
		
		downloadPdf.setIconCssClass("far fa-download");
		bar.add(downloadPdf);
		
		add(bar);
	}	
	
	protected File getIncludedFile (EFormData data) throws IOException {
		for (EFormField<?> field : data.getForm().getFields()) {
			if (field instanceof EResourceField) {
				Object resourceobject = data.getData(field);
				if (resourceobject instanceof KBFile) {
					File file = ((KBFile)resourceobject).getFile();
					return file;
				}
			}
		}
		return null;
	}
}
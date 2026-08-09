package kbee.web.searcher.panel;

import org.apache.wicket.model.IModel;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.kbee.content.form.KbeeMemberForm;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.util.logging.Logger;
import kbee.web.eform.EFormDataModel;
import kbee.web.eform.EFormViewer;

public class SearcherMemberViewPanel<T extends DataSetMember> extends KBPanel {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SearcherContentViewPanel.class.getName());
	
	private IModel<T> model;

	public SearcherMemberViewPanel(String id, IModel<T> model) {
		super(id, model);
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		try {
			add(new EFormViewer("eform", getFormData()));
		} 
		catch (Exception e) {
			logger.error(e);
			add(new InvisiblePanel("eform"));
		}
	}
	
	public T getMember() {
		return model.getObject();
	}
	
	private IModel<EFormData> getFormData() {
		EForm form = getForm();
		EFormData data = new KbeeEMemMemberData(form, getMember());
		for (EFormField<?> field : form.getFields()) {
			field.get(getMember(), data);
		}	
		IModel<EFormData> model = new EFormDataModel(data);
		return model;
	}
	
	private EForm getForm() {
		return new KbeeMemberForm(getMember());
	}
}
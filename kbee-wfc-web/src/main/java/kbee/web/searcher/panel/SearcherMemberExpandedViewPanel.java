package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.kbee.content.form.KbeeMemberForm;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;

import kbee.util.logging.Logger;
import kbee.web.eform.EFormDataModel;

@SuppressWarnings("serial")
public class SearcherMemberExpandedViewPanel<T extends DataSetMember> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(SearcherMemberExpandedViewPanel.class.getName());

	private IModel<T> model;
	private IModel<Site> siteModel;
	
	public SearcherMemberExpandedViewPanel(String id, IModel<T> model) {
		super(id, model);
		this.model = model;
		//this.siteModel=siteModel;
	}

	public 	IModel<Site> getSiteModel() {
		return this.siteModel;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (model != null)
			model.detach();

		if (siteModel!=null)
			siteModel.detach();
	}

	public IModel<T> getModel() {
		return model;
	}
	
	
	public T getMember() {
		return model.getObject();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}	
		
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("tabs")!=null)
			return;
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		boolean has_eforms = false;
		
				tabs.add(new AbstractTabKB(new Model<String>("Carpeta"), "carpeta") {
					
					@Override
					public Panel getPanel(String panelId) {
						return new 	SearcherMemberFormViewer(panelId, getFormData());
					}
				});
				
				tabs.add(new AbstractTabKB(new Model<String>("Notas"), "notes") {
					@Override
					public Panel getPanel(String panelId) {
						return new SearcherMemberNotesPanel<T>(panelId, getModel());
					}
				});
		
		AjaxTabbedPanel<ITab> tabbedpanel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
			protected String getNavCss() {
				return "nav nav-tabs";
			}
		};
		
		add(tabbedpanel);
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

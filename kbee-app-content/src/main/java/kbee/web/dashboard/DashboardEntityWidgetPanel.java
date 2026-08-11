package kbee.web.dashboard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.EntityMember;
import com.novamens.content.service.UrlService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.kbee.content.form.KbeeMemberForm;

import kbee.web.eform.EFormDataModel;
import kbee.web.eform.EFormViewer;

@SuppressWarnings("serial")
public class DashboardEntityWidgetPanel extends DashboardWidgetBasePanel {
	private static final long serialVersionUID = 1L;

	IModel<EntityMember> entitymodel;
	
	public DashboardEntityWidgetPanel(String id, IModel<EntityMember> entitymodel) {
		super(id, "roles");
		this.entitymodel = entitymodel;
		setTitle(new Model<String>(getEntity().getDataSet().getDisplayName()));
	}
	
	public EntityMember getEntity() {
		return entitymodel.getObject();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new EFormViewer("eform", getFormData()));
		
		add(new Link<Void>("entity-link") {
			public void onClick() {
				setResponsePage(new RedirectPage(getEntity().getService(UrlService.class).getUrl()));
			}
		});
	}
	
 
	/** TODO */
	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		//main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}
	@Override
	protected void onHelp(AjaxRequestTarget target) {
		refresh(target);
	}
	
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private IModel<EFormData> getFormData() {
		EForm form = getForm();
		EFormData data = new KbeeEMemMemberData(form, getEntity());
		for (EFormField<?> field : form.getFields()) {
			field.get(getEntity(), data);
		}	
		IModel<EFormData> model = new EFormDataModel(data);
		return model;
	}
	
	private EForm getForm() {
		return new KbeeMemberForm(getEntity());
	}

	@Override
	protected void onTitleClick() {
		 
	}
	
}
package kbee.web.portal6.sitemanager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.DOMObjectService;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.editor.MemberClassificationEditor;
import kbee.web.form.EditButtonsV5;

public class SimpleSiteTagsEditor extends DomainObjectEditor<DataSetMember> {
																									
	/**
	 * 
	 */
private static final long serialVersionUID = 1L;
static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SimpleSiteTagsEditor.class.getName());
	
		
	public SimpleSiteTagsEditor(String id, IModel<Site> site_model, IModel<DataSetMember> model) {
		super(id, model);
		this.site_model=site_model;
		setOutputMarkupId(true);
	}
	
	IModel<Site> site_model;
	
	public IModel<Site> getSiteModel() {
		return this.site_model;
	}
	
	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
		target.add( SimpleSiteTagsEditor.this.getParent());
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add( SimpleSiteTagsEditor.this.getParent());
	}

	
	public void onDetach() {
		super.onDetach();
		if (this.site_model!=null)
			this.site_model.detach();
	}
	
	@Override
	public void onInitialize() {
		
		super.onInitialize();
		
		add (new SiteBCPanel("bc.site-tags", getSiteModel()));
		
		
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		if (getSiteDataSetMemberModel()!=null) {
			form.add(new MemberClassificationEditor(false) {
				private static final long serialVersionUID = 1L;
				public IModel<DataSetMember> getModel2() {
					return getSiteDataSetMemberModel();
				}	
				public DataSetMember getModelObject2() {
					return getSiteDataSetMemberModel().getObject();
				}
			});
		}
		else {
			form.add(new InvisiblePanel("classification"));
		}
		add(form);

		add(new EditButtonsV5<DataSetMember>(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !isReadOnly();
			}
			
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-link";
			}
		});

		
		
	}
		
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
					getSiteDataSetMemberModel().getObject().getService(DOMObjectService.class).update(getUpdatedParts());
					target.add(SimpleSiteTagsEditor.this);
			}
		}
		catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	
	protected IModel<DataSetMember> getSiteDataSetMemberModel() {
		return getModel();
		
		/**
		DataSet dataset = getExternalDao().getSiteDataSet(getDomain());
		DataSetMember  member = getExternalDao().findMemberByExternalId(getModel().getObject().getOId(), dataset);
		if (member!=null)
			return new ObjectModel<DataSetMember>(member);
		return null;
		**/
	}
	

}

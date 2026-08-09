package kbee.web.searcher.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExternalDao;
import com.novamens.content.model.LabelMember;
import com.novamens.content.service.DOMObjectService;
//import com.novamens.content.web.dataset.markup.MemberClassificationEditor;
//import com.novamens.content.web.dataset.markup.MemberEditor;
import com.novamens.kbee.content.model.KbeeLabelMember;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.editor.MemberClassificationEditor;
import kbee.web.form.EditButtonsV5;

/**
 *  

 *
 */
public class SearcherSiteAttributesEditor extends DomainObjectEditor<DataSetMember> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherSiteAttributesEditor.class.getName());

	
	public SearcherSiteAttributesEditor(String id, IModel<DataSetMember> model) {
		super(id, model);

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
		});
		
	}
	

	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
					getSiteDataSetMemberModel().getObject().getService(DOMObjectService.class).update(getUpdatedParts());
					target.add(SearcherSiteAttributesEditor.this);
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

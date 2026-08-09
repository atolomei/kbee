package kbee.web.searcher.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;
import kbee.web.portal6.DomainSearcherPortalService;

public class SearcherSiteFiltersEditor extends DomainObjectEditor<Site> {

	private static final long serialVersionUID = 1L;
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger( SearcherSiteFiltersEditor.class.getName());
	
	public SearcherSiteFiltersEditor(String id, IModel<Site> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		
		Json json;
		String site_iql;
		
		try {
			json = getModel().getObject().getCustomValuesJson();
			site_iql = (String) json.get("iql");
		}
		catch (Exception e) {
			logger.error(e);
			json = new KbeeJson();
			site_iql = null;
		}
		
		//setIql(site_iql !=null ? site_iql : getDomain().getService(DomainSearcherPortalService.class).getDefaultSearcherPortalIql(getModel().getObject()));
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new TextAreaField<String>("iql", new Model<String>() {
			private static final long serialVersionUID = 1L;
			@Override
			public String getObject() {
				return "";
				//return getIql();
			}
			
			@Override
			public void setObject(String o) {
				//setIql(o);
			}
		}, 8, 80));
		
		add(form);

		add(new EditButtonsV5<Site>(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !isReadOnly();
			}
		});
	}



	
	public void onEdit(IModel<Site> model) {
	}

	public void onUpdate(AjaxRequestTarget target) {
	}

	public void onCancel(AjaxRequestTarget target) {
	}

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				
				
				
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	protected Domain getDomain() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}


	
}

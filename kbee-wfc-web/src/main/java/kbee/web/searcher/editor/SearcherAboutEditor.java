package kbee.web.searcher.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;

public class SearcherAboutEditor extends DomainObjectEditor<Site> {
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherAboutEditor.class.getName());
	
	private IModel<String> aboutTitle;
	private IModel<String> aboutAbstract;
	private IModel<String> aboutText;


	public SearcherAboutEditor(String id, IModel<Site> model) {
		super(id, model);
	}
	
	public IModel<String> getAboutTitle() {
		return aboutTitle;
	}

	public void setAboutTitle(IModel<String> aboutTitle) {
		this.aboutTitle = aboutTitle;
	}

	public IModel<String> getAboutAbstract() {
		return aboutAbstract;
	}

	public void setAboutAbstract(IModel<String> aboutAbstract) {
		this.aboutAbstract = aboutAbstract;
	}

	public IModel<String> getAboutText() {
		return aboutText;
	}

	public void setAboutText(IModel<String> aboutText) {
		this.aboutText = aboutText;
	}

	/**
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
		
		String s = getModel().getObject().getCustomValuesJson().getString("about-text");
		if (s!=null)
			s=s.replace("\\'", "\"");
		
		setAboutTitle( new Model<String>( (String) getModel().getObject().getCustomValuesJson().get("about-title")));
		setAboutAbstract( new Model<String>(getModel().getObject().getCustomValuesJson().getString("about-abstract")));
		setAboutText( new Model<String>(s));
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new TextField<String>("title", getAboutTitle()));
		form.add(new TextField<String>("abstract", getAboutAbstract()));
		form.add(new TextAreaField<String>("text", getAboutText(), 8, 80));
		
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
				Json js = getModel().getObject().getCustomValuesJson();
				
				js.put("about-title", getAboutTitle().getObject());
				js.put("about-abstract", getAboutAbstract().getObject());
				js.put("about-text", getAboutText().getObject());
				
				((KbeeSite) getModel().getObject()).setCustomValuesJson(js);
				getModel().getObject().getService(SiteService.class).update(getUpdatedParts());
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

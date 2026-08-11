package kbee.web.portal6;

import java.util.HashMap;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.portal.service.SiteFactoryService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.portal6.model.SiteTemplate;
import com.novamens.portal6.model.SiteType;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;

import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.form.EditButtonsV5;
import kbee.web.nav.HomeBC;
import kbee.web.nav.SitesBC;

import com.novamens.wicket.markup.html.form.TextAreaField;

/**
 * Editor de Sitio Externo
 * 
 */
public class NewSiteEditor extends ObjectEditor<NewSiteData> {
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(NewSiteEditor.class.getName());

	private static final long serialVersionUID = 1L;

	public class UrlValidator implements IValidator<String> {
		
		private static final long serialVersionUID = 1L;
		Map<String, String> reservedURL = new HashMap<String, String>();

		@Override
		public void validate(final IValidatable<String> validatable) {
			String url = validatable.getValue();
			if (url == null || url.length() == 0) {
				validatable.error(new ValidationError(this, "characters"));
				return;
			}
			if (!url.matches("[a-z|0-9]+")) {
				validatable.error(new ValidationError(this, "characters"));
				return;
			}
			if (reservedURL.containsKey(url)) {
				validatable.error(new ValidationError(this, "reserved-url"));
				return;
			}
		}
	}

	/**
	 * @param id
	 * @param model
	 */
	public NewSiteEditor(String id, IModel<NewSiteData> model) {
		super(id, model);

		setOutputMarkupId(true);
		setEditionEnabled(true);
		
 		MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
		 
 		bc.addElement( new HomeBC());
 		
 		bc.addElement(new SitesBC());
		 bc.addElement(new BCElement(new Model<String>(getModel().getObject().getTitle())));
		 add(bc);

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		form.add(new TextField<String>("title", new Model<String>(getModel().getObject().getTitle()), true));

		TextField<String> tfurl = new TextField<String>("url", new Model<String>(getModel().getObject().getUrl()), true) {
			private static final long serialVersionUID = 1L;
			@Override
			protected IModel<String> getHelpText() {
				return new StringResourceModel("url-external", NewSiteEditor.this, null);
			}
		};
		form.add(tfurl);

		/**
		IModel<SiteType> typemodel = new IModel<SiteType>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void detach() {
			}

			@Override
			public SiteType getObject() {
				return getModelObject().getType();
			}

			@Override
			public void setObject(SiteType object) {
				getModelObject().setType(object);
			}
		};
		
		
		com.novamens.wicket.model.ListModel<SiteType> lm = new com.novamens.wicket.model.ListModel<SiteType>(
				new Model<ObjectEditor<NewSiteData>>(this), "types");
		 */
		/**
		ChoiceField<SiteType> sitetype = new ChoiceField<SiteType>("sitetype", typemodel, lm, true) {

			private static final long serialVersionUID = 1L;

			@Override
			public String getIdValue(SiteType value) {
				return String.valueOf(value.getId());
			}

			@Override
			public String getDisplayValue(SiteType value) {
				return (value.getLabel(getLocale()));
			}
		};*/

		form.add(new TextField<String>("subtitle", new Model<String>(getModel().getObject().getSubtitle())));
		form.add(new TextAreaField<String>("description", new Model<String>(getModel().getObject().getDescription())));

		//form.add(sitetype);
		add(form);

		add(new EditButtonsV5<NewSiteData>(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
		});

		WebMarkupContainer feedbackcontainer = new WebMarkupContainer("feedback-container") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !isEditionEnabled();
			}
		};

		feedbackcontainer.add(new Link<Void>("create-link") {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new NewSitePage(new Model<NewSiteData>(new NewSiteData())));
			}
		});

		feedbackcontainer.add(new Link<Void>("open-link") {
			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new RedirectPage(NewSiteEditor.this.getModel().getObject().getUrl()));
			}
		});

		add(feedbackcontainer);
	}

	/**
	public List<SiteType> getTypes() {
		
		List<SiteType> list = new ArrayList<SiteType>();
		
		//list.add(SiteType.APPLICATION);
		//list.add(SiteType.BLOG);
		//list.add(SiteType.ECOMMERCE);
		//list.add(SiteType.EVENT);
		//list.add(SiteType.GOV);
		//list.add(SiteType.MUSIC);
		
		list.add(SiteType.INTEREST_GROUP);
		list.add(SiteType.ORGANIZATIONAL_AREA);
		list.add(SiteType.SUBJECT);
		list.add(SiteType.SEARCH);
		list.add(SiteType.DIRECTORY);
		
		//list.add(SiteType.SOCIAL_NETWORK);
		//list.add(SiteType.VIDEO);
		
		list.add(SiteType.WEB_PORTAL);

		Collections.sort(list, new Comparator<SiteType>() {
			@Override
			public int compare(SiteType a, SiteType b) {
				try {
					return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});

		return list;
	}*/

	@Override
	public void cancel(AjaxRequestTarget target) {
		onCancel(target);
	}

	public void onEdit(IModel<Site> model) {
	}

	public void onUpdate(AjaxRequestTarget target) {
	}

	public void onCancel(AjaxRequestTarget target) {
	}

	@SuppressWarnings("unchecked")
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {

				SiteFactoryService service = ServiceLocator.getService(SiteFactoryService.class);

				getModel().getObject().setTitle(((TextField<String>) get("form:title")).getValue());
				getModel().getObject().setUrl(((TextField<String>) get("form:url")).getValue());
				getModel().getObject().setDescription(((TextAreaField<String>) get("form:description")).getValue());
				getModel().getObject().setSubtitle(((TextField<String>) get("form:subtitle")).getValue());
				

				com.novamens.kbee.portal.model.KbeeSite site = (com.novamens.kbee.portal.model.KbeeSite) service.createExternalSite();

				site.setIsExternal(true);
				site.setPublicAccess(true);
				site.setDomain(getDomain());

				site.setUrl(((TextField<String>) get("form:url")).getValue());
				site.setTitle(((TextField<String>) get("form:title")).getValue());
				site.setSubtitle(((TextField<String>) get("form:subtitle")).getValue());
				site.setDescription(((TextAreaField<String>) get("form:description")).getValue());

				//SiteType st = ((ChoiceField<SiteType>) get("form:sitetype")).getValue();
				//site.setSiteType(st);
				
				site.setState(ObjectState.ENABLED);
				
				// TODO VER AT SITE
				if (getModelObject().isExternal())
					site.setSiteType(SiteType.GENERAL);
				else
					site.setSiteType(SiteType.GENERAL);
					

				//site.setDetailCommentsEnabled(false);
				//site.setDetailFollowEnabled(false);
				//site.setDetailInformEnabled(false);
				//site.setDetailRelatedEnabled(false);
				//site.setDetailSendEnabled(false);
				//site.setDetailVotesEnabled(false);

				site.getService(SiteService.class).update("Title, Url, Type, State, Description, Subtitle, Access");

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

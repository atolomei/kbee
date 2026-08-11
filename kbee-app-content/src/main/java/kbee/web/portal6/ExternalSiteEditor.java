package kbee.web.portal6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.portal6.model.SiteTemplate;
import com.novamens.portal6.model.SiteType;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.nav.SitesBC;

import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;

public class ExternalSiteEditor extends ObjectEditor<Site> {

	private static final long serialVersionUID = 1L;
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ExternalSiteEditor.class.getName());

	IModel<Site> model;

	public ExternalSiteEditor(String id, IModel<Site> model) {
		super(id);

		setModel(model);
		addComponents();
	}

	public void setModel(IModel<Site> model) {
		this.model = model;
	}

	public IModel<Site> getModel() {
		return model;
	}

	private void addComponents() {

		setOutputMarkupId(true);
		setEditionEnabled(true);
		
		MenuBreadCrumbPanel  bc =new MenuBreadCrumbPanel();
		//DropdownMenuBC dd = new DropdownMenuBC();
		bc.addElement(new SitesBC());
		bc.addElement(new BCElement(new Model<String>(getModel().getObject().getTitle())));
		add(bc);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		form.add(new TextField<String>("title", new Model<String>(getModel().getObject().getTitle()), true));

		TextField<String> tfurl = new TextField<String>("url", new Model<String>(getModel().getObject().getUrl()),
				true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected IModel<String> getHelpText() {
				return new StringResourceModel("url-external", ExternalSiteEditor.this, null);
			}
		};
		form.add(tfurl);

		IModel<SiteType> typemodel = new IModel<SiteType>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void detach() {
			}

			@Override
			public SiteType getObject() {
				return ExternalSiteEditor.this.getModel().getObject().getSiteType();
			}

			@Override
			public void setObject(SiteType object) {
				((com.novamens.kbee.portal.model.KbeeSite) ExternalSiteEditor.this.getModel().getObject()).setSiteType(object);
			}
		};

		com.novamens.wicket.model.ListModel<SiteType> lm = new com.novamens.wicket.model.ListModel<SiteType>(new Model<ObjectEditor<Site>>(this), "types");

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
			
			
			@Override
			public boolean isVisible() {
				return false;
			}
		};

		sitetype.setValue(getModel().getObject().getSiteType());

		form.add(new TextField<String>("subtitle", new Model<String>(getModel().getObject().getSubtitle())));
		form.add(new TextAreaField<String>("description", new Model<String>(getModel().getObject().getDescription())));

		form.add(sitetype);
		add(form);

		add(new EditButtonsV5<Site>(this) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
		});
	}

	/**
	 * -----------------------------------------------------------------------------------------------------
	 */
	public List<SiteType> getTypes() {
		List<SiteType> list = new ArrayList<SiteType>();
		list.add(SiteType.DEAL_ROOM);
		list.add(SiteType.GENERAL);
		list.add(SiteType.HOME);
		list.add(SiteType.KNOWLEDGE_BASE);
		list.add(SiteType.LIBRARY);

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

	}

	
	@Override
	public void cancel(AjaxRequestTarget target) {
		onCancel(target);
	}


	public void onEdit(IModel<Site> model) {}
	public void onUpdate(AjaxRequestTarget target) {}
	public void onCancel(AjaxRequestTarget target) {}

	
	@SuppressWarnings("unchecked")
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {

				com.novamens.kbee.portal.model.KbeeSite site = (com.novamens.kbee.portal.model.KbeeSite) getModel().getObject();
				
				site.setIsExternal(true);
				//site.setPublicAccess(true);
				site.setDomain(getDomain());

				site.setUrl(((TextField<String>) get("form:url")).getValue());
				site.setTitle(((TextField<String>) get("form:title")).getValue());
				site.setSubtitle(((TextField<String>) get("form:subtitle")).getValue());
				site.setDescription(((TextAreaField<String>) get("form:description")).getValue());
				
				// SiteType st = ((ChoiceField<SiteType>) get("form:sitetype")).getValue();
				// site.setSiteType(st);
				
				site.setState(ObjectState.ENABLED);
				

				//site.setDetailCommentsEnabled(false);
				//site.setDetailFollowEnabled(false);
				//site.setDetailInformEnabled(false);
				//site.setDetailRelatedEnabled(false);
				//site.setDetailSendEnabled(false);
				//site.setDetailVotesEnabled(false);

				site.getService(SiteService.class).update("Title, Url, Description, Subtitle");

			}
		} catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));
		}
	}

	protected Domain getDomain() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		} catch (Exception e) {
			return null;
		}
	}

}

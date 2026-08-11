package kbee.web.model.contentclass;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeRelationTemplate;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class RelationsEditor extends ModelPanel<ContentTemplate> {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = new Logger(LogManager.getLogger(RelationsEditor.class.getName()));
	 
	private class TemplateTab implements ITab, IDetachable {
		private IModel<RelationTemplate> model;
		public TemplateTab(IModel<RelationTemplate> model) {
			this.model = model;
		}
		public IModel<String> getTitle() {
			return new Model<String>(getModel().getObject().getName());
		}
		public Panel getPanel(String id) {
			RelationTemplateEditor editor = new RelationTemplateEditor(id, getModel()) {
				protected void onDelete(AjaxRequestTarget target) {
					addTabsPanel();
					target.add(RelationsEditor.this.get("tabs"));
				}
			};
			return editor;
		}
		public boolean isVisible() {
			return true;
		}
		public IModel<RelationTemplate> getModel() {
			return model;
		}
		public void detach() {
			model.detach();
		}
	}
	
	/**
	 * @param id
	 * @param model
	 */
	public RelationsEditor(String id, IModel<ContentTemplate> model) {
		super(id, model);
	
		add(new AjaxLink<Void>("new-button") {
			public void onClick(AjaxRequestTarget target) {
				RelationTemplate template = getNewTemplate();
				@SuppressWarnings("unchecked")
				AjaxTabbedPanel<ITab> tabs = (AjaxTabbedPanel<ITab>)RelationsEditor.this.get("tabs");
				
				tabs.getTabs().add(new TemplateTab(new ObjectModel<RelationTemplate>(template)));
				tabs.setSelectedTab(tabs.getTabs().size()-1);
				target.add(RelationsEditor.this);
			}
			public boolean isVisible() {
				if (isRoot())
					return true;
				
				if (RelationsEditor.this.getModel().getObject().isOnlyRootEdit())
					return false;

				if (!isFreeVersion())
					return true;
				
				return false;
		
			}
		});
		
		addTabsPanel();
		
		add(new ConfirmationDialog("confirmation-dialog"));
	}
	
	public void onDetach() {
		super.onDetach();
		for (ITab tab : ((AjaxTabbedPanel<?>)get("tabs")).getTabs()) {
			if (tab instanceof IDetachable) {
				((IDetachable)tab).detach();
			}
		}
	}
	
	protected boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}

	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(SecurityService.class).isRoot(getSessionUser());
	}
	
	protected boolean isAdmin() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private RelationTemplate getNewTemplate() {
		try {
			ContentTemplate contenttemplate = getModelObject();
			KbeeRelationTemplate relationtemplate = new KbeeRelationTemplate();
			relationtemplate.setState(ObjectState.ENABLED);
			relationtemplate.setDomain(contenttemplate.getDomain());
			relationtemplate.setLastModifiedUser(getSessionUser());
			relationtemplate.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			contenttemplate.getRelations().add(relationtemplate);
			contenttemplate.getService(DOMObjectService.class).update();
			return relationtemplate;
		}
		catch (ContentMgmtException e) {
			logger.error(e);
			return null;
		}
	}
	
	private void addTabsPanel() {
		List<ITab> tabs = new ArrayList<ITab>();
		
		for (RelationTemplate template : getModelObject().getRelations()) {
			tabs.add(new TemplateTab(new ObjectModel<RelationTemplate>(template)));
		}
		
		AjaxTabbedPanel<ITab> panel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
			@Override
			protected String getNavCss() {
				return "nav nav-tabs nav-horizontal";
			}
		};
		
		if (get("tabs")!=null)
			replace(panel);
		else
			add(panel);
	}
}

package kbee.web.model.contentclass;

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
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.relationshipsbycriteria.KbeeRelationshipByCriteriaTemplate;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
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
public class RelationsByCriteriaEditor extends ModelPanel<ContentTemplate> {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = new Logger(LogManager.getLogger(RelationsByCriteriaEditor.class.getName()));
	 
	private class TemplateTab implements ITab, IDetachable {
		private IModel<RelationshipByCriteriaTemplate> model;
		public TemplateTab(IModel<RelationshipByCriteriaTemplate> model) {
			this.model = model;
		}
		public IModel<String> getTitle() {
			return new Model<String>(getModel().getObject().getName());
		}
		public Panel getPanel(String id) {
			Panel editor = new RelationByCriteriaTemplateEditor(id, getModel()) {
				protected void onDelete(AjaxRequestTarget target) {
					addTabsPanel();
					target.add(RelationsByCriteriaEditor.this.get("tabs"));
				}
			};
			return editor;
		}
		public boolean isVisible() {
			return true;
		}
		public IModel<RelationshipByCriteriaTemplate> getModel() {
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
	public RelationsByCriteriaEditor(String id, IModel<ContentTemplate> model) {
		super(id, model);
	
		add(new AjaxLink<Void>("new-button") {
			public void onClick(AjaxRequestTarget target) {
				RelationshipByCriteriaTemplate template = getNewTemplate();
				@SuppressWarnings("unchecked")
				VerticalLayout<ITab> tabs = (VerticalLayout<ITab>)RelationsByCriteriaEditor.this.get("tabs");
				tabs.setTitle(new StringResourceModel("sections", this, null));
				
				tabs.getTabs().add(new TemplateTab(new ObjectModel<RelationshipByCriteriaTemplate>(template)));
				tabs.setSelectedTab(tabs.getTabs().size()-1);
				target.add(RelationsByCriteriaEditor.this);
			}
			public boolean isVisible() {
				return isRoot() || !isFreeVersion();
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
	
	private RelationshipByCriteriaTemplate getNewTemplate() {
		try {
			ContentTemplate contenttemplate = getModelObject();
			RelationshipByCriteriaTemplate relationtemplate = new KbeeRelationshipByCriteriaTemplate();
			contenttemplate.getRelationshipsByCriteria().add(relationtemplate);
			contenttemplate.getService(DOMObjectService.class).update();
			return relationtemplate;
		}
		catch (ContentMgmtException e) {
			logger.error(e);
			return null;
		}
	}
	
	private void addTabsPanel() {
//		List<ITab> tabs = new ArrayList<ITab>();
//		
//		for (RelationshipByCriteriaTemplate template : getModelObject().getRelationshipsByCriteria()) {
//			tabs.add(new TemplateTab(new ObjectModel<RelationshipByCriteriaTemplate>(template)));
//		}
//		
//		VerticalLayout<ITab> panel = new VerticalLayout<ITab>("tabs",this.getClass().getName(), tabs) {
//			@Override
//			protected String getNavCss() {
//				return "nav nav-tabs nav-horizontal";
//			}
//		};
//		
//		if (get("tabs")!=null)
//			replace(panel);
//		else
//			add(panel);
		
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		for (RelationshipByCriteriaTemplate template : getModelObject().getRelationshipsByCriteria()) {
			tabs.add(new TemplateTab(new ObjectModel<RelationshipByCriteriaTemplate>(template)));
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

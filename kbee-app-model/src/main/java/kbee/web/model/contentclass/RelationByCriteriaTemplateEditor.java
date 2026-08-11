package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.relationshipsbycriteria.KbeeRelationshipByCriteriaTemplate;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;

import kbee.util.logging.Logger;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class RelationByCriteriaTemplateEditor extends ObjectEditor<RelationshipByCriteriaTemplate> {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = new Logger(LogManager.getLogger("TxLogger"));

	public RelationByCriteriaTemplateEditor(String id, IModel<RelationshipByCriteriaTemplate> relationtemplatemodel) {
		super(id, relationtemplatemodel);
		
		setEditionEnabled(false);
		
		final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("name"));
		form.add(new TextField<String>("targetLabel"));
		form.add(new TextField<String>("reverseLabel"));
		form.add(new ChoiceField<Classifier>("classifier", () -> getClassifiers(), true));
		add(form);
		
		add(new EditButtonsV5<RelationshipByCriteriaTemplate>(this) {
			@Override
			public boolean isEnabled() {
				return isRoot() || (role_admin && !isFreeVersion());
			}
		});
		
		add(new AjaxLink<Void>("delete-button") {
			public void onClick(AjaxRequestTarget target) {
				getConfirmationDialog().open(target, new Model<String>("Delete Relationship ?"), Dialog.Delete, new Dialog.Handler() {
					@Override
					public void onClick(AjaxRequestTarget target, Button button) {
						if (button.key().equals(Dialog.Delete.key())) {
							executeDelete(target);
							onDelete(target);
						}
					}
				});
			}
			public boolean isVisible() {
				return !isEditionEnabled();// && (getProcesses()==0);
			}
		});
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			ContentTemplate template = ((KbeeRelationshipByCriteriaTemplate)getModelObject()).getSourceTemplate();
			template.getService(DOMObjectService.class).update();
			super.reset();
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}

	public List<ContentTemplate> getContentTemplates() {
		return getContentDao().getTemplates(getDomain());
	}
	
	protected void onDelete(AjaxRequestTarget target) {
		
	}

	protected void executeDelete(AjaxRequestTarget target) {
		RelationshipByCriteriaTemplate relation = (RelationshipByCriteriaTemplate)getModelObject();
		ContentTemplate template = ((KbeeRelationshipByCriteriaTemplate)relation).getSourceTemplate();
		template.getRelationshipsByCriteria().remove(relation);
		setUpdatedPart("delete "+relation.getName());
		update(target);
	}
	
	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			classifiers.add(classifier);
		}
		Collections.sort(classifiers , new Comparator<Classifier>() {
			@Override
			public int compare(Classifier o1, Classifier o2) {
				if (o1.getDisplayName()==null)
					return 1;
				if (o2.getDisplayName()==null)
					return -1;
				return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
			}
		});
		return classifiers;
	}
	
	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) getParent().getParent().getParent().get("confirmation-dialog");
	}
	
	protected boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
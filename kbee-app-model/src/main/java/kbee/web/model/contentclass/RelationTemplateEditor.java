package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeRelationTemplate;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class RelationTemplateEditor extends ObjectEditor<RelationTemplate> {
	private static final long serialVersionUID = 1L;
	
	static kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(RelationTemplateEditor.class.getName()));
	
	public class AnyTemplate extends KbeeContentTemplate {
	}
	
	public class AnyTemplateModel implements IModel<ContentTemplate> {
		private ContentTemplate any;
		@Override
		public ContentTemplate getObject() {
			if (any==null) any = new AnyTemplate();
			return any;
		}
		@Override
		public void setObject(ContentTemplate template) {
		}
		@Override
		public void detach() {
			any = null;
		}
	}
	
	public enum DisplayMode {
		
		LINK (0, "Link"), 
		RESOURCE (1, "Resource"), 
		EMBEDDED (2, "Embedded"), 
		RESOURCEANDLINK (3, "Resource & Link"); 
		
		private int code;
		private String label;
		
		private DisplayMode(int code, String label) {
			this.label = label;
			this.code = code;
		}
		public int getCode() {
			return code;
		}
		public String getLabel() {
			return label;
		}
		static public DisplayMode valueOf(int code) {
			for (DisplayMode mode : values()) {
				if (mode.getCode()==code)
					return mode;
			}
			return null;
		}
	}

	public RelationTemplateEditor(String id, IModel<RelationTemplate> relationtemplatemodel) {
		super(id, relationtemplatemodel);
		
		setEditionEnabled(false);
		
		
		final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("name"));
		
		form.add(new RelationTargetEditor());
		form.add(new TextField<String>("targetLabel"));
		form.add(new ChoiceField<DisplayMode>("targetDisplayMode", new PropertyModel<DisplayMode>(this, "targetDisplayMode"), () -> getDisplayModes()));
		form.add(new TextField<String>("reverseLabel"));
		form.add(new NumberField<Integer>("reverseOrder"));
		form.add(new ChoiceField<DisplayMode>("reverseDisplayMode", new PropertyModel<DisplayMode>(this, "reverseDisplayMode"), () -> getDisplayModes()));
		form.add(new ChoiceField<Multiplicity>("multiplicity", () -> getMultiplicities()));
		form.add(new NumberField<Integer>("targetOrder"));
		form.add(new BooleanField("aggregation"));
		form.add(new BooleanField("keepVersion"));
		form.add(new ChoiceField<ObjectState>("state", () -> getStates())); 
		
		add(form);
		
		add(new EditButtonsV5<RelationTemplate>(this) {
			@Override
			public boolean isEnabled() {
				if (isRoot())
					return true;
				return (role_admin && !isFreeVersion());
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
				return !isEditionEnabled() && getInstances()==0;
			}
		});
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			ContentTemplate template = ((KbeeRelationTemplate)getModelObject()).getSourceTemplate();
			template.getService(DOMObjectService.class).update(getUpdatedParts());
			super.reset();
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public DisplayMode getTargetDisplayMode() {
		DisplayMode mode = DisplayMode.valueOf(getModelObject().getTargetDisplayMode());
		if (mode==null) mode = DisplayMode.LINK;
		return mode;
	}
	
	public void setTargetDisplayMode(DisplayMode mode) {
		((KbeeRelationTemplate)getModelObject()).setTargetDisplayMode(mode.getCode());
	}
	
	public ContentTemplate getTargetTemplate() {
		ContentTemplate template = ((KbeeRelationTemplate)getModelObject()).getTargetTemplate();
		if (template==null) template = new AnyTemplate();
		return template;
	}
	
	public void setTargetTemplate(ContentTemplate template) {
		if (template instanceof AnyTemplate)
		((KbeeRelationTemplate)getModelObject()).setTargetTemplate(null);
		else
		((KbeeRelationTemplate)getModelObject()).setTargetTemplate(template);
	}
	
	public DisplayMode getReverseDisplayMode() {
		DisplayMode mode = DisplayMode.valueOf(getModelObject().getReverseDisplayMode());
		if (mode==null) mode = DisplayMode.LINK;
		return mode;
	}
	
	public void setReverseDisplayMode(DisplayMode mode) {
		((KbeeRelationTemplate)getModelObject()).setReverseDisplayMode(mode.getCode());
	}
	
	public List<ContentTemplate> getContentTemplates() {
		List<ContentTemplate> templates = getContentDao().getTemplates(getDomain());
		ContentTemplate anytemplate = new AnyTemplate();
		anytemplate.setName((new StringResourceModel("anytemplate.name", this)).getObject());
		templates.add(0, anytemplate);
		return templates;
	}
	
	public boolean isAggregation() {
		return ((KbeeRelationTemplate)getModelObject()).isAggregation();
	}
	
	public void setAggregation(boolean value) {
		((KbeeRelationTemplate)getModelObject()).setAggregation(value);
	}
	
	public List<DisplayMode> getDisplayModes() {
		List<DisplayMode> modes = new ArrayList<DisplayMode>();
		modes.add(DisplayMode.LINK);
		modes.add(DisplayMode.RESOURCE);
		modes.add(DisplayMode.EMBEDDED);
		modes.add(DisplayMode.RESOURCEANDLINK);
		return modes;
	}
	
	public Multiplicity getMultiplicity() {
		return ((KbeeRelationTemplate)getModelObject()).getMultiplicity();
	}
	
	public void setMultiplicity(Multiplicity value) {
		((KbeeRelationTemplate)getModelObject()).setMultiplicity(value);
	}
	
	public List<ObjectState> getStates() {
		List<ObjectState> states = new ArrayList<>();
		states = new ArrayList<ObjectState>();
		states.add(ObjectState.ENABLED);
		states.add(ObjectState.ARCHIVED);
		return states;
	}
	
	public List<Multiplicity> getMultiplicities() {
		List<Multiplicity> multiplicities = new ArrayList<Multiplicity>();
		multiplicities.add(Multiplicity.M01);
		multiplicities.add(Multiplicity.M11);
		multiplicities.add(Multiplicity.M0N);
		multiplicities.add(Multiplicity.M1N);
		return multiplicities;
	}
	
	protected void onDelete(AjaxRequestTarget target) {
		
	}

	protected void executeDelete(AjaxRequestTarget target) {
		RelationTemplate relation = (RelationTemplate)getModelObject();
		ContentTemplate template = ((KbeeRelationTemplate)relation).getSourceTemplate();
		template.getRelations().remove(relation);
		setUpdatedPart("delete "+relation.getName());
		update(target);
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
	
	protected int getInstances() {
		List<com.novamens.content.base.Relation> relations = getContentDao().getRelationsByTemplate(getModelObject());
		return relations.size();
	}
	
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
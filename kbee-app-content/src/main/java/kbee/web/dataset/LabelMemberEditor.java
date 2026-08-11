package kbee.web.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.LabelColor;
import com.novamens.content.model.LabelMember;
import com.novamens.content.service.DOMObjectService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeLabelMember;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.editor.MemberClassificationEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class LabelMemberEditor extends DomainObjectEditor<LabelMember> {
			
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(LabelMemberEditor.class.getName());
	
	private boolean is_external = false;
	
	private LabelColor labelcolor;
	WebMarkupContainer icon;
	Form<?> form;
	
	final boolean role_admin 				= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model 				= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_members 		= role_model || role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_dataset_members_read = role_dataset_members || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
					
	class UniquenessValidator implements IValidator<String> {
		private static final long serialVersionUID = 1L;
		@Override
		public void validate(final IValidatable<String> validatable) {
			String membername = validatable.getValue();
			DataSetMember member =  getContentDao().findMemberByValue(getModelObject().getDataSet(), membername);
			if (member!=null && !member.equals(getModelObject())) {
				validatable.error(new ValidationError(this));
			}
		}
	}
	
	public LabelMemberEditor(IModel<LabelMember> model) {
		this("editor", model, false);
	}

	
	public LabelMemberEditor(String id, IModel<LabelMember> model, boolean isNew) {
			this(id, model, isNew, false);
	}

	
	/**
	 * @param id
	 * @param model
	 */											
	public LabelMemberEditor(String id, IModel<LabelMember> model, boolean isNew, boolean isReadOnly) {
		super(id, model);
		
		setIsNew(isNew);
		setReadOnly(isReadOnly);
		
		logger.debug(model.getObject().getClass().getName());
		
		this.is_external = LabelMemberEditor.this.getModelObject().getDataSet().getDataSetType().equals(DataSetType.EXTERNAL);
		
		WebMarkupContainer alertext = new WebMarkupContainer("alert-external") {
			@Override
			public boolean isVisible() {
				return LabelMemberEditor.this.getModelObject().getDataSet().getDataSetType().equals(DataSetType.EXTERNAL);
			}
		};

		alertext.add(new Label("note-external", new StringResourceModel("external", LabelMemberEditor.this, null)));
		add(alertext);
		
		 form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("value", true, new UniquenessValidator()) {
			@Override
			public boolean autofocus() {
				return true;
			}
		});
	
		if (LabelMemberEditor.this.getModel().getObject() instanceof LabelMember)
			labelcolor = ((LabelMember) getModel().getObject()).getLabelColor();
		else
			labelcolor = LabelColor.BLUE;
			
		 
		form.add(new ChoiceField<LabelColor>("labelcolor", new PropertyModel<LabelColor>(this, "labelcolor"), new PropertyModel<List<LabelColor>>(this, "LabelColors"), true) {
			
			protected String getDisplayValue(LabelColor value) {
				return value.getLabel(getSessionUser().getLocale());
			}
			
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				icon = new WebMarkupContainer("icon");
				icon.add( new AttributeModifier("class", "fa-regular fa-tag " + (getValue()!=null?getValue().getKey():"")));
				form.addOrReplace(icon);
				target.add(LabelMemberEditor.this);
				
			}
			
			@Override
			public boolean isVisible() {
				return LabelMemberEditor.this.getModel().getObject().getDataSet().getDataSetType()==DataSetType.LABEL;
			}
		});

		
		//WebMarkupContainer tg=new WebMarkupContainer("tag");
		//tg.add( new AttributeModifier("class", get("form").get()));
				
		form.add(new MemberClassificationEditor(isReadOnly()));
		
		
		icon = new WebMarkupContainer("icon");
		icon.add( new AttributeModifier("class", "fa-regular fa-tag " + (getLabelColor()!=null?getLabelColor().getKey():"")));
		form.add(icon);
				
		
		add(form);
		
		add(new EditButtonsV5<LabelMember>(this) {
			@Override
			public boolean isVisible() {

				if (getModel().getObject().getDataSet().isReadonly())
					return isRoot();
				
				if (getModel().getObject().getState()==ObjectState.DELETED)
					return false;
				
				if (isReadOnly())
					return false;
				
				if (isSupportSessionUser() && !isRoot())
					return false;
				
				if (isExternal())
					return false;
				
				if (!role_dataset_members)
					return false;
				
				return true;
			}
		});
	}
	

	
	/**
	 * 
	 * 
	 */
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				((KbeeLabelMember) getModelObject()).setLabelColor(getLabelColor());
				((KbeeLabelMember) getModelObject()).getService(DOMObjectService.class).update(getUpdatedParts());
				reset();
				target.add(LabelMemberEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	
	public void setLabelColor(LabelColor c) {
		this.labelcolor=c;
	}

	
	public LabelColor getLabelColor() {
		return this.labelcolor;
	}


	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		if (isNew()) {
			try {
				getModelObject().getService(DOMObjectService.class).delete();
			}
			catch ( ConstraintException | ContentMgmtException | ServiceNotFoundException e) {
				logger.error(e);
				fire(new ErrorEvent<>(target, e));
			}
			onCancel(target);
		}
	}
	

	public void onUpdate(AjaxRequestTarget target) {
	}

	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	
	public List<LabelColor> getLabelColors() {
		
		List<LabelColor> list = new ArrayList<LabelColor>();
		final LabelColor la[] = LabelColor.getAll(); 
		for (LabelColor l: la) 
			list.add(l);
		Collections.sort(list, new Comparator<LabelColor>() {
			@Override
			public int compare(LabelColor o1, LabelColor o2) {
				try {
					return o1.getLabel(getSessionUser().getLocale()).compareToIgnoreCase(o2.getLabel(getSessionUser().getLocale()));
				} catch (Exception e) {
					logger.error(e.getClass().getName(), e);
					return 0;
				}
			}
		});
		return list;
	}
	
	
	private boolean isExternal() {
		return this.is_external;
	}

}

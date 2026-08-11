package kbee.web.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
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
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.LabelColor;
import com.novamens.content.model.LabelMember;
import com.novamens.content.service.DOMObjectService;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.model.KbeeLabelMember;
import com.novamens.kbee.content.service.MemberSuggestionService;
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
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.form.EditButtonsV5;
import kbee.web.page.ErrorPageEvent;

@Deprecated
@SuppressWarnings("serial")
public class MemberEditor extends DomainObjectEditor<DataSetMember> {

	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(MemberEditor.class.getName()));

	private boolean is_external = false;
	
	private LabelColor labelcolor;
	
	final boolean role_admin 				= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model 				= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_members 		= role_model || role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_dataset_members_read = role_dataset_members || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
					
	class UniquenessValidator implements IValidator<String> {
		@Override
		public void validate(final IValidatable<String> validatable) {
			String membername = validatable.getValue();
			DataSetMember member =  getContentDao().findMemberByValue(getModelObject().getDataSet(), membername);
			if (member!=null && member.getState()!=ObjectState.DELETED && !member.equals(getModelObject())) {
				validatable.error(new ValidationError(this));
			}
		}
	}
	
	class CycleValidator implements IValidator<DataSetMember> {
		@Override
		public void validate(final IValidatable<DataSetMember> validatable) {
			validate(validatable.getValue(), validatable);
		}
		private void validate(DataSetMember value, IValidatable<DataSetMember> validatable) {
			if (value.getParents()==null) return;
			for (DataSetMember parent : value.getParents()) {
				if (parent.equals(getModelObject())) {
					validatable.error(new ValidationError(this));
					break;
				}
				else {
					validate(parent, validatable);
				}
			}
		}
	}
	
	public MemberEditor(IModel<DataSetMember> model) {
		this("editor", model, false);
	}

	
	public MemberEditor(String id, IModel<DataSetMember> model, boolean isNew) {
		this(id, model, isNew, false);
	}
	
	public MemberEditor(String id, IModel<DataSetMember> model, boolean isNew, boolean isReadOnly) {
		super(id, model);
		
		setIsNew(isNew);
		setReadOnly(isReadOnly);
		setEditionEnabled(isNew);
		
		this.is_external = MemberEditor.this.getModelObject().getDataSet().getDataSetType().equals(DataSetType.EXTERNAL);
							
		WebMarkupContainer alertext = new WebMarkupContainer("alert-external") {
			@Override
			public boolean isVisible() {
				return MemberEditor.this.getModelObject().getDataSet().getDataSetType().equals(DataSetType.EXTERNAL);
			}
		};

		alertext.add(new Label("note-external", new StringResourceModel("external", MemberEditor.this, null)));
		add(alertext);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		form.add(new TextField<String>("value", true, new UniquenessValidator()) {
			@Override
			public boolean autofocus() {
				return true;
			}
		});

		form.add(new AutoCompleteFieldV5<DataSetMember>("parent", new CycleValidator()) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
			}	
			public List<Suggestion> getSuggestions(String pattern) {
				return getDataSet().getService(MemberSuggestionService.class).getSuggestions(pattern); 
			}
			@Override 
			public boolean isVisible() {
				return isHierachical(); 
			}
			@Override 
			public String getHistoryKey() {
				return "parent-"+getModelObject().getId(); 
			}
		}); 
	
		if (MemberEditor.this.getModel().getObject() instanceof LabelMember)
			labelcolor = ((LabelMember) getModel().getObject()).getLabelColor();
		else
			labelcolor = LabelColor.BLUE;
			
		 
		form.add(new ChoiceField<LabelColor>("labelcolor", new PropertyModel<LabelColor>(this, "labelcolor"), new PropertyModel<List<LabelColor>>(this, "LabelColors"), true) {
			protected String getDisplayValue(LabelColor value) {
				return value.getLabel(getSessionUser().getLocale());
			}
			@Override
			public boolean isVisible() {
				return MemberEditor.this.getModel().getObject().getDataSet().getDataSetType()==DataSetType.LABEL;
			}
		});

		form.add(new MemberClassificationEditor(isReadOnly()));
		
		add(form);
		
		add(new EditButtonsV5<DataSetMember>(this) {
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
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				if (getModelObject() instanceof LabelMember) {
					((KbeeLabelMember) getModelObject()).setLabelColor(getLabelColor());
					((KbeeLabelMember) getModelObject()).getService(DOMObjectService.class).update(getUpdatedParts());
				}
				else {
					getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				}
				reset();
				target.add(MemberEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire (new ErrorPageEvent(target, e));
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
				// new elements are physically removed
				getModelObject().getService(DOMObjectService.class).delete();
			}
			catch ( ConstraintException | ContentMgmtException | ServiceNotFoundException e) {
				logger.error(e);
				fire (new ErrorPageEvent(target, e));
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
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		return list;
	}
	
	public boolean isHierachical() {
		return getModelObject().getDataSet().isHierachical();
	}
	
	public DataSet getDataSet() {
		return getModelObject().getDataSet();
	}
	
	private boolean isExternal() {
		return this.is_external;
	}
}

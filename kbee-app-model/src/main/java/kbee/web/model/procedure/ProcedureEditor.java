package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;
import com.novamens.beans.BeansService;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.workflow.AttributeRule;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.ScriptRule;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.kbee.content.workflow.Subprocedure;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.workflow.Procedure;

import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class ProcedureEditor extends ObjectEditor<Procedure> {
	private static final long serialVersionUID = 1L;

	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	private boolean editionEnabled = false;
	private TasksTablePanel taskPanel;
	
	private class SubprocedureModel implements IModel<Procedure> {
		private long id;
		private transient Procedure procedure;
		public SubprocedureModel(Procedure procedure) {
			this.procedure = procedure;
		}
		public Procedure getObject() {
			if (this.procedure==null) {
				for (Procedure procedure : getProcedure().getSubprocedures()) {
					if ((long)procedure.getId()==id) {
						this.procedure = procedure;
						break;
					}
				}
				
			}
			return procedure;
		}
		public void detach() {
			if (procedure!=null) {
			this.id = (long)procedure.getId();
			procedure=null;
			}
		}
	}
	
	public class SubprocedureFragment extends Fragment implements IFormModelUpdateListener {
		private IModel<Procedure> model;
		private String name;
		public SubprocedureFragment(String id, IModel<Procedure> model) {
			super(id, "subprocedure-fragment", ProcedureEditor.this);
			this.model = model;
			setName(model.getObject().getName());
			TextField<String> name = new TextField<String>("subprocedure", new PropertyModel<String>(this, "name"));
			add(name);
			add(new TasksTablePanel(model) {
				@Override
				public IModel<Procedure> getModel() {
					return SubprocedureFragment.this.model;
				}
				@Override
				public boolean isEditionEnabled() {
					return ProcedureEditor.this.editionEnabled;
				}
				@Override
				public void updateModel() {
					getParentProcedure().setSubprocedures(getParentProcedure().getSubprocedures());
					getWorkflowDao().update(getParentProcedure());
				}
				@Override
				protected boolean enableThreads() {
					return false;
				}
			});
			add(new AjaxLink<Void>("delete-link") {
				public void onClick(AjaxRequestTarget target) {
				}
				public boolean isVisible() {
					return isEditionEnabled();
				}
				public boolean isEnabled() {
					return true;
				}
			});
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void updateModel() {
			((Subprocedure)model.getObject()).setName(getName());
		}
		public KbeeProcedure getParentProcedure() {
			return (KbeeProcedure)getProcedure();
		}
		public void onDetach() {
			super.onDetach();
			model.detach();
		}
	}
	
	public ProcedureEditor(IModel<Procedure> model) {
		this("editor", model);
	}	
	
	public ProcedureEditor(String id, IModel<Procedure> model) {
		super(id, model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setEditionEnabled(false);
				
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new StaticField<String>("id"));
		form.add(new TextField<String>("name",true)); 
		
		TextField<String> code = new TextField<String>("code",true);

		form.add(code);
		
		taskPanel = new TasksTablePanel(getModel() ) {
			@Override
			public void updateModel() {
				getWorkflowDao().update(getProcedure());
			}
		}; 
		form.add(taskPanel);
		

		
		
		form.add(new ListView<Procedure>("subprocedure", () -> getSubprocedures()) {
			public void populateItem(ListItem<Procedure> item) {
				item.add(new SubprocedureFragment("specification", new SubprocedureModel(item.getModelObject())));
			}
		});
		
		WebMarkupContainer subprocedureToolbar = new WebMarkupContainer("subprocedure-toolbar") {
			public boolean isVisible() {
				return getProcedure().getVersion()>2 && isEditionEnabled();
			}
		};
		
		subprocedureToolbar.add(new AjaxLink<Void>("addsubprocedure-link") {
			public void onClick(AjaxRequestTarget target) {
				addSubprocedure();
				target.add(ProcedureEditor.this);
			}
			public boolean isEnabled() {
				return isEditionEnabled();
			}
		});
		
		form.add(subprocedureToolbar);
					
		form.add(new ClassifiersRulesEditor<ProcessLauncher>("classifiersrules") {
			@Override
			public List<ClassificationRule> getRules() {
 				return getRule().getRules(ClassificationRule.class);
			}
			@Override
			public void setRules(List<ClassificationRule> rules) {
				ProcedureEditor.this.setRules(rules);
			}
		});
		
		
		form.add(new AttributesRulesEditor<ProcessLauncher>("attributesrules") {
			@Override
			public List<AttributeRule> getRules() {
				return getRule().getRules(AttributeRule.class);
			}
			@Override
			public void setRules(List<AttributeRule> rules) {
				ProcedureEditor.this.setRules(rules);
			}
		});
		
		form.add(new ScriptRuleEditor<ProcessLauncher>("scriptrule") {
			@Override
			public ContentTemplate getTemplate() {
				return ((ContentProcedure)getProcedure()).getContentTemplate();
			}
			@Override
			public ScriptRule getRule() {
				return ProcedureEditor.this.getRule().getRule(ScriptRule.class);
			}
			@Override
			public void setRule(ScriptRule rule) {
				ProcedureEditor.this.setRules(Collections.singletonList(rule));
			}
		});
		
		form.add(new WRolesEditor(getModel()));
		form.add(new PhasesEditor(getModel()));
		
		add(form);
		
		add(new EditButtonsV5<Procedure>(this) {
			public void onSubmitClick(AjaxRequestTarget target)  {
				super.onSubmitClick(target);
				ProcedureEditor.this.onSubmitClick(target);
			}
			public void onCancelClick(AjaxRequestTarget target)  {
				super.onCancelClick(target);
				ProcedureEditor.this.onCancelClick(target);
			}
			public void onEditClick(AjaxRequestTarget target)  {
				ProcedureEditor.this.onEditClick(target);
				super.onEditClick(target);
			}
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				return role_admin || role_model;
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			@Override
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
		});
		
		add(new InfoDialog("help-modal"));
	}

	public Procedure getProcedure() {
		return getModelObject();
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	public List<Procedure> getSubprocedures() {
		List<Procedure> procedures = ((KbeeProcedure)getProcedure()).getSubprocedures();
		if (procedures==null) procedures = new ArrayList<>();
		return procedures;
	}
	
	public MultipleRule getRule() {
		MultipleRule rule = (MultipleRule)((KbeeProcedure)getProcedure()).getInitialRule();
		if (rule == null) rule = new MultipleRule();
		return rule;
	}
	
	public <T extends WorkflowRule> void setRules(List<T> rules) {
		MultipleRule rule = getRule();
		rule.setRules(rules);
		((KbeeProcedure)getProcedure()).setInitialRule(rule);
	}
	
	public void update(AjaxRequestTarget target) {
		if (!getUpdatedParts().isEmpty()) {
			KbeeProcedure procedure = (KbeeProcedure)getModelObject();
			
			procedure.setTasks(procedure.getTasks());
			procedure.setSubprocedures(procedure.getSubprocedures());
			
			onUpdate(target);
			procedure.getDomain().getService(WorkflowDomainService.class).update(procedure, getUpdatedParts());
			reset();
		}
	}
	
	private void addSubprocedure() {
		KbeeProcedure procedure = new KbeeProcedure();
		procedure.setName("New Procedure");
		List<Procedure> subprocedures = getProcedure().getSubprocedures();
		long id = 1;
		for (Procedure s : subprocedures) {
			if ((long)s.getId()>=id) id++;
		}
		procedure.setId(id);
		subprocedures.add(procedure);
		((KbeeProcedure)getProcedure()).setSubprocedures(subprocedures);
		getWorkflowDao().update(getProcedure());
	}
	
	private void onSubmitClick(AjaxRequestTarget target) {
		editionEnabled = false;
		taskPanel.setEditEnabled(false);
	}
	
	private void onCancelClick(AjaxRequestTarget target) {
		editionEnabled = false;
		taskPanel.setEditEnabled(false);
	}

	private void onEditClick(AjaxRequestTarget target) {
		editionEnabled = true;
		taskPanel.setEditEnabled(true);
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
}
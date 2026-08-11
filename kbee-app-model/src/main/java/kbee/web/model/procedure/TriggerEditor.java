package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.kbee.content.workflow.KbeeCollaboratorTrigger;
import com.novamens.kbee.content.workflow.KbeeLastUserAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeLastUserManualTrigger;
import com.novamens.kbee.content.workflow.KbeeManualTrigger;
import com.novamens.kbee.content.workflow.KbeeRoleAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeRoundRobin;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeUserAutomaticTrigger;
import com.novamens.kbee.content.workflow.KbeeWRoleTrigger;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.security.acl.Permission;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;
import com.novamens.workflow.Trigger;
import com.novamens.workflow.TriggerType;

@SuppressWarnings("serial")
public class TriggerEditor<T> extends ObjectEditorPanel<T>{
	private static final long serialVersionUID = 1L;
	
	private IModel<Task> taskmodel;
	private TriggerType triggerType;
	
	public TriggerEditor(String id, Trigger trigger, IModel<Task> taskmodel) {
		super(id);
		
		setOutputMarkupId(true);
		
		setTaskModel(taskmodel);
		setTrigger(trigger);
		
		add(new ChoiceField<TriggerType>("triggerType", new PropertyModel<TriggerType>(this, "triggerType"), new PropertyModel<List<TriggerType>>(this, "triggerTypes")) {
			@Override
			public boolean isEnabled() {
				return true;
				//return super.isEnabled() && (isRoot() || getProcedure()==null || getProcedure().getVersion()>1);
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setTriggerType(getValue());
				setUpdatedPart("trigger");
				target.add(TriggerEditor.this);
			}
			@Override
			public String getNullValidDisplayValue() {
				return getLabelString("none");
			}
			@Override
			public boolean isNullValid() {
				return true;
			}
		});
		
		add(new Label("trigger.help", new Model<String>() {
			public String getObject() {
				try {
					return getLabelString("trigger."+(getTriggerType()!=null?getTriggerType().getId():"none")+".help");
				}
				catch (Exception e) {
					return "-";
				}
			}
		}));
		
		((Label)get("trigger.help")).setEscapeModelStrings(false);
		
		add(new Label("trigger.1task.help", new Model<String>() {
			public String getObject() {
				try {
					return getLabelString("trigger.1task.help");
				}
				catch (Exception e) {
					return "-";
				}
			}
		}));
	}
	
	public void setTrigger(Trigger trigger) {
		if (trigger==null) return;
		setTriggerType(trigger.getType().equals(TriggerType.AUTOMATIC) ? TriggerType.USERAUTOMATIC : trigger.getType());
	}
	
	public void setTaskModel(IModel<Task> model) {
		this.taskmodel = model;
	}
	
	public IModel<Task> getTaskModel() {
		return taskmodel;
	}
	
	public Task getTask() {
		return getTaskModel().getObject();
	}
	
	public TriggerType getTriggerType() {
		return triggerType;
	}
	
	public void setTriggerType(TriggerType type) {
		this.triggerType = type;
	}
	
	public Permission getTakePerm() {
		String action = "take";
		KbeePermission perm = (KbeePermission)getPerm(action+"-"+getTask().getId());
		perm.setAction(action);
		String actionLabel = getLabelString("take-label");
		perm.setActionLabel(actionLabel);
		String label = actionLabel + " " + getTask().getDisplayName();
		perm.setLabel(label);
		return perm;
	}
	
	public Permission getRoundRobinPerm() {
		KbeePermission perm = (KbeePermission)getPerm(getTask().getId());
		perm.setLabel(getTask().getDisplayName());
		return perm;
	}
	
	public Permission getRoundRobinBackupPerm() {
		String action = "backup";
		KbeePermission perm = (KbeePermission)getPerm(action+"-"+getTask().getId());
		perm.setAction(action);
		String actionLabel = getLabelString("backup-label");
		perm.setActionLabel(actionLabel);
		String label = actionLabel + " " + getTask().getDisplayName();
		perm.setLabel(label);
		return perm;
	}
	
	public List<TriggerType> getTriggerTypes() {
		List<TriggerType> types = new ArrayList<TriggerType>();
		types.add(TriggerType.USERAUTOMATIC);
		types.add(TriggerType.USERAUTOMATIC_LASTUSER);
		types.add(TriggerType.USERAUTOMATIC_ROLE);
		types.add(TriggerType.ROLE);
		types.add(TriggerType.MANUAL);
		types.add(TriggerType.MANUAL_LASTUSER);
		types.add(TriggerType.COLLABORATOR);
		
		types.sort(new Comparator<TriggerType>() {
			@Override
			public int compare(TriggerType a, TriggerType b) {
				try { 
					return a.getLabel().compareToIgnoreCase(b.getLabel());
				} 
				catch (Exception e) {
					return 0;
				}
			}
		});
		return types;
	}
	
	protected Trigger getTrigger() {
		
		if (triggerType==null) return null;
		
		if (triggerType.equals(TriggerType.MANUAL_LASTUSER) ||
			triggerType.equals(TriggerType.MANUAL))	{
			
			KbeeManualTrigger trigger = triggerType.equals(TriggerType.MANUAL) ?
					new KbeeManualTrigger() :
					new KbeeLastUserManualTrigger();	
			
			trigger.setManualPermission(getTakePerm());
			
			return trigger;
		}
		
		if (triggerType.equals(TriggerType.USERAUTOMATIC) || 
			triggerType.equals(TriggerType.USERAUTOMATIC_LASTUSER) ||
			triggerType.equals(TriggerType.USERAUTOMATIC_ROLE)) {
			
			KbeeUserAutomaticTrigger trigger = null; 
			if (triggerType.equals(TriggerType.USERAUTOMATIC)) 
				trigger = new KbeeUserAutomaticTrigger();
			if (triggerType.equals(TriggerType.USERAUTOMATIC_LASTUSER)) 
				trigger = new KbeeLastUserAutomaticTrigger();
			if (triggerType.equals(TriggerType.USERAUTOMATIC_ROLE)) 
				trigger = new KbeeRoleAutomaticTrigger();
			
			KbeeRoundRobin strategy = new KbeeRoundRobin();
			strategy.setPermission(getRoundRobinPerm());
			strategy.setBackupPermission(getRoundRobinBackupPerm());
			trigger.setUserAssignationStrategy(strategy);
			
			trigger.setManualPermission(getTakePerm());
			
			return trigger;
		}
		
		if (triggerType.equals(TriggerType.ROLE)) {
			KbeeWRoleTrigger trigger = new KbeeWRoleTrigger();
			trigger.setManualPermission(getTakePerm());
			return trigger;
		}
		
		if (triggerType.equals(TriggerType.COLLABORATOR)) {
			KbeeCollaboratorTrigger trigger = new KbeeCollaboratorTrigger();
			trigger.setManualPermission(getTakePerm());
			return trigger;
		}
		
		return null;
	}
	
	public Procedure getProcedure() {
		return getTask()!=null ? ((KbeeTask)getTask()).getProcedure() : null;
	}
	
	private Permission getPerm(String name) {
		name = String.valueOf(getProcedure().getId()) + "-" + name;
		KbeePermission permission = KbeePermission.valueOf(name.toLowerCase());
		return permission;
	} 
	
//	private boolean isRoot() {
//		return ServiceLocator.getService(SecurityService.class).isRoot();
//	}
}
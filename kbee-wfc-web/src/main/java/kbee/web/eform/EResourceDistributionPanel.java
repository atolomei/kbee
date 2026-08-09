package kbee.web.eform;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.ProcessLaunched;
import com.novamens.content.form.ResourceMoved;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.form.KbeeEResourceDistribution;
import com.novamens.kbee.content.form.KbeeEResourceDistributionFieldModel;
import com.novamens.kbee.content.form.KbeeEResourceSystemV2;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Process;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class EResourceDistributionPanel extends EResourceSystemPanelV2 {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(EResourceDistributionPanel.class.getName());
	
	private IModel<ProcessLauncher> launchermodel;

	public EResourceDistributionPanel(String id, KbeeEResourceDistribution field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	protected Panel getActionsMenu() {
		try {
			ContextMenuPanel<KbeeEResourceSystemV2> menu = new ContextMenuPanel<KbeeEResourceSystemV2>( getFieldModel() );
			menu.setOutputMarkupId(true);
			
			menu.addItem((id) ->
				new HeaderMenuItemPanelV5<KbeeEResourceSystemV2>(id) {
					public String getLabel() {
						return getLabelString("menu.create");
					}
				});
			
			for (IModel<ProcessLauncher> launchermodel : getLaunchers()) {
				
				menu.addItem(id ->
					new AjaxMenuItemPanelV5<KbeeEResourceSystemV2>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							EResourceDistributionPanel.this.launchermodel = launchermodel;
							showSelection(target);
						}
						@Override
						public String getLabel() {
							return launchermodel.getObject().getDisplayName();
						}
						@Override
						public boolean isEnabled() {
							return !getSelection().isEmpty();
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
					}
				);
			}
			
	
			return menu;
		}
		catch (Exception e) {
			logger.error(e, getSessionUser().getUserName());
			return new InvisiblePanel("menu");
		}
	}
	
	@Override
	protected boolean isSelectionEnabled() {
		return true;
	}
	
	public IModel<ProcessLauncher> getLauncherModel() {
		return launchermodel;
	}
	
	public ProcessLauncher getLauncher() {
		return launchermodel!=null ? launchermodel.getObject() : null;
	}
	
	@Override
	protected WebMarkupContainer getActionPanel() {
		ResourceTag doneTag = ((KbeeEResourceDistributionFieldModel)getField().getModel()).getDoneTag();
		ResourceTag targetTag = ((KbeeEResourceDistributionFieldModel)getField().getModel()).getTargetTag();
		return new ELauncherPanel(getContentModel(), 
				getSelectedResources(), 
				getLauncherModel(), 
				new ObjectModel<ResourceTag>(doneTag), 
				new ObjectModel<ResourceTag>(targetTag)) {
			@Override
			protected void onBeforeLaunch(AjaxRequestTarget target) {
				getEditor().update(target);
			}
			@Override
			protected void onAfterLaunch(AjaxRequestTarget target, Process process) {
				onLaunch(target, process);
			}
			@Override
			protected void onCancel(AjaxRequestTarget target) {
				refresh(target);
			}
		};
	}
	
	private void onLaunch(AjaxRequestTarget target, Process process) {

		com.novamens.workflow.Activity wfactivity = process.getContext().getCurrentActivity();
		User user = wfactivity!=null ? wfactivity.getUser() : null;
		List<Resource>  resources = new ArrayList<>();
		ResourceTag tag = ((KbeeEResourceDistributionFieldModel)getField().getModel()).getTag();
		ResourceTag doneTag = ((KbeeEResourceDistributionFieldModel)getField().getModel()).getDoneTag();

		for (IModel<ResourceNode> selectedmodel : getSelectedResources()) {
			resources.add(selectedmodel.getObject().getResource());
		}	
		setUpdatedField(new ProcessLaunched(getData().getForm(), getLabel(), getLauncher(), resources, user));
		if (!tag.equals(doneTag)) {
			for (IModel<ResourceNode> selectedmodel : getSelectedResources()) {
				setUpdatedField(new ResourceMoved(getData().getForm(), getLabel(), selectedmodel.getObject().getResource(), doneTag.getDisplayName()));
				for (IModel<ResourceNode> model : getResources()) {
					if (model.getObject().getResource().equals(selectedmodel.getObject().getResource())) {
						getResources().remove(model);
						break;
					}
				}
			}
			getData().setData(getField(), getResources());
		}
		else {
			fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
		}
		
		fireScanAll(new EAjaxFormReloadEvent(target, doneTag));
		
		getSelection().clear();
		//getEditor().getModel().detach();
		//setResources();
		//setUpdated(false);
 		getEditor().update(target);
		refresh(target);
	}
	
	private List<IModel<ResourceNode>> getSelectedResources() {
		List<IModel<ResourceNode>> resources = new ArrayList<>();
		for (IModel<ResourceNode> nodemodel : getSelection()) {
			resources.add(nodemodel);
		}
		return resources;
	}
	
	private List<IModel<ProcessLauncher>> getLaunchers() {
		List<IModel<ProcessLauncher>> launchers = new ArrayList<>();
		for (ProcessLauncher launcher: ((KbeeEResourceDistributionFieldModel)getField().getModel()).getLaunchers()) {
			if (launcher.isEnabled() && 
				launcher.executeable() && 
				launcher.getContentTemplate()!=null && 
				launcher.getContentTemplate().getState()==ObjectState.ENABLED) { 
				launchers.add( new ObjectModel<ProcessLauncher>(launcher));
			}	
		}
		return launchers;
	}
}
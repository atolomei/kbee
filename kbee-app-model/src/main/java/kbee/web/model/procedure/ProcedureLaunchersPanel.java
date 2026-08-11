package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class ProcedureLaunchersPanel extends ObjectEditor<Procedure>  {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = Logger.getLogger(ProcedureLaunchersPanel.class.getName());

	
	List<IModel<ProcessLauncher>> launchersmodels;
	
	public ProcedureLaunchersPanel(String id, IModel<Procedure> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	public ContentProcedure getProcedure() {
		return (ContentProcedure)getModelObject();
	}
	
	public List<IModel<ProcessLauncher>> getLaunchers() {
		List<IModel<ProcessLauncher>> launchers = new ArrayList<>();
		for (ProcessLauncher launcher : getProcedure().getProcessLaunchers()) {
			launchers.add(new ObjectModel<ProcessLauncher>(launcher));
		}
		return launchers;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addTable();
	}
	
	protected void onSelect(AjaxRequestTarget target, IModel<ProcessLauncher> model) {
		
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
		
	}
	
	private void addTable() {
		add(new ListView<IModel<ProcessLauncher>>("launchers", () -> getLaunchers()) {
			public void populateItem(ListItem<IModel<ProcessLauncher>> item) {
				IModel<ProcessLauncher> model = item.getModelObject();
				ProcessLauncher launcher = model.getObject();
				AjaxLink<Void> link = new AjaxLink<Void>("launcher.link") {
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, model);
					}
				};
				link.add(new Label("launcher.name", launcher.getDisplayName()));
				item.add(link);
				item.add(new Label("launcher.description", launcher.getDescription()));
				item.add(new Label("launcher.context", getContext(launcher)));
				WebMarkupContainer menu = new WebMarkupContainer("menu-container");
				menu.add(getMenu(model));
				item.add(menu);
			}
		});	
		
		add(new AjaxLink<Void>("creation-link") {
			public void onClick(AjaxRequestTarget target) {
				ProcessLauncher launcher = getDomain().getService(WorkflowDomainService.class).createLauncher(getProcedure());
				onUpdate(target);
				onSelect(target, new ObjectModel<ProcessLauncher>(launcher));
				target.add(ProcedureLaunchersPanel.this);
			}
		});
		
		add(new ConfirmationDialog("confirmation-dialog"));
	}
	
	private String getContext(ProcessLauncher launcher) {
		String context = "";
		if (launcher.isEnabled())
			context += getLabelString("mytask.context");
		if (launcher.isLibrary()) {
			if (!"".equals(context))
				context += ", ";
			context += getLabelString("library.context");
		}
		if (launcher.isMobile()) {
			if (!"".equals(context))
				context += ", ";
			context += getLabelString("mobile.context");
		}	
		return context;
	}
	
	private Panel getMenu(IModel<ProcessLauncher> model) {
		
		ContextMenuPanel<ProcessLauncher> menu = new ContextMenuPanel<ProcessLauncher>(model);
		
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<ProcessLauncher>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						getConfirmationDialog().open(target, 
								ProcedureLaunchersPanel.this.getLabel("delete.confirmation.message", getModelObject().getLabel()), 
								Dialog.Delete, 
								new Dialog.Handler() {
									@Override
									public void onClick(AjaxRequestTarget target, Button button) {
										if (button.key().equals(Dialog.Delete.key())) {
											try {
												((KbeeProcessLauncher)getModelObject()).setContentTemplate(getProcedure().getContentTemplate());
												getDomain().getService(WorkflowDomainService.class).deleteLauncher(getModelObject());
											}
											catch (Exception e2) {
												logger.error(e2);
											}
											onUpdate(target);
										}
									}
							});
					}	
					@Override
					public boolean isEnabled() {
						return true;
					}
					@Override
					public String getLabel() {	
						return getLabelString("delete.menuitem");
					}
					@Override
					public String getTarget() {
						return "_blank";
					}
			});
		
		return menu;
	}
	
	private ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("confirmation-dialog");
	}
}
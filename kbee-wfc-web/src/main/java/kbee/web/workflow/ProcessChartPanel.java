package kbee.web.workflow;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.ProcedurePhase;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;
			
public class ProcessChartPanel extends ModelPanel<WorkflowContext> {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ProcessChartPanel.class.getName());
	
	String col_item;
	
	private boolean isHide = true;

	
	public boolean isHide() {
		return this.isHide;
	}
	
	public void setHide(boolean b) {
		this.isHide=b;
	}
	
	private IModel<User> model_session_user = null;
	
	WebMarkupContainer panel;
	AjaxLink<Void> hide;
	
	public ProcessChartPanel(String id, IModel<WorkflowContext> model) {
		super(id, model);
	}
	
	
	public void addListeners() {
		super.addListeners();
		
		if (isHide()) {
			add(new WicketEventListener<ClickHideShowWorkflowEvent>() {
				private static final long serialVersionUID = 1L;
				@Override
				public void onEvent(ClickHideShowWorkflowEvent event) {
					if (panel.isVisible())
						panel.setVisible(false);
					else
						panel.setVisible(true);
					setPreference("procedure-phases", panel.isVisible()?"yes":"no");
					event.getRequestTarget().add(ProcessChartPanel.this);
					
				}
			});
		}
	}
	
	public void onDetach() {
		super.onDetach();
		if (model_session_user!=null)
			model_session_user.detach();
	}
	
	
	public String getPreference(String name, String defaultVaule) {
		String value = getSessionUser().getService(PreferencesService.class).getValue("task-editor", name, defaultVaule);
		return value;
	}
	
	public void setPreference(String name, String value) {
		getSessionUser().getService(PreferencesService.class).setValue("task-editor", name, value);
	}
	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		String pref=getPreference("procedure-phases", "no");
				
		panel = new WebMarkupContainer("panel");
		add(panel);
		
		panel.setVisible(!isHide() || pref.equals("yes"));
		
		
		hide = new AjaxLink<Void>("hide") {

			private static final long serialVersionUID = 1L;

			
			public boolean isVisible() {
				return isHide();
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
					
				if (panel.isVisible())
					panel.setVisible(false);
				else
					panel.setVisible(true);
				
				//hide.addOrReplace( new Label("show-hide", panel.isVisible()? new StringResourceModel("hide-steps", ProcessChartPanel.this, null) : new StringResourceModel("show-steps", ProcessChartPanel.this, null) ));
				
				setPreference("procedure-phases", panel.isVisible()?"yes":"no");
				target.add(ProcessChartPanel.this);
			}
		};
		
		//Label sh=new Label("show-hide",  panel.isVisible()? new StringResourceModel("hide-steps", ProcessChartPanel.this, null) : new StringResourceModel("show-steps", ProcessChartPanel.this, null));
		//hide.add(sh);
		// hide.setVisible(false);
		
		
		
		
		List<ProcedurePhase> phases = getPhases();
		
		if  (phases.size()<2) 			col_item = "col-lg-12 col-md-12 col-xs-12";
		else if (phases.size()==2)		col_item = "col-lg-6  col-md-12 col-xs-12";
		else if (phases.size()==3)		col_item = "col-lg-4  col-md-12  col-xs-12";
		else if (phases.size()==4)		col_item = "col-lg-3  col-md-12  col-xs-12";
		else if (phases.size()==5)		col_item = "col-lg-20pc  col-md-12  col-xs-12";
		else							col_item = "col-lg-2  col-md-12  col-xs-12";
		
		
		
			
		panel.add(new ListView<ProcedurePhase>("items", phases) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<ProcedurePhase> item) {
				try {
					ProcedurePhase phase=item.getModelObject();
					
					Label key   = new Label("phase-label", phase.getLabel());
					Label task  = new Label("task-label", isActive(phase) ? getTask().getDisplayName() : "");
					
					item.add(new AttributeModifier("class", col_item));
					
					WebMarkupContainer sep = new WebMarkupContainer("sep");
					sep.setVisible(item.getIndex()>0);
					item.add(sep);
					
					WebMarkupContainer pill = new WebMarkupContainer("pill");
					pill.add(new AttributeModifier("class", "square " + ( isActive(phase)  ? " active " : "") ));
					key.add(new AttributeModifier("class", isActive(phase)  ? " bold " : "") ); 
							
					item.add(pill);
					
					pill.add(key);
					pill.add(task);
					
					item.setOutputMarkupId(true);
				} 
				catch (Exception e) {
					item.addOrReplace(new Label("key", e.getClass().getName()));
					item.addOrReplace(new Label("value", e.getMessage()));
					logger.error(e);
				}	
			}
			
		});
		
		panel.add(hide);
		
	}

	public  List<ProcedurePhase> getPhases() {
		return getModelObject().getProcedure().getPhases();
	}
	
	public Task getTask() {
		return getModelObject().getTask();
	}
	
	protected boolean isActive(ProcedurePhase phase) {
		ProcedurePhase current = getModelObject().getCurrentPhase();
		return current!=null && current.getName().toLowerCase().equals(phase.getName().toLowerCase()); 
	}
	
	protected KbeeUser getSessionUser() {
		try {
			if (model_session_user != null && model_session_user.getObject() != null)
				return (KbeeUser) model_session_user.getObject();

			User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			model_session_user = new ObjectModel<User>(session_user);
			return (KbeeUser) model_session_user.getObject();
		} catch (Exception e) {
				logger.error(e);
			return null;
		}
	}

}

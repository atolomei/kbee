package kbee.web.workflow;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

import kbee.web.nav.HomeBC;

public class EFormViewerHeader extends ModelPanel<Activity>  {
	private static final long serialVersionUID = 1L;
	
	public EFormViewerHeader(String id, IModel<Activity> model) {
		super(id, model);
		add(getBreadCrumb());
	}
	
	private Panel getBreadCrumb() {
		MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
		
		bc.addElement(new BCElement( new Model<String>(getProcedure().getDisplayName())));
		bc.addElement(new BCElement( new Model<String>(getTask().getDisplayName())));
		return bc;
	}
	
	public Task getTask() {
		return getProcedure().getTask(((KbeeWorkflowActivity)getModelObject()).getTaskName());
	}
	
	public Procedure getProcedure() {
		return getModelObject().getProcess().getProcedure();
	}
}